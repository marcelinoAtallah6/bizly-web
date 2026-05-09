/** Mirrors backend ApiResponse<T> */
export interface ApiEnvelope<T> {
  success: boolean;
  message: string;
  data: T;
}

export interface PageResponse<T> {
  items: T[];
  totalCount: number;
  pageNumber: number;
  pageSize: number;
  totalPages: number;
}
