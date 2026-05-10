import { NgModule } from '@angular/core';
import { DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE, NativeDateAdapter } from '@angular/material/core';
import { CommonMaterialModule } from 'src/app/common/MaterialModule';
import { AgGridComponent } from './ag-grid/ag-grid.component';
import { ButtonComponent } from './button/button.component';
import { ToolbarComponent } from './button/toolbar/toolbar.component';
import { CardComponent } from './card/card.component';
import { DetailScreenComponent } from './Custom/detail-screen/detail-screen.component';
import { ImagePreviewDialogComponent } from './Custom/image-preview/image-preview.component';
import { MainScreenComponent } from './Custom/main-screen/main-screen.component';
import { DatepickerComponent } from './datepicker/datepicker.component';
import { DialogComponent } from './dialog/dialog.component';
import { DropdownComponent } from './dropdown/dropdown.component';
import { FieldsetComponent } from './fieldset/fieldset.component';
import { ImageUploadComponent } from './image-upload/image-upload.component';
import { InputComponent } from './input/input.component';
import { PageActionBarComponent } from './page-action-bar/page-action-bar.component';
import { RadioComponent } from './radio/radio.component';
import { SwitchComponent } from './switch/switch.component';

export const MY_FORMATS = {
  parse: {
    dateInput: 'LL',
  },
  display: {
    dateInput: 'YYYY-MM-DD',
    monthYearLabel: 'YYYY',
    dateA11yLabel: 'LL',
    monthYearA11yLabel: 'YYYY',
  },
};

const declarationsExports = [
  ImagePreviewDialogComponent,
  PageActionBarComponent,
  ToolbarComponent,
  MainScreenComponent,
  DetailScreenComponent,
  SwitchComponent,
  ImageUploadComponent,
  AgGridComponent,
  InputComponent,
  ButtonComponent,
  DropdownComponent,
  DatepickerComponent,
  RadioComponent,
  CardComponent,
  FieldsetComponent,
  DialogComponent,
];

@NgModule({
  imports: [CommonMaterialModule],
  declarations: [declarationsExports],
  exports: [declarationsExports],
  providers: [
    { provide: DateAdapter, useClass: NativeDateAdapter },
    { provide: MAT_DATE_FORMATS, useValue: MY_FORMATS },
    { provide: MAT_DATE_LOCALE, useValue: 'en-US' },
  ],
})
export class UicomponentsModule {}
