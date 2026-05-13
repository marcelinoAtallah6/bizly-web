import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { BlankComponent } from './layouts/blank/blank.component';
import { FullComponent } from './layouts/full/full.component';
import { AuthGuard } from './guards/auth.guard';
import { GuestGuard } from './guards/guest.guard';
import { OnboardingGuard } from './guards/onboarding.guard';

const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'authentication/login',
  },
  {
    path: '',
    component: FullComponent,
    /*
     * Auth + onboarding gating: AuthGuard checks "is the user logged in?" first; OnboardingGuard
     * then checks "did they finish welcome + business registration?" and redirects to those
     * screens when not. The order matters — AuthGuard must be first.
     */
    canActivate: [AuthGuard, OnboardingGuard],
    canActivateChild: [AuthGuard, OnboardingGuard],
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'dashboard',
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
        redirectTo: 'pm/products',
        pathMatch: 'full',
      },
      {
        path: 'pm',
        loadChildren: () =>
          import('./pages/pm/pm.module').then((m) => m.PmModule),
      },
      {
        path: 'bm',
        loadChildren: () =>
          import('./pages/bm/bm.module').then((m) => m.BmModule),
      },
      {
        path: 'users',
        redirectTo: '/um/user',
        pathMatch: 'full',
      },
      {
        path: 'um',
        loadChildren: () =>
          import('./pages/um/um.module').then((m) => m.UmModule),
      },
      {
        path: 'broadcast',
        loadChildren: () =>
          import('./pages/broadcast/broadcast.module').then((m) => m.BroadcastModule),
      },
      {
        path: 'apt',
        redirectTo: 'bm/appointments',
        pathMatch: 'full',
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
        path: 'um-builder',
        loadChildren: () =>
          import('./pages/setup-application/usermanagement/usermanagement.module').then(
            (m) => m.UsermanagementModule
          ),
      },
      {
        // Dynamic reporting screen — filters, paginated grid, CSV/Excel/PDF export.
        // Backed by /settings/reporting/* (provider-based engine).
        path: 'reporting',
        loadChildren: () =>
          import('./pages/setup-application/reporting/reporting.module').then(
            (m) => m.ReportingModule
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
    canActivate: [GuestGuard],
    canActivateChild: [GuestGuard],
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
