import {
  AfterViewInit,
  Component,
  ElementRef,
  OnDestroy,
  OnInit,
  ViewChild,
} from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { finalize } from 'rxjs';
import { TravelDocumentRow } from 'src/app/core/models/travel.models';
import { TRAVEL_DOCUMENT_TYPES } from '../../shared/travel-ui.constants';
import { ToolbarButton } from 'src/app/pages/ui-components/button/toolbar/toolbar.component';
import { MenuPermissionService } from 'src/app/services/menu-permission.service';
import {
  TravelDocumentFormDialogComponent,
  TravelDocumentFormDialogData,
} from '../../dialogs/document-form-dialog/travel-document-form-dialog.component';
import { TravelKpiItem } from '../../shared/components/kpi-strip/travel-kpi-strip.component';
import { TravelLookupService } from '../../shared/services/travel-lookup.service';
import { TravelDocumentService } from '../../services/travel-document.service';

@Component({
  selector: 'app-travel-document-list',
  templateUrl: './travel-document-list.component.html',
  styleUrl: './travel-document-list.component.scss',
})
export class TravelDocumentListComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('scrollSentinel') scrollSentinel?: ElementRef<HTMLElement>;

  private scrollObserver?: IntersectionObserver;
  items: TravelDocumentRow[] = [];
  filteredItems: TravelDocumentRow[] = [];
  searchQuery = '';
  categoryFilter = 'ALL';
  readonly docTypeOptions = [...TRAVEL_DOCUMENT_TYPES];
  loading = false;
  loadingMore = false;
  hasMore = true;
  readonly route = '/travel/documents';
  kpis: TravelKpiItem[] = [];
  private page = 0;
  private readonly pageSize = 24;
  private totalPages = 0;

  constructor(
    private readonly api: TravelDocumentService,
    private readonly lookup: TravelLookupService,
    private readonly dialog: MatDialog,
    readonly menuPerm: MenuPermissionService,
    private readonly snackBar: MatSnackBar
  ) {}

  get listToolbar(): ToolbarButton[] {
    return [
      { id: 'refresh', icon: 'refresh', tooltip: 'Refresh', action: () => this.load(true), disabled: this.loading },
    ];
  }

  ngOnInit(): void {
    this.lookup.ensureAllForGrids().subscribe({ next: () => this.load(true), error: () => this.load(true) });
  }

  ngAfterViewInit(): void {
    this.setupInfiniteScroll();
  }

  ngOnDestroy(): void {
    this.scrollObserver?.disconnect();
  }

  private setupInfiniteScroll(): void {
    this.scrollObserver?.disconnect();
    const el = this.scrollSentinel?.nativeElement;
    if (!el || typeof IntersectionObserver === 'undefined') {
      return;
    }
    this.scrollObserver = new IntersectionObserver(
      (entries) => {
        if (entries.some((e) => e.isIntersecting)) {
          this.load(false);
        }
      },
      { root: null, rootMargin: '240px', threshold: 0 }
    );
    this.scrollObserver.observe(el);
  }

  load(reset: boolean): void {
    if (reset) {
      this.page = 0;
      this.items = [];
      this.hasMore = true;
      this.loading = true;
    } else {
      if (!this.hasMore || this.loadingMore) {
        return;
      }
      this.loadingMore = true;
    }
    this.api
      .gets({ pageNumber: this.page, pageSize: this.pageSize })
      .pipe(finalize(() => {
        this.loading = false;
        this.loadingMore = false;
      }))
      .subscribe({
        next: (p) => {
          const batch = p.items ?? [];
          this.items = reset ? batch : [...this.items, ...batch];
          this.totalPages = p.totalPages ?? 0;
          this.hasMore = this.page + 1 < this.totalPages;
          this.page += 1;
          this.refreshKpis();
          this.applyFilters();
          setTimeout(() => this.setupInfiniteScroll(), 0);
        },
      });
  }

  private refreshKpis(): void {
    const countType = (needle: string) =>
      this.items.filter((d) => (d.docType ?? '').toUpperCase().includes(needle)).length;
    this.kpis = [
      { label: 'Total files', value: this.items.length, icon: 'folder', tone: 'clients' },
      { label: 'Passports', value: countType('PASSPORT'), icon: 'badge', tone: 'bookings' },
      { label: 'Visa', value: countType('VISA'), icon: 'card_travel', tone: 'packages' },
      { label: 'Tickets', value: countType('TICKET'), icon: 'flight', tone: 'open' },
      { label: 'Invoices', value: countType('INVOICE'), icon: 'receipt', tone: 'finance' },
      { label: 'Insurance', value: countType('INSURANCE'), icon: 'health_and_safety', tone: 'clients' },
      { label: 'Contracts', value: countType('CONTRACT'), icon: 'gavel', tone: 'bookings' },
      { label: 'Other', value: countType('OTHER'), icon: 'more_horiz', tone: 'default' },
    ];
  }

  clientName(doc: TravelDocumentRow): string {
    return this.lookup.clientLabel(doc.clientId);
  }

  applyFilters(): void {
    let list = this.items;
    const q = this.searchQuery.trim().toLowerCase();
    if (q) {
      list = list.filter(
        (d) =>
          (d.fileName ?? '').toLowerCase().includes(q) ||
          (d.docType ?? '').toLowerCase().includes(q) ||
          this.clientName(d).toLowerCase().includes(q)
      );
    }
    if (this.categoryFilter !== 'ALL') {
      const cat = this.categoryFilter.toUpperCase();
      list = list.filter((d) => (d.docType ?? '').toUpperCase().includes(cat));
    }
    this.filteredItems = list;
  }

  openDialog(row?: TravelDocumentRow): void {
    this.dialog
      .open(TravelDocumentFormDialogComponent, {
        width: 'min(640px, 96vw)',
        maxWidth: '96vw',
        panelClass: 'travel-form-dialog-panel',
        data: { row } as TravelDocumentFormDialogData,
      })
      .afterClosed()
      .subscribe((ok) => {
        if (ok) {
          this.load(true);
        }
      });
  }

  view(doc: TravelDocumentRow): void {
    if (!doc.id) {
      return;
    }
    if (doc.storageRef?.startsWith('http')) {
      window.open(doc.storageRef, '_blank');
      return;
    }
    this.api.viewBlob({ id: doc.id }).subscribe({
      next: (blob) => this.openBlob(blob, doc.fileName ?? 'document', true),
      error: () => this.snackBar.open('Unable to open file. Ensure it exists under server uploads folder.', 'OK', { duration: 5000 }),
    });
  }

  download(doc: TravelDocumentRow): void {
    if (!doc.id) {
      return;
    }
    if (doc.storageRef?.startsWith('http')) {
      window.open(doc.storageRef, '_blank');
      return;
    }
    this.api.downloadBlob({ id: doc.id }).subscribe({
      next: (blob) => this.openBlob(blob, doc.fileName ?? 'document', false),
      error: () => this.snackBar.open('Download failed. File may not exist on server yet.', 'OK', { duration: 5000 }),
    });
  }

  private openBlob(blob: Blob, fileName: string, inline: boolean): void {
    const url = URL.createObjectURL(blob);
    if (inline) {
      window.open(url, '_blank');
    } else {
      const a = document.createElement('a');
      a.href = url;
      a.download = fileName;
      a.click();
    }
    setTimeout(() => URL.revokeObjectURL(url), 60_000);
  }

  remove(doc: TravelDocumentRow): void {
    if (!doc.id || !confirm(`Delete document "${doc.fileName || doc.docType}"?`)) {
      return;
    }
    this.api.delete({ id: doc.id }).subscribe({ next: () => this.load(true) });
  }
}
