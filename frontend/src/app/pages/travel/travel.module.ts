import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';

import { CommonModule } from '@angular/common';

import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { CommonMaterialModule } from 'src/app/common/MaterialModule';

import { PermissionsModule } from 'src/app/shared/permissions/permissions.module';

import { UicomponentsModule } from '../ui-components/ui-components.module';

import { TravelBookingFormDialogComponent } from './dialogs/booking-form-dialog/travel-booking-form-dialog.component';

import { TravelCommissionFormDialogComponent } from './dialogs/commission-form-dialog/travel-commission-form-dialog.component';

import { TravelDocumentFormDialogComponent } from './dialogs/document-form-dialog/travel-document-form-dialog.component';

import { TravelFollowUpFormDialogComponent } from './dialogs/follow-up-form-dialog/travel-follow-up-form-dialog.component';

import { TravelInvoiceFormDialogComponent } from './dialogs/invoice-form-dialog/travel-invoice-form-dialog.component';

import { TravelPackageFormDialogComponent } from './dialogs/package-form-dialog/travel-package-form-dialog.component';

import { TravelPaymentFormDialogComponent } from './dialogs/payment-form-dialog/travel-payment-form-dialog.component';

import { TravelSupplierFormDialogComponent } from './dialogs/supplier-form-dialog/travel-supplier-form-dialog.component';

import { TravelVisaFormDialogComponent } from './dialogs/visa-form-dialog/travel-visa-form-dialog.component';

import { TravelDestinationFormDialogComponent } from './dialogs/destination-form-dialog/travel-destination-form-dialog.component';

import { TravelClientDetailsComponent } from './details/client-details/travel-client-details.component';

import { TravelClientFormComponent } from './form/client-form/travel-client-form.component';

import { TravelFinanceComponent } from './finance/travel-finance.component';

import { TravelBookingListComponent } from './list/booking-list/travel-booking-list.component';

import { TravelClientListComponent } from './list/client-list/travel-client-list.component';

import { TravelCommissionListComponent } from './list/commission-list/travel-commission-list.component';

import { TravelDocumentListComponent } from './list/document-list/travel-document-list.component';

import { TravelFollowUpListComponent } from './list/follow-up-list/travel-follow-up-list.component';

import { TravelInvoiceListComponent } from './list/invoice-list/travel-invoice-list.component';

import { TravelPackageListComponent } from './list/package-list/travel-package-list.component';

import { TravelPaymentListComponent } from './list/payment-list/travel-payment-list.component';

import { TravelSupplierListComponent } from './list/supplier-list/travel-supplier-list.component';

import { TravelVisaListComponent } from './list/visa-list/travel-visa-list.component';

import { TravelDestinationListComponent } from './list/destination-list/travel-destination-list.component';

import { TravelBookingAvailabilityCalendarComponent } from './shared/components/booking-availability-calendar/travel-booking-availability-calendar.component';
import { TravelDocumentGridCardComponent } from './shared/components/document-grid-card/travel-document-grid-card.component';
import { TravelDocumentRowCardComponent } from './shared/components/document-row-card/travel-document-row-card.component';
import { TravelEntityPickerComponent } from './shared/components/entity-picker/travel-entity-picker.component';
import { TravelFileUploadComponent } from './shared/components/file-upload/travel-file-upload.component';
import { TravelPassportPreviewComponent } from './shared/components/passport-preview/travel-passport-preview.component';
import { TravelKpiStripComponent } from './shared/components/kpi-strip/travel-kpi-strip.component';
import { TravelPaginatedCountrySelectComponent } from './shared/components/paginated-country-select/travel-paginated-country-select.component';

import { TravelComponent } from './travel.component';

import { TravelRoutingModule } from './travel-routing.module';



@NgModule({

  schemas: [CUSTOM_ELEMENTS_SCHEMA],

  declarations: [

    TravelComponent,

    TravelClientListComponent,

    TravelClientFormComponent,

    TravelClientDetailsComponent,

    TravelPackageListComponent,

    TravelPackageFormDialogComponent,

    TravelBookingListComponent,

    TravelBookingFormDialogComponent,

    TravelVisaListComponent,

    TravelVisaFormDialogComponent,

    TravelDocumentListComponent,

    TravelDocumentFormDialogComponent,

    TravelSupplierListComponent,

    TravelSupplierFormDialogComponent,

    TravelFinanceComponent,

    TravelInvoiceListComponent,

    TravelInvoiceFormDialogComponent,

    TravelPaymentListComponent,

    TravelPaymentFormDialogComponent,

    TravelCommissionListComponent,

    TravelCommissionFormDialogComponent,

    TravelFollowUpListComponent,

    TravelFollowUpFormDialogComponent,

    TravelDestinationListComponent,

    TravelDestinationFormDialogComponent,

    TravelEntityPickerComponent,

    TravelBookingAvailabilityCalendarComponent,

    TravelKpiStripComponent,

    TravelDocumentRowCardComponent,

    TravelDocumentGridCardComponent,

    TravelPaginatedCountrySelectComponent,

    TravelFileUploadComponent,
    TravelPassportPreviewComponent,

  ],

  imports: [

    CommonModule,

    FormsModule,

    ReactiveFormsModule,

    TravelRoutingModule,

    CommonMaterialModule,

    UicomponentsModule,

    PermissionsModule,

  ],

})

export class TravelModule {}

