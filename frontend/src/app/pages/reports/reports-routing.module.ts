import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { PermissionGuard } from 'src/app/guards/permission.guard';
import { ReportRunComponent } from '../setup-application/reporting/report-run/report-run.component';
import { ReportingSharedModule } from '../setup-application/reporting/reporting-shared.module';
import { ReportsListComponent } from './reports-list/reports-list.component';

const routes: Routes = [
  {
    path: '',
    component: ReportsListComponent,
    canActivate: [PermissionGuard],
    data: { breadcrumb: 'Reports', permission: { action: 'view', strict: true } },
  },
  {
    path: 'run/:id',
    component: ReportRunComponent,
    canActivate: [PermissionGuard],
    data: {
      breadcrumb: 'Run report',
      viewerMode: true,
      permission: { action: 'view', strict: true },
    },
  },
];

@NgModule({
  imports: [ReportingSharedModule, RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class ReportsRoutingModule {}
