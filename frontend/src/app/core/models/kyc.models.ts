import { PageResponse } from './api.types';

/** com.kyc.api.dto.add.AddCustomerRequest — dob as yyyy-MM-dd JSON string */
export interface AddCustomerRequest {
  firstName: string;
  lastName: string;
  dob: string;
  email: string;
  mobileNumber: string;
  addressLine1?: string;
  addressLine2?: string;
  city?: string;
  stateProvince?: string;
  postalCode?: string;
  country?: string;
  customerStatus: string;
}

/** com.kyc.api.dto.update.UpdateCustomerRequest */
export interface UpdateCustomerRequest extends AddCustomerRequest {
  id: number;
}

/** com.kyc.api.dto.get.GetCustomerRequest */
export interface GetCustomerRequest {
  id: number;
}

/** com.kyc.api.dto.delete.DeleteCustomerRequest */
export interface DeleteCustomerRequest {
  id: number;
}

/** com.kyc.api.dto.gets.GetsCustomersRequest */
export interface GetsCustomersRequest {
  pageNumber: number;
  pageSize: number;
}

/** com.kyc.api.dto.get.GetCustomerResponse */
export interface GetCustomerResponse {
  id: number;
  firstName?: string;
  lastName?: string;
  fullName?: string;
  dob?: string;
  email?: string;
  mobileNumber?: string;
  addressLine1?: string;
  addressLine2?: string;
  city?: string;
  stateProvince?: string;
  postalCode?: string;
  country?: string;
  customerStatus?: string;
  createdAt?: string;
}

/** com.kyc.common.PageResponse<GetCustomerResponse> */
export type GetsCustomersResponse = PageResponse<GetCustomerResponse>;

/** com.kyc.api.dto.add.AddCustomerResponse */
export interface AddCustomerResponse {
  id: number;
}

/** com.kyc.api.dto.update.UpdateCustomerResponse */
export interface UpdateCustomerResponse {
  id: number;
}

/** com.kyc.api.dto.delete.DeleteCustomerResponse */
export interface DeleteCustomerResponse {
  id: number;
}
