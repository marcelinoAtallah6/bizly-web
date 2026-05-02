import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { StoremgmtComponent } from './storemgmt.component';
import { ItemsComponent } from './items/items.component';
import { CategoryComponent } from './category/category.component';

const routes: Routes = [{
  path: '',
  component: StoremgmtComponent,
  data: { breadcrumb: 'Store Management' },
  children: [
        {
          path: 'items',
          component: ItemsComponent,
          data: { breadcrumb: 'Items' },
        },
        {
          path: 'categories',
          component: CategoryComponent,
          data: { breadcrumb: 'Items' },
        },
  ],
},];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class storeMgmtRoutingModule { }
