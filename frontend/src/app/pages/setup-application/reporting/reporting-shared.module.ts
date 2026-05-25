import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { CommonMaterialModule } from 'src/app/common/MaterialModule';
import { ReportRunComponent } from './report-run/report-run.component';

/**
 * Runner UI only — no Report Builder routes. Import this from {@link ReportsModule}
 * so {@link ReportingRoutingModule} (builder default route) is not merged into /reports.
 */
@NgModule({
  declarations: [ReportRunComponent],
  imports: [CommonModule, FormsModule, ReactiveFormsModule, CommonMaterialModule],
  exports: [ReportRunComponent],
})
export class ReportingSharedModule {}
