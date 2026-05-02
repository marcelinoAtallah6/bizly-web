import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { BlankComponent } from './layouts/blank/blank.component';
import { FullComponent } from './layouts/full/full.component';

const routes: Routes = [
  {
    path: '',
    component: FullComponent,
    children: [
      {
        path: '',
        redirectTo: '/dashboard',
        pathMatch: 'full',
      },
      {
        path: 'dashboard',
        loadChildren: () =>
          import('./pages/dashboard/dashboard.module').then(
            (m) => m.DashboardModule
          ),
      },
      {
        path: 'kyc',
        loadChildren: () =>
          import('./pages/kyc/kyc.module').then(
            (m) => m.KycModule
          ),
      },
      {
        path: 'store',
        loadChildren: () =>
          import('./pages/storemgmt/storemgmt.module').then(
            (m) => m.StoremgmtModule
          ),
      },
      {
        path: 'apt',
        loadChildren: () =>
          import('./pages/appointment/appointment.module').then(
            (m) => m.AppointmentModule
          ),
      },
      {
        path: 'pay',
        loadChildren: () =>
          import('./pages/payments/payments.module').then(
            (m) => m.PaymentsModule
          ),
      },
      {
        path: 'conf',
        loadChildren: () =>
          import('./pages/globalconfiguration/globalconfiguration.module').then(
            (m) => m.GlobalconfigurationModule
          ),
      },
      {
        path: 'qbe',
        loadChildren: () =>
          import('./pages/setup-application/querybuilder/querybuilder.module').then(
            (m) => m.QuerybuilderModule
          ),
      },
      {
        path: 'rpt',
        loadChildren: () =>
          import('./pages/setup-application/reportbuilder/reportbuilder.module').then(
            (m) => m.ReportbuilderModule
          ),
      },
      {
        path: 'api',
        loadChildren: () =>
          import('./pages/setup-application/apibuilder/apibuilder.module').then(
            (m) => m.ApibuilderModule
          ),
      },
      {
        path: 'dash',
        loadChildren: () =>
          import('./pages/setup-application/dashboardbuilder/dashboardbuilder.module').then(
            (m) => m.DashboardbuilderModule
          ),
      },
      {
        path: 'um',
        loadChildren: () =>
          import('./pages/setup-application/usermanagement/usermanagement.module').then(
            (m) => m.UsermanagementModule
          ),
      },
      {
        path: 'ui-components',
        loadChildren: () =>
          import('./pages/ui-components/ui-components.module').then(
            (m) => m.UicomponentsModule
          ),
      },
      {
        path: 'extra',
        loadChildren: () =>
          import('./pages/extra/extra.module').then((m) => m.ExtraModule),
      },
    ],
  },
  {
    path: '',
    component: BlankComponent,
    children: [
      {
        path: 'authentication',
        loadChildren: () =>
          import('./pages/authentication/authentication.module').then(
            (m) => m.AuthenticationModule
          ),
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
