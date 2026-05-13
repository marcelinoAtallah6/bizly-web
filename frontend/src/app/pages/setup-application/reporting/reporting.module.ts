import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { CommonMaterialModule } from 'src/app/common/MaterialModule';
import { ReportBuilderComponent } from './report-builder/report-builder.component';
import { ReportRunComponent } from './report-run/report-run.component';
import { ReportingRoutingModule } from './reporting-routing.module';

@NgModule({
  declarations: [ReportBuilderComponent, ReportRunComponent],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    CommonMaterialModule,
    ReportingRoutingModule,
  ],
})
export class ReportingModule {}
