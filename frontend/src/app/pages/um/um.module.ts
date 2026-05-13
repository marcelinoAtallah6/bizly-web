import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { UmRoutingModule } from './um-routing.module';
import { CommonMaterialModule } from 'src/app/common/MaterialModule';
import { PermissionsModule } from 'src/app/shared/permissions/permissions.module';
import { UicomponentsModule } from '../ui-components/ui-components.module';
import { UmComponent } from './um.component';
import { RoleListComponent } from './list/role-list/role-list.component';
import { UserListComponent } from './list/user-list/user-list.component';
import { RoleFormComponent } from './form/role-form/role-form.component';
import { UserFormComponent } from './form/user-form/user-form.component';
import { RoleDetailsComponent } from './details/role-details/role-details.component';
import { UserDetailsComponent } from './details/user-details/user-details.component';
import { AuditListComponent } from './list/audit-list/audit-list.component';
import { RoleMenuPermissionsTabComponent } from './details/role-menu-permissions-tab/role-menu-permissions-tab.component';

@NgModule({
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  declarations: [
    UmComponent,
    UserListComponent,
    UserFormComponent,
    UserDetailsComponent,
    RoleListComponent,
    RoleFormComponent,
    RoleDetailsComponent,
    AuditListComponent,
    RoleMenuPermissionsTabComponent,
  ],
  imports: [UmRoutingModule, CommonMaterialModule, UicomponentsModule, CommonModule, PermissionsModule],
})
export class UmModule {}
