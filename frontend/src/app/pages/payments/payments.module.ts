import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

import { PaymentsRoutingModule } from './payments-routing.module';
import { CommonMaterialModule } from 'src/app/common/MaterialModule';
import { UicomponentsModule } from '../ui-components/ui-components.module';
import { PaymentCheckoutComponent } from './payment-checkout/payment-checkout.component';
import { PaymentHistoryComponent } from './payment-history/payment-history.component';
import { PaymentsComponent } from './payments.component';
import { SaleDetailDialogModule } from 'src/app/shared/sale-detail-dialog/sale-detail-dialog.module';

@NgModule({
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  declarations: [PaymentsComponent, PaymentCheckoutComponent, PaymentHistoryComponent],
  imports: [
    CommonModule,
    RouterModule,
    CommonMaterialModule,
    UicomponentsModule,
    SaleDetailDialogModule,
    PaymentsRoutingModule,
  ],
})
export class PaymentsModule {}
