import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DashboardbuilderComponent } from './dashboardbuilder.component';

const routes: Routes = [{
  path: '',
  component: DashboardbuilderComponent,
  data: { breadcrumb: 'Dashbnoard Builder' },
  children: [
    
  ],
},];
@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DashboardbuilderRoutingModule { }
