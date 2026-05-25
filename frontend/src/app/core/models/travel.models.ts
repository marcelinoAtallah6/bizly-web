import { PageResponse } from './api.types';

export interface TravelPageRequest {
  pageNumber: number;
  pageSize: number;
}

export interface TravelClientProfile {
  nationality?: string;
  passportIssueDate?: string;
  passportExpiryDate?: string;
  passportPlaceOfIssue?: string;
  passportType?: string;
  passportScanStorageRef?: string;
  nationalId?: string;
  savedDestinations?: string[];
  preferredAirlines?: string[];
  preferredHotels?: string[];
  loyaltyProgram?: string;
  loyaltyPoints?: number;
  loyaltyStatus?: string;
}

export interface UploadTravelFileResponse {
  storageRef: string;
  fileName: string;
  contentType?: string;
}

export interface TravelClientRow {
  id: number;
  fullName?: string;
  email?: string;
  phone?: string;
  passportNo?: string;
  notes?: string;
  active?: boolean;
  travelProfile?: TravelClientProfile;
}

export type GetsTravelClientsRequest = TravelPageRequest;
export type GetsTravelClientsResponse = PageResponse<TravelClientRow>;

export interface GetTravelClientRequest {
  id: number;
}
export type GetTravelClientResponse = TravelClientRow;

export interface AddTravelClientRequest {
  fullName: string;
  email?: string;
  phone?: string;
  passportNo?: string;
  notes?: string;
  active?: boolean;
  travelProfile?: TravelClientProfile;
}
export interface AddTravelClientResponse {
  id: number;
}

export interface UpdateTravelClientRequest extends AddTravelClientRequest {
  id: number;
}
export interface UpdateTravelClientResponse {}

export interface DeleteTravelClientRequest {
  id: number;
}
export interface DeleteTravelClientResponse {}

export interface TravelItineraryDay {
  dayNumber: number;
  title: string;
  details?: string;
}

export interface TravelPackageMedia {
  fileName: string;
  storageRef?: string;
}

export interface TravelPackageDestination {
  destinationId?: number;
  destinationName?: string;
  nights?: number;
}

export interface TravelPackageAddon {
  code?: string;
  name?: string;
  description?: string;
  price?: number;
}

export interface TravelPackageRow {
  id: number;
  code?: string;
  name?: string;
  description?: string;
  inclusions?: string;
  exclusions?: string;
  itinerary?: TravelItineraryDay[];
  media?: TravelPackageMedia[];
  destinations?: TravelPackageDestination[];
  addons?: TravelPackageAddon[];
  maxCapacityPerDay?: number;
  destination?: string;
  durationDays?: number;
  basePrice?: number;
  currency?: string;
  active?: boolean;
}

export type GetsTravelPackagesRequest = TravelPageRequest;
export type GetsTravelPackagesResponse = PageResponse<TravelPackageRow>;

export interface GetTravelPackageRequest {
  id: number;
}
export type GetTravelPackageResponse = TravelPackageRow;

export interface AddTravelPackageRequest {
  code?: string;
  name: string;
  description?: string;
  inclusions?: string;
  exclusions?: string;
  itinerary?: TravelItineraryDay[];
  media?: TravelPackageMedia[];
  destinations?: TravelPackageDestination[];
  addons?: TravelPackageAddon[];
  maxCapacityPerDay?: number;
  destination?: string;
  durationDays?: number;
  basePrice: number;
  currency?: string;
  active?: boolean;
}
export interface AddTravelPackageResponse {
  id: number;
}

export interface UpdateTravelPackageRequest extends AddTravelPackageRequest {
  id: number;
}
export interface UpdateTravelPackageResponse {}

export interface DeleteTravelPackageRequest {
  id: number;
}
export interface DeleteTravelPackageResponse {}

export interface TravelPaymentScheduleItem {
  dueDate?: string;
  amount?: number;
  label?: string;
  paid?: boolean;
}

export interface TravelBookingRow {
  id: number;
  clientId?: number;
  packageId?: number;
  referenceNo?: string;
  status?: string;
  departureDate?: string;
  returnDate?: string;
  totalAmount?: number;
  currency?: string;
  notes?: string;
  timelineStage?: string;
  approvalStatus?: string;
  paymentSchedule?: TravelPaymentScheduleItem[];
  requiresApproval?: boolean;
}

