import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ModuleIndexGuard } from 'src/app/guards/module-index.guard';
import { PermissionGuard } from 'src/app/guards/permission.guard';
import { BroadcastComponent } from './broadcast.component';
import { BroadcastListComponent } from './list/broadcast-list/broadcast-list.component';
import { BroadcastComposeComponent } from './form/broadcast-compose/broadcast-compose.component';

const routes: Routes = [
  {
    path: '',
    component: BroadcastComponent,
    data: { breadcrumb: 'Broadcast' },
    children: [
      {
        path: '',
        pathMatch: 'full',
        canActivate: [ModuleIndexGuard],
        data: { moduleIndex: '/broadcast' },
        children: [],
      },
      {
        path: 'messages',
        component: BroadcastListComponent,
        canActivate: [PermissionGuard],
        data: { breadcrumb: 'Broadcast messages' },
      },
      {
        path: 'compose',
        component: BroadcastComposeComponent,
        canActivate: [PermissionGuard],
        data: { breadcrumb: 'New broadcast', permission: 'add' },
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class BroadcastRoutingModule {}
