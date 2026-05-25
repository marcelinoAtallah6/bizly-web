import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { CommonMaterialModule } from 'src/app/common/MaterialModule';
import { ReportBuilderComponent } from './report-builder/report-builder.component';
import { ReportingRoutingModule } from './reporting-routing.module';
import { ReportingSharedModule } from './reporting-shared.module';

@NgModule({
  declarations: [ReportBuilderComponent],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    CommonMaterialModule,
    ReportingSharedModule,
    ReportingRoutingModule,
  ],
})
export class ReportingModule {}
