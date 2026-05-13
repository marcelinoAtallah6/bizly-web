import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { CommonMaterialModule } from 'src/app/common/MaterialModule';
import { PermissionsModule } from 'src/app/shared/permissions/permissions.module';
import { UicomponentsModule } from '../ui-components/ui-components.module';
import { BroadcastRoutingModule } from './broadcast-routing.module';
import { BroadcastComponent } from './broadcast.component';
import { BroadcastListComponent } from './list/broadcast-list/broadcast-list.component';
import { BroadcastComposeComponent } from './form/broadcast-compose/broadcast-compose.component';
import { CKEditorModule } from 'ckeditor4-angular';

@NgModule({
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  declarations: [BroadcastComponent, BroadcastListComponent, BroadcastComposeComponent],
  imports: [BroadcastRoutingModule, CommonMaterialModule, UicomponentsModule, CKEditorModule, CommonModule, PermissionsModule],
})
export class BroadcastModule {}

