import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { OverlayModule } from '@angular/cdk/overlay';
import { CommonMaterialModule } from '../../common/MaterialModule';
import { DynamicDashboardViewComponent } from './dynamic-dashboard-view/dynamic-dashboard-view.component';
import { DashboardCalendarWidgetComponent } from './widgets/dashboard-calendar-widget/dashboard-calendar-widget.component';
import { DashboardCardListWidgetComponent } from './widgets/dashboard-card-list-widget/dashboard-card-list-widget.component';

/** Shared shell for embedding the runtime dashboard grid (builder preview + `/dashboard`). */
@NgModule({
  declarations: [
    DynamicDashboardViewComponent,
    DashboardCalendarWidgetComponent,
    DashboardCardListWidgetComponent,
  ],
  imports: [CommonMaterialModule, RouterModule, OverlayModule],
  exports: [DynamicDashboardViewComponent],
})
export class DashboardWidgetsModule {}
