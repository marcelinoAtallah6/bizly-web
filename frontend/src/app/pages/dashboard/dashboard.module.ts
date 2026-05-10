import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { AppDashboardComponent } from './dashboard.component';
import { CommonMaterialModule } from '../../common/MaterialModule';
import { UicomponentsModule } from '../ui-components/ui-components.module';
import { DashboardWidgetsModule } from './dashboard-widgets.module';
import { Dashboardroute } from './dashboard.routing.module';

@NgModule({
  declarations: [AppDashboardComponent],
  imports: [
    RouterModule.forChild(Dashboardroute),
    CommonMaterialModule,
    UicomponentsModule,
    DashboardWidgetsModule,
  ],
  exports:[
    // UicomponentsModule,
    // ExtraModule,
    CommonMaterialModule
  ]
})
export class DashboardModule {}



// import { NgModule } from '@angular/core';
// import { RouterModule } from '@angular/router';
// import { CommonModule } from '@angular/common';
// import { PagesRoutes } from './pages.routing.module';
// import { MaterialModule } from '../material.module';
// import { FormsModule } from '@angular/forms';
// import { NgApexchartsModule } from 'ng-apexcharts';
// // icons
// import { TablerIconsModule } from 'angular-tabler-icons';
// import * as TablerIcons from 'angular-tabler-icons/icons';
// import { AppDashboardComponent } from './dashboard/dashboard.component';

// @NgModule({
//   declarations: [AppDashboardComponent],
//   imports: [
//     CommonModule,
//     MaterialModule,
//     FormsModule,
//     NgApexchartsModule,
//     RouterModule.forChild(PagesRoutes),
//     TablerIconsModule.pick(TablerIcons),
//   ],
//   exports: [TablerIconsModule],
// })
// export class PagesModule {}
