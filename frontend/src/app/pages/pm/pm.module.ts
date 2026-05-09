import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { PmRoutingModule } from './pm-routing.module';
import { CommonMaterialModule } from 'src/app/common/MaterialModule';
import { UicomponentsModule } from '../ui-components/ui-components.module';
import { PmComponent } from './pm.component';
import { ProductListComponent } from './list/product-list/product-list.component';
import { ProductFormComponent } from './form/product-form/product-form.component';
import { ProductDetailsComponent } from './details/product-details/product-details.component';

@NgModule({
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  declarations: [PmComponent, ProductListComponent, ProductFormComponent, ProductDetailsComponent],
  imports: [PmRoutingModule, CommonMaterialModule, UicomponentsModule, CommonModule],
})
export class PmModule {}
