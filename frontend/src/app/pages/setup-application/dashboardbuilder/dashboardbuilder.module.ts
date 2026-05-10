import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { DashboardbuilderRoutingModule } from './dashboardbuilder-routing.module';
import { DashboardbuilderComponent } from './dashboardbuilder.component';
import { CommonMaterialModule } from 'src/app/common/MaterialModule';
import { DashboardWidgetsModule } from '../../dashboard/dashboard-widgets.module';
import { UicomponentsModule } from '../../ui-components/ui-components.module';

@NgModule({
  declarations: [DashboardbuilderComponent],
  imports: [
    CommonModule,
    FormsModule,
    CommonMaterialModule,
    DashboardWidgetsModule,
    UicomponentsModule,
    DashboardbuilderRoutingModule,
  ],
})
export class DashboardbuilderModule {}
