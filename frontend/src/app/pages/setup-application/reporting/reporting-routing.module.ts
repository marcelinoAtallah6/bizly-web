import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ReportBuilderComponent } from './report-builder/report-builder.component';
import { ReportRunComponent } from './report-run/report-run.component';

const routes: Routes = [
  {
    path: '',
    component: ReportBuilderComponent,
    data: { breadcrumb: 'Report Builder' },
  },
  {
    path: 'run/:id',
    component: ReportRunComponent,
    data: { breadcrumb: 'Run Report' },
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class ReportingRoutingModule {}
