import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { UsermanagementComponent } from './usermanagement.component';

const routes: Routes = [{
  path: '',
  component: UsermanagementComponent,
  data: { breadcrumb: 'User Mamagement' },
  children: [
    
  ],
},];
@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class UsermanagementRoutingModule { }
