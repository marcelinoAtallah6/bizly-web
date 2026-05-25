import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { NoAccessComponent } from './no-access.component';

const routes: Routes = [
  {
    path: '',
    component: NoAccessComponent,
    data: { breadcrumb: 'No access' },
  },
];

@NgModule({
  declarations: [NoAccessComponent],
  imports: [CommonModule, MatIconModule, RouterModule.forChild(routes)],
})
export class NoAccessModule {}