export type GetsTravelBookingsRequest = TravelPageRequest;
export type GetsTravelBookingsResponse = PageResponse<TravelBookingRow>;

export interface GetTravelBookingRequest {
  id: number;
}
export type GetTravelBookingResponse = TravelBookingRow;

export interface AddTravelBookingRequest {
  clientId: number;
  packageId?: number;
  referenceNo?: string;
  status?: string;
  departureDate?: string;
  returnDate?: string;
  totalAmount?: number;
  currency?: string;
  notes?: string;
  timelineStage?: string;
  approvalStatus?: string;
  paymentSchedule?: TravelPaymentScheduleItem[];
  requiresApproval?: boolean;
}
export interface AddTravelBookingResponse {
  id: number;
}

export interface UpdateTravelBookingRequest extends AddTravelBookingRequest {
  id: number;
}
export interface UpdateTravelBookingResponse {}

export interface DeleteTravelBookingRequest {
  id: number;
}
export interface DeleteTravelBookingResponse {}

export type TravelAvailabilityStatus = 'AVAILABLE' | 'PENDING' | 'BOOKED' | 'UNAVAILABLE';

export interface TravelBookingAvailabilityDay {
  date: string;
  status: TravelAvailabilityStatus;
}

export interface TravelBookingAvailabilityRequest {
  packageId?: number;
  year: number;
  month: number;
  excludeBookingId?: number;
}

export interface TravelBookingAvailabilityResponse {
  days: TravelBookingAvailabilityDay[];
}

// --- Visa ---
export interface TravelVisaRow {
  id: number;
  clientId?: number;
  bookingId?: number;
  country?: string;
  visaType?: string;
  status?: string;
  submittedAt?: string;
  decisionAt?: string;
  notes?: string;
}
export type GetsTravelVisasResponse = PageResponse<TravelVisaRow>;
export interface AddTravelVisaRequest {
  clientId: number;
  bookingId?: number;
  country: string;
  visaType?: string;
  status?: string;
  submittedAt?: string;
  decisionAt?: string;
  notes?: string;
}
export interface UpdateTravelVisaRequest extends AddTravelVisaRequest {
  id: number;
}

// --- Document ---
export interface TravelDocumentRow {
  id: number;
  clientId?: number;
  bookingId?: number;
  docType?: string;
  fileName?: string;
  storageRef?: string;
  uploadedAt?: string;
  uploadedBy?: string;
}
export type GetsTravelDocumentsResponse = PageResponse<TravelDocumentRow>;
export interface AddTravelDocumentRequest {
  clientId?: number;
  bookingId?: number;
  docType: string;
  fileName?: string;
  storageRef?: string;
  uploadedBy?: string;
}
export interface UpdateTravelDocumentRequest extends AddTravelDocumentRequest {
  id: number;
}

// --- Supplier ---
export interface TravelSupplierRow {
  id: number;
  name?: string;
  supplierType?: string;
  contactEmail?: string;
  contactPhone?: string;
  contractsJson?: string;
  commissionNotes?: string;
  rateTableNotes?: string;
  active?: boolean;
}
export type GetsTravelSuppliersResponse = PageResponse<TravelSupplierRow>;
export interface AddTravelSupplierRequest {
  name: string;
  supplierType?: string;
  contactEmail?: string;
  contactPhone?: string;
  contractsJson?: string;
  commissionNotes?: string;
  rateTableNotes?: string;
  active?: boolean;
}
export interface UpdateTravelSupplierRequest extends AddTravelSupplierRequest {
  id: number;
}

// --- Invoice ---
export interface TravelInvoiceRow {
  id: number;
  bookingId?: number;
  invoiceNo?: string;
  amount?: number;
  currency?: string;
  status?: string;
  issuedAt?: string;
  dueAt?: string;
}
export type GetsTravelInvoicesResponse = PageResponse<TravelInvoiceRow>;
export interface AddTravelInvoiceRequest {
  bookingId: number;
  invoiceNo?: string;
  amount: number;
  currency?: string;
  status?: string;
  issuedAt?: string;
  dueAt?: string;
}
export interface UpdateTravelInvoiceRequest extends AddTravelInvoiceRequest {
  id: number;
}

