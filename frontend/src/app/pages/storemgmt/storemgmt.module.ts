import { NgModule } from '@angular/core';
import { storeMgmtRoutingModule } from './storemgmt-routing.module';
import { StoremgmtComponent } from './storemgmt.component';
import { ItemsdetailsComponent } from './items/itemsdetails/itemsdetails.component';
import { ItemsComponent } from './items/items.component';
import { CommonMaterialModule } from 'src/app/common/MaterialModule';
import { UicomponentsModule } from '../ui-components/ui-components.module';
import { CategorydetailsComponent } from './category/categorydetails/categorydetails.component';
import { CategoryComponent } from './category/category.component';



@NgModule({
  declarations: [StoremgmtComponent, ItemsComponent, ItemsdetailsComponent,CategoryComponent,CategorydetailsComponent],
  imports: [
    storeMgmtRoutingModule,
    CommonMaterialModule,
    UicomponentsModule
  ]
})
export class StoremgmtModule { }
