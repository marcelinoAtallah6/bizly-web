import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatNativeDateModule } from '@angular/material/core';
import { AgGridModule } from 'ag-grid-angular';
import { TablerIconsModule } from 'angular-tabler-icons';
import { MaterialModule } from '../material.module';
import { MatTabsModule } from '@angular/material/tabs';
import * as TablerIcons from 'angular-tabler-icons/icons';
import { NgApexchartsModule } from 'ng-apexcharts';
import {CdkDrag} from '@angular/cdk/drag-drop';
import { DragDropModule } from '@angular/cdk/drag-drop';

const MaterialArray = [
  CommonModule,
  MaterialModule,
  FormsModule,
  ReactiveFormsModule,
  MatNativeDateModule,
  AgGridModule,
  MatTabsModule,
  NgApexchartsModule,
  CdkDrag,
  DragDropModule
];

@NgModule({
  imports: [
    MaterialArray,
    TablerIconsModule.pick(TablerIcons),

  ],
  exports: [
    MaterialArray,
    TablerIconsModule
  ]
})
export class CommonMaterialModule { }
