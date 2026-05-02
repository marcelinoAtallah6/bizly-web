import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { UiComponentsRoutes } from './ui-components.routing';
import { AppBadgeComponent } from './badge/badge.component';
import { AppChipsComponent } from './chips/chips.component';
import { AppListsComponent } from './lists/lists.component';
import { AppMenuComponent } from './menu/menu.component';
import { AppTooltipsComponent } from './tooltips/tooltips.component';
import { AgGridComponent } from './ag-grid/ag-grid.component';
import { DropdownRendererComponent } from './ag-grid/ag-grid-components/dropdown-renderer/dropdown-renderer.component';
import { InputComponent } from './input/input.component';
import { CommonMaterialModule } from 'src/app/common/MaterialModule';
import { ButtonComponent } from './button/button.component';
import { DropdownComponent } from './dropdown/dropdown.component';
import { DateAdapter, NativeDateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE } from '@angular/material/core';
import { DatepickerComponent } from './datepicker/datepicker.component';
import { RadioComponent } from './radio/radio.component';
import { AccordionComponent } from './accordion/accordion.component';
import { CardComponent } from './card/card.component';
import { FieldsetComponent } from './fieldset/fieldset.component';
import { CheckboxComponent } from './checkbox/checkbox.component';
import { ToggleComponent } from './toggle/toggle.component';
import { AutocompleteComponent } from './autocomplete/autocomplete.component';
import { ListComponent } from './list/list.component';
import { ProgressbarComponent } from './progressbar/progressbar.component';
import { SpinnerComponent } from './spinner/spinner.component';
import { AlertComponent } from './alert/alert.component';
import { DialogComponent } from './dialog/dialog.component';
import { MultipleInputComponent } from './input/multiple-input/multiple-input.component';
import { MenuListComponent } from './list/menu-list/menu-list.component';
import { DragDropComponent } from './dragdrop/dragdrop.component';
import { ComponentBuilderComponent } from './componentbuilder/componentbuilder.component';
import { SwitchComponent } from './switch/switch.component';
import { MainScreenComponent } from './Custom/main-screen/main-screen.component';
import { DetailScreenComponent } from './Custom/detail-screen/detail-screen.component';
import { ToolbarComponent } from './button/toolbar/toolbar.component';
import { CardholderComponent } from './cardholder/cardholder.component';
import { ImageUploadComponent } from './image-upload/image-upload.component';


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

const commonArray = [
  ImageUploadComponent,
  CardholderComponent,
  ToolbarComponent,
  MainScreenComponent,
  DetailScreenComponent,
  SwitchComponent,
  ComponentBuilderComponent,
  DragDropComponent,
  ListComponent,
  AppBadgeComponent,
  AppChipsComponent,  
  AppListsComponent,
  AppMenuComponent,
  AppTooltipsComponent,
  AgGridComponent,
  DropdownRendererComponent,
  InputComponent,
  ButtonComponent,
  DropdownComponent,
  DatepickerComponent,
  RadioComponent,
  AccordionComponent,
  CardComponent,
  FieldsetComponent,
  CheckboxComponent,
  ToggleComponent,
  AutocompleteComponent,
  ProgressbarComponent,
  SpinnerComponent,
  AlertComponent,
  DialogComponent,
  MultipleInputComponent,
  MenuListComponent
];



@NgModule({
  imports: [
    RouterModule.forChild(UiComponentsRoutes),
    CommonMaterialModule
  ],
  declarations: [
    commonArray
  ],
  exports: [
    commonArray
  ],
  providers: [
    { provide: DateAdapter, useClass: NativeDateAdapter },//calendar component
    { provide: MAT_DATE_FORMATS, useValue: MY_FORMATS },//calendar component
    { provide: MAT_DATE_LOCALE, useValue: 'en-US' }//calendar component
  ],
})
export class UicomponentsModule { }