// --- Payment ---
export interface TravelPaymentRow {
  id: number;
  invoiceId?: number;
  amount?: number;
  paymentMethod?: string;
  paidAt?: string;
  referenceNo?: string;
}
export type GetsTravelPaymentsResponse = PageResponse<TravelPaymentRow>;
export interface AddTravelPaymentRequest {
  invoiceId: number;
  amount: number;
  paymentMethod?: string;
  referenceNo?: string;
}
export interface UpdateTravelPaymentRequest extends AddTravelPaymentRequest {
  id: number;
}

// --- Commission rule ---
export interface TravelCommissionRuleRow {
  id: number;
  name?: string;
  ruleType?: string;
  ratePercent?: number;
  flatAmount?: number;
  active?: boolean;
}
export type GetsTravelCommissionRulesResponse = PageResponse<TravelCommissionRuleRow>;
export interface AddTravelCommissionRuleRequest {
  name: string;
  ruleType: string;
  ratePercent?: number;
  flatAmount?: number;
  active?: boolean;
}
export interface UpdateTravelCommissionRuleRequest extends AddTravelCommissionRuleRequest {
  id: number;
}

// --- Follow-up ---
export interface TravelFollowUpRow {
  id: number;
  clientId?: number;
  bookingId?: number;
  subject?: string;
  dueAt?: string;
  status?: string;
  notes?: string;
  assignedTo?: string;
}
export type GetsTravelFollowUpsResponse = PageResponse<TravelFollowUpRow>;
export interface AddTravelFollowUpRequest {
  clientId?: number;
  bookingId?: number;
  subject: string;
  dueAt?: string;
  status?: string;
  notes?: string;
  assignedTo?: string;
}
export interface UpdateTravelFollowUpRequest extends AddTravelFollowUpRequest {
  id: number;
}

// --- Destination ---
export interface TravelDestinationRow {
  id: number;
  name?: string;
  country?: string;
  region?: string;
  visaRequirements?: string;
  healthRequirements?: string;
  highSeasonNotes?: string;
  lowSeasonNotes?: string;
  travelWarnings?: string;
  riskLevel?: string;
  travelAdvisory?: string;
  active?: boolean;
}
export type GetsTravelDestinationsResponse = PageResponse<TravelDestinationRow>;
export interface AddTravelDestinationRequest {
  name: string;
  country?: string;
  region?: string;
  visaRequirements?: string;
  healthRequirements?: string;
  highSeasonNotes?: string;
  lowSeasonNotes?: string;
  travelWarnings?: string;
  riskLevel?: string;
  travelAdvisory?: string;
  active?: boolean;
}
export interface UpdateTravelDestinationRequest extends AddTravelDestinationRequest {
  id: number;
}

// --- Trip request ---
export type TravelTripRequestStatus = 'PENDING' | 'QUOTED' | 'ACCEPTED' | 'REJECTED' | 'CONVERTED';

export interface TravelTripRequestRow {
  id: number;
  clientId?: number;
  destinationId?: number;
  destinationName?: string;
  departureDate?: string;
  returnDate?: string;
  budgetAmount?: number;
  currency?: string;
  travelerCount?: number;
  travelerDetails?: string;
  status?: TravelTripRequestStatus;
  quotedAmount?: number;
  notes?: string;
  assignedTo?: string;
  bookingId?: number;
}
export type GetsTravelTripRequestsResponse = PageResponse<TravelTripRequestRow>;
export interface AddTravelTripRequestRequest {
  clientId: number;
  destinationId?: number;
  destinationName?: string;
  departureDate?: string;
  returnDate?: string;
  budgetAmount?: number;
  currency?: string;
  travelerCount?: number;
  travelerDetails?: string;
  notes?: string;
  assignedTo?: string;
}
export interface UpdateTravelTripRequestRequest extends AddTravelTripRequestRequest {
  id: number;
}
