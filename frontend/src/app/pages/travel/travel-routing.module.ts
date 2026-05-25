import { NgModule } from '@angular/core';

import { RouterModule, Routes } from '@angular/router';

import { ModuleIndexGuard } from 'src/app/guards/module-index.guard';

import { PermissionGuard } from 'src/app/guards/permission.guard';

import { TravelClientDetailsComponent } from './details/client-details/travel-client-details.component';

import { TravelClientFormComponent } from './form/client-form/travel-client-form.component';

import { TravelFinanceComponent } from './finance/travel-finance.component';

import { TravelBookingListComponent } from './list/booking-list/travel-booking-list.component';

import { TravelClientListComponent } from './list/client-list/travel-client-list.component';

import { TravelCommissionListComponent } from './list/commission-list/travel-commission-list.component';

import { TravelDocumentListComponent } from './list/document-list/travel-document-list.component';

import { TravelFollowUpListComponent } from './list/follow-up-list/travel-follow-up-list.component';

import { TravelPackageListComponent } from './list/package-list/travel-package-list.component';

import { TravelSupplierListComponent } from './list/supplier-list/travel-supplier-list.component';

import { TravelVisaListComponent } from './list/visa-list/travel-visa-list.component';

import { TravelDestinationListComponent } from './list/destination-list/travel-destination-list.component';

import { TravelComponent } from './travel.component';



const routes: Routes = [

  {

    path: '',

    component: TravelComponent,

    data: { breadcrumb: 'Travel' },

    children: [

      {

        path: '',

        pathMatch: 'full',

        canActivate: [ModuleIndexGuard],

        data: { moduleIndex: '/travel', moduleIndexDefault: 'clients' },

        children: [],

      },

      {

        path: 'clients',

        component: TravelClientListComponent,

        canActivate: [PermissionGuard],

        data: { breadcrumb: 'Clients' },

      },

      {

        path: 'clients/new',

        component: TravelClientFormComponent,

        canActivate: [PermissionGuard],

        data: { breadcrumb: 'New client', mode: 'create' },

      },

      {

        path: 'clients/:id/edit',

        component: TravelClientFormComponent,

        canActivate: [PermissionGuard],

        data: { breadcrumb: 'Edit client', mode: 'edit' },

      },

      {

        path: 'clients/:id',

        component: TravelClientDetailsComponent,

        canActivate: [PermissionGuard],

        data: { breadcrumb: 'Client profile' },

      },

      {

        path: 'bookings',

        component: TravelBookingListComponent,

        canActivate: [PermissionGuard],

        data: { breadcrumb: 'Bookings' },

      },

      {

        path: 'packages',

        component: TravelPackageListComponent,

        canActivate: [PermissionGuard],

        data: { breadcrumb: 'Tour packages' },

      },

      {

        path: 'visas',

        component: TravelVisaListComponent,

        canActivate: [PermissionGuard],

        data: { breadcrumb: 'Visa management' },

      },

      {

        path: 'documents',

        component: TravelDocumentListComponent,

        canActivate: [PermissionGuard],

        data: { breadcrumb: 'Documents' },

      },

      {

        path: 'suppliers',

        component: TravelSupplierListComponent,

        canActivate: [PermissionGuard],

        data: { breadcrumb: 'Suppliers' },

      },

      {

        path: 'finance',

        component: TravelFinanceComponent,

        canActivate: [PermissionGuard],

        data: { breadcrumb: 'Finance' },

      },

      {

        path: 'commissions',

        component: TravelCommissionListComponent,

        canActivate: [PermissionGuard],

        data: { breadcrumb: 'Commissions' },

      },

      {

        path: 'follow-ups',

        component: TravelFollowUpListComponent,

        canActivate: [PermissionGuard],

        data: { breadcrumb: 'Follow-ups' },

      },

      {

        path: 'destinations',

        component: TravelDestinationListComponent,

        canActivate: [PermissionGuard],

        data: { breadcrumb: 'Destinations' },

      },

      {
        path: 'trip-requests',
        redirectTo: 'bookings',
        pathMatch: 'full',
      },

    ],

  },

];



@NgModule({

  imports: [RouterModule.forChild(routes)],

  exports: [RouterModule],

})

export class TravelRoutingModule {}

