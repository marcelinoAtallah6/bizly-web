import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ModuleIndexGuard } from 'src/app/guards/module-index.guard';
import { PermissionGuard } from 'src/app/guards/permission.guard';
import { UmComponent } from './um.component';
import { RoleDetailsComponent } from './details/role-details/role-details.component';
import { UserDetailsComponent } from './details/user-details/user-details.component';
import { RoleFormComponent } from './form/role-form/role-form.component';
import { UserFormComponent } from './form/user-form/user-form.component';
import { RoleListComponent } from './list/role-list/role-list.component';
import { UserListComponent } from './list/user-list/user-list.component';
import { AuditListComponent } from './list/audit-list/audit-list.component';
import { MyProfileComponent } from './form/my-profile/my-profile.component';
import { WorkflowQueueComponent } from './list/workflow-queue/workflow-queue.component';
import { WorkflowEngineListComponent } from './form/workflow-engine/workflow-engine-list.component';
import { WorkflowWorkspaceComponent } from './form/workflow-engine/workflow-workspace.component';
import { ApplicationBuilderComponent } from './form/application-builder/application-builder.component';
import { PortalRootAdminGuard } from 'src/app/guards/portal-root-admin.guard';

const routes: Routes = [
  {
    path: '',
    component: UmComponent,
    data: { breadcrumb: 'UM' },
    children: [
      {
        path: '',
        pathMatch: 'full',
        canActivate: [ModuleIndexGuard],
        data: { moduleIndex: '/um' },
        children: [],
      },
      {
        path: 'my-profile',
        component: MyProfileComponent,
        data: { breadcrumb: 'My profile' },
      },
      {
        path: 'user/new',
        component: UserFormComponent,
        canActivate: [PermissionGuard],
        data: { breadcrumb: 'New user', mode: 'create' },
      },
      {
        path: 'user/:id/edit',
        component: UserFormComponent,
        canActivate: [PermissionGuard],
        data: { breadcrumb: 'Edit user', mode: 'edit' },
      },
      {
        path: 'user/:id',
        component: UserDetailsComponent,
        canActivate: [PermissionGuard],
        data: { breadcrumb: 'User details' },
      },
      {
        path: 'user',
        component: UserListComponent,
        canActivate: [PermissionGuard],
        data: { breadcrumb: 'Users' },
      },
      {
        path: 'role/new',
        component: RoleFormComponent,
        canActivate: [PermissionGuard],
        data: { breadcrumb: 'New role', mode: 'create' },
      },
      {
        path: 'role/:id/edit',
        component: RoleFormComponent,
        canActivate: [PermissionGuard],
        data: { breadcrumb: 'Edit role', mode: 'edit' },
      },
      {
        path: 'role/:id',
        component: RoleDetailsComponent,
        canActivate: [PermissionGuard],
        data: { breadcrumb: 'Role details' },
      },
      {
        path: 'role',
        component: RoleListComponent,
        canActivate: [PermissionGuard],
        data: { breadcrumb: 'Roles' },
      },
      {
        path: 'audit',
        component: AuditListComponent,
        canActivate: [PermissionGuard],
        data: { breadcrumb: 'Audit log' },
      },
      {
        path: 'workflow-queue',
        component: WorkflowQueueComponent,
        canActivate: [PermissionGuard],
        data: { breadcrumb: 'Approval queue', permission: { action: 'view', strict: true } },
      },
      {
        path: 'workflow-engine/new',
        component: WorkflowWorkspaceComponent,
        canActivate: [PermissionGuard],
        data: { breadcrumb: 'New workflow', permission: { action: 'add', strict: true } },
      },
      {
        path: 'workflow-engine/:id/edit',
        component: WorkflowWorkspaceComponent,
        canActivate: [PermissionGuard],
        data: { breadcrumb: 'Edit workflow', permission: { action: 'edit', strict: true } },
      },
      {
        path: 'workflow-engine',
        component: WorkflowEngineListComponent,
        canActivate: [PermissionGuard],
        data: { breadcrumb: 'Workflow engine', permission: { action: 'view', strict: true } },
      },
      {
        path: 'application-builder',
        component: ApplicationBuilderComponent,
        canActivate: [PortalRootAdminGuard],
        data: { breadcrumb: 'Application builder' },
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class UmRoutingModule {}
