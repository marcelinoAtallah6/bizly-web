import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { KycComponent } from './kyc.component';
import { CustomersComponent } from './customers/customers.component';

const routes: Routes = [{
  path: '',
  component: CustomersComponent,
  data: { breadcrumb: 'Kyc' },
  children: [
    {
      path: 'customers',
      component: CustomersComponent,
      data: { breadcrumb: 'Customers' },
    },

  ],
},];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class KycRoutingModule { }
