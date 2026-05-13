import { PageResponse } from './api.types';

/** com.pm.api.dto.add.AddProductRequest */
export interface AddProductRequest {
  name: string;
  price: number;
  productImageMimeType?: string;
  productImageBase64?: string;
  /** Omit or null = unlimited stock at checkout */
  stockQuantity?: number | null;
}

/** com.pm.api.dto.update.UpdateProductRequest */
export interface UpdateProductRequest {
  id: number;
  name: string;
  price: number;
  clearProductImage?: boolean;
  productImageMimeType?: string;
  productImageBase64?: string;
  stockQuantity?: number | null;
}

/** com.pm.api.dto.get.GetProductRequest */
export interface GetProductRequest {
  id: number;
}

/** com.pm.api.dto.delete.DeleteProductRequest */
export interface DeleteProductRequest {
  id: number;
}

/** com.pm.api.dto.gets.GetsProductsRequest */
export interface GetsProductsRequest {
  pageNumber: number;
  pageSize: number;
  /** When true, backend includes Base64 image on each row (larger payload). */
  includeImages?: boolean;
}

/** com.pm.api.dto.get.GetProductResponse */
export interface GetProductResponse {
  id: number;
  name: string;
  price: number;
  stockQuantity?: number | null;
  productImageMimeType?: string;
  productImageBase64?: string;
}

/** com.pm.common.PageResponse<GetProductResponse> */
export type GetsProductsResponse = PageResponse<GetProductResponse>;

/** com.pm.api.dto.add.AddProductResponse */
export interface AddProductResponse {
  id?: number;
}

/** com.pm.api.dto.update.UpdateProductResponse */
export interface UpdateProductResponse {}

/** com.pm.api.dto.delete.DeleteProductResponse */
export interface DeleteProductResponse {}

/** Sale / checkout — exactly one of productId or serviceId */
export interface CheckoutLineRequest {
  productId?: number;
  serviceId?: number;
  quantity: number;
}

export interface CheckoutRequest {
  customerId: number;
  lines: CheckoutLineRequest[];
  /** Snapshot for sale history (customer full name) */
  customerDisplayName?: string;
}

export interface CheckoutResponse {
  saleId: number;
  totalAmount: number;
}

export interface GetSaleRequest {
  id: number;
}

export interface SaleLineResponse {
  lineType?: 'PRODUCT' | 'SERVICE';
  productId?: number;
  serviceId?: number;
  productName?: string;
  quantity: number;
  unitPrice: number;
  lineTotal?: number;
}

export interface GetSaleResponse {
  id: number;
  customerId: number;
  customerDisplayName?: string;
  totalAmount: number;
  status?: string;
  createdAt?: string;
  lines?: SaleLineResponse[];
}

export interface SaleSummaryResponse {
  id: number;
  customerId: number;
  customerDisplayName?: string;
  totalAmount: number;
  status?: string;
  createdAt?: string;
}

export interface GetsSalesRequest {
  pageNumber: number;
  pageSize: number;
  /** When set, returns sales for this customer only */
  customerId?: number;
}

export type GetsSalesResponse = PageResponse<SaleSummaryResponse>;
