import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { BmRoutingModule } from './bm-routing.module';
import { CommonMaterialModule } from 'src/app/common/MaterialModule';
import { PermissionsModule } from 'src/app/shared/permissions/permissions.module';
import { UicomponentsModule } from '../ui-components/ui-components.module';
import { BmComponent } from './bm.component';
import { AppointmentFormDialogComponent } from './dialogs/appointment-form-dialog/appointment-form-dialog.component';
import { ServiceFormDialogComponent } from './dialogs/service-form-dialog/service-form-dialog.component';
import { ServiceListComponent } from './list/service-list/service-list.component';
import { AppointmentManagementComponent } from './list/appointment-management/appointment-management.component';

@NgModule({
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  declarations: [
    BmComponent,
    ServiceListComponent,
    AppointmentManagementComponent,
    ServiceFormDialogComponent,
    AppointmentFormDialogComponent,
  ],
  imports: [BmRoutingModule, CommonMaterialModule, UicomponentsModule, CommonModule, PermissionsModule],
})
export class BmModule {}
