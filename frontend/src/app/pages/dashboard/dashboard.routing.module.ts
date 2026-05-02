import { Routes } from '@angular/router';
import { AppDashboardComponent } from './dashboard.component';

export const Dashboardroute: Routes = [
  {
    path: '',
    component: AppDashboardComponent,
    data: { breadcrumb: 'Dashbnoard' },
  },
];
