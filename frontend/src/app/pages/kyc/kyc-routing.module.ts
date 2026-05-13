import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ModuleIndexGuard } from 'src/app/guards/module-index.guard';
import { PermissionGuard } from 'src/app/guards/permission.guard';
import { KycComponent } from './kyc.component';
import { CustomerDetailsComponent } from './details/customer-details/customer-details.component';
import { CustomerFormComponent } from './form/customer-form/customer-form.component';
import { CustomerListComponent } from './list/customer-list/customer-list.component';

const routes: Routes = [
  {
    path: '',
    component: KycComponent,
    data: { breadcrumb: 'KYC' },
    children: [
      {
        path: '',
        pathMatch: 'full',
        canActivate: [ModuleIndexGuard],
        data: { moduleIndex: '/kyc' },
        children: [],
      },
      {
        path: 'customers',
        component: CustomerListComponent,
        canActivate: [PermissionGuard],
        data: { breadcrumb: 'Customers' },
      },
      {
        path: 'customers/new',
        component: CustomerFormComponent,
        canActivate: [PermissionGuard],
        data: { breadcrumb: 'New customer', mode: 'create' },
      },
      {
        path: 'customers/:id/edit',
        component: CustomerFormComponent,
        canActivate: [PermissionGuard],
        data: { breadcrumb: 'Edit customer', mode: 'edit' },
      },
      {
        path: 'customers/:id',
        component: CustomerDetailsComponent,
        canActivate: [PermissionGuard],
        data: { breadcrumb: 'Customer details' },
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class KycRoutingModule {}
