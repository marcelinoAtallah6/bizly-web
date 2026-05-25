import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ColDef } from 'ag-grid-community';
import { finalize, forkJoin } from 'rxjs';
import {
  TravelBookingRow,
  TravelClientRow,
  TravelDocumentRow,
  TravelFollowUpRow,
  TravelVisaRow,
} from 'src/app/core/models/travel.models';
import { ToolbarButton } from 'src/app/pages/ui-components/button/toolbar/toolbar.component';
import { MenuPermissionService } from 'src/app/services/menu-permission.service';
import { gridListPaginationMixin } from 'src/app/shared/ag-grid/ag-grid-list-pagination.mixin';
import { TravelKpiItem } from '../../shared/components/kpi-strip/travel-kpi-strip.component';
import { TravelPassportPreviewData } from '../../shared/components/passport-preview/travel-passport-preview.component';
import { TravelLookupService } from '../../shared/services/travel-lookup.service';
import { TravelBookingService } from '../../services/travel-booking.service';
import { TravelClientService } from '../../services/travel-client.service';
import { TravelDocumentService } from '../../services/travel-document.service';
import { TravelFollowUpService } from '../../services/travel-follow-up.service';
import { TravelVisaService } from '../../services/travel-visa.service';

@Component({
  selector: 'app-travel-client-details',
  templateUrl: './travel-client-details.component.html',
  styleUrl: './travel-client-details.component.scss',
})
export class TravelClientDetailsComponent implements OnInit, OnDestroy {
  client: TravelClientRow | null = null;
  bookings: TravelBookingRow[] = [];
  visas: TravelVisaRow[] = [];
  documents: TravelDocumentRow[] = [];
  followUps: TravelFollowUpRow[] = [];
  loading = true;
  kpis: TravelKpiItem[] = [];
  docKpis: TravelKpiItem[] = [];
  passportPreview: TravelPassportPreviewData | null = null;
  passportPhotoUrl: string | null = null;
  readonly route = '/travel/clients';
  readonly gridPagination = gridListPaginationMixin;

  bookingCols: ColDef[] = [
    { field: 'referenceNo', headerName: 'Reference', flex: 1 },
    { field: 'status', headerName: 'Status', width: 110 },
    { field: 'departureDate', headerName: 'Departure', width: 120 },
    {
      field: 'totalAmount',
      headerName: 'Total',
      width: 100,
      valueFormatter: (p) => (p.value != null ? Number(p.value).toFixed(2) : ''),
    },
  ];

  visaCols: ColDef[] = [
    { field: 'country', headerName: 'Country', flex: 1 },
    { field: 'visaType', headerName: 'Type', width: 100 },
    { field: 'status', headerName: 'Status', width: 110 },
  ];

  followUpCols: ColDef[] = [
    { field: 'subject', headerName: 'Subject', flex: 1.2 },
    { field: 'status', headerName: 'Status', width: 100 },
    { field: 'dueAt', headerName: 'Due', width: 140 },
  ];

  defaultColDef: ColDef = { resizable: true };

  constructor(
    private readonly routeAct: ActivatedRoute,
    private readonly router: Router,
    private readonly api: TravelClientService,
    private readonly bookingApi: TravelBookingService,
    private readonly visaApi: TravelVisaService,
    private readonly documentApi: TravelDocumentService,
    private readonly followUpApi: TravelFollowUpService,
    readonly lookup: TravelLookupService,
    private readonly menuPerm: MenuPermissionService,
    private readonly snackBar: MatSnackBar
  ) {}

  get toolbar(): ToolbarButton[] {
    return [
      { id: 'back', icon: 'arrow_back', tooltip: 'Back', action: () => this.router.navigate(['/travel/clients']) },
      {
        id: 'edit',
        icon: 'edit',
        tooltip: 'Edit',
        action: () => this.router.navigate(['/travel/clients', this.client?.id, 'edit']),
        hidden: !this.menuPerm.can(this.route, 'edit') || this.client?.id == null,
      },
      {
        id: 'delete',
        icon: 'delete',
        tooltip: 'Delete',
        action: () => this.remove(),
        hidden: !this.menuPerm.can(this.route, 'delete') || this.client?.id == null,
      },
    ];
  }

