import { Component, OnInit } from '@angular/core';
import { FormControl } from '@angular/forms';
import { Router } from '@angular/router';
import { debounceTime, distinctUntilChanged, finalize } from 'rxjs/operators';

import { ActiveReportRef } from 'src/app/pages/setup-application/reporting/reporting.types';
import { ReportingService } from 'src/app/pages/setup-application/reporting/reporting.service';

/**
 * Read-only catalog: list, run, filter, export only (no create/update/delete).
 * Data is scoped by admin grants on each report (roles + usernames).
 */
@Component({
  selector: 'app-reports-list',
  templateUrl: './reports-list.component.html',
  styleUrls: ['./reports-list.component.scss'],
})
export class ReportsListComponent implements OnInit {
  reports: ActiveReportRef[] = [];
  filtered: ActiveReportRef[] = [];
  loading = false;
  nameSearchCtrl = new FormControl('', { nonNullable: true });

  constructor(
    private readonly reporting: ReportingService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.load();
    this.nameSearchCtrl.valueChanges
      .pipe(debounceTime(250), distinctUntilChanged())
      .subscribe(() => this.applyFilter());
  }

  load(): void {
    this.loading = true;
    this.reporting
      .listAssignedReports()
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (items) => {
          this.reports = items ?? [];
          this.applyFilter();
        },
        error: () => {
          this.reports = [];
          this.filtered = [];
        },
      });
  }

  applyFilter(): void {
    const q = this.nameSearchCtrl.value.trim().toLowerCase();
    if (!q) {
      this.filtered = [...this.reports];
      return;
    }
    this.filtered = this.reports.filter((r) => {
      const hay = [r.name, r.description, r.code].filter(Boolean).join(' ').toLowerCase();
      return hay.includes(q);
    });
  }

  openReport(r: ActiveReportRef): void {
    if (r.id == null) {
      return;
    }
    this.router.navigate(['/reports', 'run', r.id]);
  }

  trackByReportId(_: number, r: ActiveReportRef): number {
    return r.id;
  }
}
