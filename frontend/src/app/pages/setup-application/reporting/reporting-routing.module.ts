import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { PermissionGuard } from 'src/app/guards/permission.guard';
import { ReportBuilderComponent } from './report-builder/report-builder.component';
import { ReportRunComponent } from './report-run/report-run.component';
import { ReportingSharedModule } from './reporting-shared.module';

const routes: Routes = [
  {
    path: '',
    component: ReportBuilderComponent,
    canActivate: [PermissionGuard],
    data: {
      breadcrumb: 'Report Builder',
      permission: { action: 'view', strict: true },
    },
  },
  {
    path: 'run/:id',
    component: ReportRunComponent,
    canActivate: [PermissionGuard],
    data: {
      breadcrumb: 'Run Report',
      permission: { action: 'view', strict: true },
    },
  },
];

@NgModule({
  imports: [ReportingSharedModule, RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class ReportingRoutingModule {}