  ngOnInit(): void {
    const id = Number(this.routeAct.snapshot.paramMap.get('id'));
    if (!Number.isFinite(id)) {
      this.loading = false;
      return;
    }
    forkJoin({
      client: this.api.get({ id }),
      catalog: this.lookup.ensureAllForGrids(),
      bookings: this.bookingApi.gets({ pageNumber: 0, pageSize: 500 }),
      visas: this.visaApi.gets({ pageNumber: 0, pageSize: 500 }),
      documents: this.documentApi.gets({ pageNumber: 0, pageSize: 500 }),
      followUps: this.followUpApi.gets({ pageNumber: 0, pageSize: 500 }),
    })
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (r) => {
          this.client = r.client;
          const cid = r.client.id;
          this.bookings = (r.bookings.items ?? []).filter((b) => b.clientId === cid);
          this.visas = (r.visas.items ?? []).filter((v) => v.clientId === cid);
          this.documents = (r.documents.items ?? []).filter((d) => d.clientId === cid);
          this.followUps = (r.followUps.items ?? []).filter((f) => f.clientId === cid);
          this.buildPassportPreview(r.client);
          this.refreshKpis(r.client.travelProfile);
          this.refreshDocKpis();
        },
      });
  }

  ngOnDestroy(): void {
    if (this.passportPhotoUrl?.startsWith('blob:')) {
      URL.revokeObjectURL(this.passportPhotoUrl);
    }
  }

  private buildPassportPreview(c: TravelClientRow): void {
    const p = c.travelProfile;
    this.passportPreview = {
      fullName: c.fullName,
      nationality: p?.nationality,
      passportNo: c.passportNo,
      passportIssueDate: p?.passportIssueDate,
      passportExpiryDate: p?.passportExpiryDate,
      passportPlaceOfIssue: p?.passportPlaceOfIssue,
      passportType: p?.passportType,
      nationalId: p?.nationalId,
      countryCode: p?.nationality,
    };
    const ref = p?.passportScanStorageRef;
    if (ref && !ref.startsWith('http')) {
      this.documentApi.viewBlobByRef(ref).subscribe({
        next: (blob) => {
          if (blob.type.startsWith('image/')) {
            this.passportPhotoUrl = URL.createObjectURL(blob);
          }
        },
      });
    }
  }

  private refreshKpis(p?: TravelClientRow['travelProfile']): void {
    this.kpis = [
      { label: 'Bookings', value: this.bookings.length, icon: 'flight', tone: 'bookings' },
      { label: 'Visa apps', value: this.visas.length, icon: 'card_travel', tone: 'packages' },
      { label: 'Documents', value: this.documents.length, icon: 'folder', tone: 'clients' },
      {
        label: 'Loyalty',
        value: p?.loyaltyStatus || p?.loyaltyPoints || '—',
        icon: 'stars',
        tone: 'open',
        hint: p?.loyaltyProgram,
      },
    ];
  }

  private refreshDocKpis(): void {
    const countType = (needle: string) =>
      this.documents.filter((d) => (d.docType ?? '').toUpperCase().includes(needle)).length;
    this.docKpis = [
      { label: 'Total', value: this.documents.length, icon: 'folder', tone: 'clients' },
      { label: 'Passports', value: countType('PASSPORT'), icon: 'badge', tone: 'bookings' },
      { label: 'Visa', value: countType('VISA'), icon: 'card_travel', tone: 'packages' },
      { label: 'Tickets', value: countType('TICKET'), icon: 'flight', tone: 'open' },
      { label: 'Other', value: countType('OTHER'), icon: 'more_horiz', tone: 'default' },
    ];
  }

  viewDocument(doc: TravelDocumentRow): void {
    if (!doc.id) {
      return;
    }
    if (doc.storageRef?.startsWith('http')) {
      window.open(doc.storageRef, '_blank');
      return;
    }
    this.documentApi.viewBlob({ id: doc.id }).subscribe({
      next: (blob) => this.openBlob(blob, doc.fileName ?? 'document', true),
      error: () =>
        this.snackBar.open('Unable to open file.', 'OK', { duration: 4000 }),
    });
  }

  downloadDocument(doc: TravelDocumentRow): void {
    if (!doc.id) {
      return;
    }
    if (doc.storageRef?.startsWith('http')) {
      window.open(doc.storageRef, '_blank');
      return;
    }
    this.documentApi.downloadBlob({ id: doc.id }).subscribe({
      next: (blob) => this.openBlob(blob, doc.fileName ?? 'document', false),
      error: () =>
        this.snackBar.open('Download failed.', 'OK', { duration: 4000 }),
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

  remove(): void {
    if (this.client?.id == null || !confirm('Delete this client?')) {
      return;
    }
    this.api.delete({ id: this.client.id }).subscribe({
      next: () => {
        this.snackBar.open('Client deleted.', 'Dismiss', { duration: 3000 });
        this.router.navigate(['/travel/clients']);
      },
    });
  }
}
