import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';

import { CommonMaterialModule } from 'src/app/common/MaterialModule';
import { ReportingSharedModule } from '../setup-application/reporting/reporting-shared.module';
import { ReportsListComponent } from './reports-list/reports-list.component';
import { ReportsRoutingModule } from './reports-routing.module';

@NgModule({
  declarations: [ReportsListComponent],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    CommonMaterialModule,
    ReportingSharedModule,
    ReportsRoutingModule,
  ],
})
export class ReportsModule {}
