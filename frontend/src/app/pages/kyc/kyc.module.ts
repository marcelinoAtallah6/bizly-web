import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { KycRoutingModule } from './kyc-routing.module';
import { CommonMaterialModule } from 'src/app/common/MaterialModule';
import { UicomponentsModule } from '../ui-components/ui-components.module';
import { KycComponent } from './kyc.component';
import { CustomersComponent } from './customers/customers.component';
import { CustomersdetailsComponent } from './customers/customersdetails/customersdetails.component';


@NgModule({
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  declarations: [KycComponent, CustomersComponent, CustomersdetailsComponent],
  imports: [
    KycRoutingModule,
    CommonMaterialModule,
    UicomponentsModule
  ]
})
export class KycModule { }
