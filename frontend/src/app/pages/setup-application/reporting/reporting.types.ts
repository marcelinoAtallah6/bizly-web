/**
 * Mirrors `com.settings.api.dto.reporting.*` 1:1. Keep this file as a thin
 * type declaration — no runtime values — so backend changes are easy to follow.
 */

export type ReportFilterType =
  | 'DATE'
  | 'DATE_RANGE'
  | 'TEXT'
  | 'SELECT'
  | 'USER'
  | 'NUMBER';

export type ReportColumnType =
  | 'STRING'
  | 'NUMBER'
  | 'MONEY'
  | 'DATE'
  | 'DATETIME'
  | 'BOOLEAN';

export interface ReportFilterOption {
  value: string;
  label: string;
}

export interface ReportFilterDef {
  key: string;
  label: string;
  type: ReportFilterType;
  placeholder?: string;
  options?: ReportFilterOption[];
}

export interface ReportColumnDef {
  key: string;
  label: string;
  type: ReportColumnType;
  sortable: boolean;
}

export interface ReportTypeMeta {
  key: string;
  name: string;
  description?: string;
  category?: string;
  icon?: string;
  filters: ReportFilterDef[];
  columns: ReportColumnDef[];
  defaultSortKey?: string;
  defaultSortDir?: 'ASC' | 'DESC';
}

export interface ReportTypesResponse {
  items: ReportTypeMeta[];
}

export interface ReportDateRangeValue {
  from?: string | null;
  to?: string | null;
}

export type ReportFilterValue =
  | string
  | number
  | null
  | ReportDateRangeValue;

export interface ReportRequest {
  typeKey: string;
  filters: Record<string, ReportFilterValue>;
  pageNumber: number;
  pageSize: number;
  sortBy?: string;
  sortDir?: 'ASC' | 'DESC';
  maxRows?: number;
}

export interface ReportPageResponse {
  typeKey: string;
  columns: ReportColumnDef[];
  items: Array<Record<string, unknown>>;
  totalCount: number;
  pageNumber: number;
  pageSize: number;
  totalPages: number;
  generatedAt: string;
}

// ----- Report Builder (admin) -----

/**
 * Per-filter binding spec persisted in the report definition. Mirrors the
 * backend `ReportFilterConfig`. Each filter is mapped to one (or two, for
 * date ranges) named placeholders in the linked query's SQL.
 */
export interface ReportFilterConfig {
  key: string;
  label: string;
  type: ReportFilterType;
  placeholder?: string | null;
  paramName?: string | null;
  fromParamName?: string | null;
  toParamName?: string | null;
  options?: ReportFilterOption[] | null;
}

export interface ReportColumnConfig {
  key: string;
  label?: string | null;
  type: ReportColumnType;
  sortable: boolean;
  visible: boolean;
}

export type ReportStatus = 'ACTIVE' | 'INACTIVE';

export interface ReportBuilderItem {
  id?: number;
  code?: string;
  name: string;
  description?: string | null;
  icon?: string | null;
  status: ReportStatus;
  queryDefId: number;
  queryDefName?: string | null;
  filters?: ReportFilterConfig[];
  columns?: ReportColumnConfig[];
  defaultSortKey?: string | null;
  defaultSortDir?: 'ASC' | 'DESC' | null;
  sortOrder?: number;
  createdAt?: string | null;
  updatedAt?: string | null;
  createdBy?: string | null;
  updatedBy?: string | null;
  /** Role names granted access (empty = open to anyone in the tenant). */
  grantRoles?: string[];
  /** Usernames granted access (empty = open to anyone in the tenant). */
  grantUsernames?: string[];
}

export interface ReportBuilderSaveRequest {
  id?: number;
  code?: string | null;
  name: string;
  description?: string | null;
  icon?: string | null;
  status?: ReportStatus;
  queryDefId: number;
  filters?: ReportFilterConfig[];
  columns?: ReportColumnConfig[];
  defaultSortKey?: string | null;
  defaultSortDir?: 'ASC' | 'DESC' | null;
  sortOrder?: number;
  autoDetectColumns?: boolean;
  grantRoles?: string[];
  grantUsernames?: string[];
}

export interface ActiveReportRef {
  id: number;
  code: string;
  name: string;
  description?: string | null;
  icon?: string | null;
  sortOrder?: number;
  route: string;
}
