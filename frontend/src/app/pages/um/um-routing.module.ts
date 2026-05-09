import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { UmComponent } from './um.component';
import { RoleDetailsComponent } from './details/role-details/role-details.component';
import { UserDetailsComponent } from './details/user-details/user-details.component';
import { RoleFormComponent } from './form/role-form/role-form.component';
import { UserFormComponent } from './form/user-form/user-form.component';
import { RoleListComponent } from './list/role-list/role-list.component';
import { UserListComponent } from './list/user-list/user-list.component';
import { AuditListComponent } from './list/audit-list/audit-list.component';

const routes: Routes = [
  {
    path: '',
    component: UmComponent,
    data: { breadcrumb: 'UM' },
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'user' },
      {
        path: 'user/new',
        component: UserFormComponent,
        data: { breadcrumb: 'New user', mode: 'create' },
      },
      {
        path: 'user/:id/edit',
        component: UserFormComponent,
        data: { breadcrumb: 'Edit user', mode: 'edit' },
      },
      {
        path: 'user/:id',
        component: UserDetailsComponent,
        data: { breadcrumb: 'User details' },
      },
      {
        path: 'user',
        component: UserListComponent,
        data: { breadcrumb: 'Users' },
      },
      {
        path: 'role/new',
        component: RoleFormComponent,
        data: { breadcrumb: 'New role', mode: 'create' },
      },
      {
        path: 'role/:id/edit',
        component: RoleFormComponent,
        data: { breadcrumb: 'Edit role', mode: 'edit' },
      },
      {
        path: 'role/:id',
        component: RoleDetailsComponent,
        data: { breadcrumb: 'Role details' },
      },
      {
        path: 'role',
        component: RoleListComponent,
        data: { breadcrumb: 'Roles' },
      },
      {
        path: 'audit',
        component: AuditListComponent,
        data: { breadcrumb: 'Audit log' },
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class UmRoutingModule {}
