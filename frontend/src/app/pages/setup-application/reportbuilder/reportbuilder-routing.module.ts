import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ReportbuilderComponent } from './reportbuilder.component';

const routes: Routes = [{
  path: '',
  component: ReportbuilderComponent,
  children: [
    
  ],
},];
@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ReportbuilderRoutingModule { }
