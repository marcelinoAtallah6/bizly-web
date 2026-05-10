import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonMaterialModule } from '../../common/MaterialModule';
import { DynamicDashboardViewComponent } from './dynamic-dashboard-view/dynamic-dashboard-view.component';

/** Shared shell for embedding the runtime dashboard grid (builder preview + `/dashboard`). */
@NgModule({
  declarations: [DynamicDashboardViewComponent],
  imports: [CommonMaterialModule, RouterModule],
  exports: [DynamicDashboardViewComponent],
})
export class DashboardWidgetsModule {}
