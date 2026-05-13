import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { PermissionGuard } from 'src/app/guards/permission.guard';
import { PmComponent } from './pm.component';
import { ProductDetailsComponent } from './details/product-details/product-details.component';
import { ProductFormComponent } from './form/product-form/product-form.component';
import { ProductListComponent } from './list/product-list/product-list.component';

const routes: Routes = [
  {
    path: '',
    component: PmComponent,
    data: { breadcrumb: 'Product management' },
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'products' },
      {
        path: 'products',
        component: ProductListComponent,
        canActivate: [PermissionGuard],
        data: { breadcrumb: 'Products' },
      },
      {
        path: 'products/add',
        component: ProductFormComponent,
        canActivate: [PermissionGuard],
        data: { breadcrumb: 'New product', mode: 'create' },
      },
      {
        path: 'products/:id/edit',
        component: ProductFormComponent,
        canActivate: [PermissionGuard],
        data: { breadcrumb: 'Edit product', mode: 'edit' },
      },
      {
        path: 'products/:id',
        component: ProductDetailsComponent,
        canActivate: [PermissionGuard],
        data: { breadcrumb: 'Product details' },
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class PmRoutingModule {}
