import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ModuleIndexGuard } from 'src/app/guards/module-index.guard';
import { PermissionGuard } from 'src/app/guards/permission.guard';
import { BmComponent } from './bm.component';
import { AppointmentManagementComponent } from './list/appointment-management/appointment-management.component';
import { ServiceListComponent } from './list/service-list/service-list.component';

const routes: Routes = [
  {
    path: '',
    component: BmComponent,
    data: { breadcrumb: 'Booking management' },
    children: [
      {
        path: '',
        pathMatch: 'full',
        canActivate: [ModuleIndexGuard],
        data: { moduleIndex: '/bm' },
        children: [],
      },
      {
        path: 'services',
        component: ServiceListComponent,
        canActivate: [PermissionGuard],
        data: { breadcrumb: 'Services' },
      },
      {
        path: 'appointments',
        component: AppointmentManagementComponent,
        canActivate: [PermissionGuard],
        data: { breadcrumb: 'Appointments' },
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class BmRoutingModule {}
