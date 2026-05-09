import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
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
      { path: '', pathMatch: 'full', redirectTo: 'customers' },
      {
        path: 'customers',
        component: CustomerListComponent,
        data: { breadcrumb: 'Customers' },
      },
      {
        path: 'customers/new',
        component: CustomerFormComponent,
        data: { breadcrumb: 'New customer', mode: 'create' },
      },
      {
        path: 'customers/:id/edit',
        component: CustomerFormComponent,
        data: { breadcrumb: 'Edit customer', mode: 'edit' },
      },
      {
        path: 'customers/:id',
        component: CustomerDetailsComponent,
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
