import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ApibuilderComponent } from './apibuilder.component';

const routes: Routes = [{
  path: '',
  component: ApibuilderComponent,
  data: { breadcrumb: 'API Builder' },
  children: [
    
  ],
},];
@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ApibuilderRoutingModule { }
