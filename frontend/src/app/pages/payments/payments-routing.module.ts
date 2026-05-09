import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { PaymentCheckoutComponent } from './payment-checkout/payment-checkout.component';
import { PaymentHistoryComponent } from './payment-history/payment-history.component';
import { PaymentsComponent } from './payments.component';

const routes: Routes = [
  {
    path: '',
    component: PaymentsComponent,
    data: { breadcrumb: 'Payments' },
    children: [
      { path: '', redirectTo: 'checkout', pathMatch: 'full' },
      {
        path: 'checkout',
        component: PaymentCheckoutComponent,
        data: { breadcrumb: 'Checkout' },
      },
      {
        path: 'history',
        component: PaymentHistoryComponent,
        data: { breadcrumb: 'History' },
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class PaymentsRoutingModule {}
