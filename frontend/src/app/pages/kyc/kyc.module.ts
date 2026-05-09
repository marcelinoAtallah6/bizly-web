import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { KycRoutingModule } from './kyc-routing.module';
import { CommonMaterialModule } from 'src/app/common/MaterialModule';
import { SaleDetailDialogModule } from 'src/app/shared/sale-detail-dialog/sale-detail-dialog.module';
import { UicomponentsModule } from '../ui-components/ui-components.module';
import { KycComponent } from './kyc.component';
import { CustomerListComponent } from './list/customer-list/customer-list.component';
import { CustomerFormComponent } from './form/customer-form/customer-form.component';
import { CustomerDetailsComponent } from './details/customer-details/customer-details.component';

@NgModule({
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  declarations: [
    KycComponent,
    CustomerListComponent,
    CustomerFormComponent,
    CustomerDetailsComponent,
  ],
  imports: [
    KycRoutingModule,
    CommonMaterialModule,
    UicomponentsModule,
    SaleDetailDialogModule,
    CommonModule,
  ],
})
export class KycModule {}
