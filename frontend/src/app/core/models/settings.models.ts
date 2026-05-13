/** Settings microservice — dashboards & saved queries */

export interface DashboardSummaryDto {
  id: number;
  name: string;
  slug: string;
  description?: string;
  builtin?: boolean;
}

export interface WidgetDetailDto {
  id: number;
  widgetType: string;
  title: string;
  queryDefId?: number | null;
  queryName?: string | null;
  configJson?: string | null;
  gridX: number;
  gridY: number;
  gridW: number;
  gridH: number;
  refreshSec?: number | null;
  sortOrder: number;
}

export interface DashboardDetailDto {
  id: number;
  name: string;
  slug: string;
  description?: string;
  layoutJson?: string | null;
  builtin?: boolean;
  widgets: WidgetDetailDto[];
  /** Dashboard access grants (when returned from API). */
  grantRoles?: string[];
  grantUsernames?: string[];
}

export interface QueryDefDto {
  id: number;
  name: string;
  description?: string;
  sqlText: string;
  parametersJson?: string | null;
  /** Role names granted access; empty = open to anyone in the tenant. */
  grantRoles?: string[];
  /** Specific usernames granted access; empty = open to anyone in the tenant. */
  grantUsernames?: string[];
}

/** Matches backend DashboardSaveRequest / WidgetDto */
export interface WidgetSaveDto {
  id?: number;
  widgetType: string;
  title: string;
  queryDefId?: number | null;
  configJson?: string | null;
  gridX: number;
  gridY: number;
  gridW: number;
  gridH: number;
  refreshSec?: number | null;
  sortOrder: number;
}

export interface DashboardSaveDto {
  id?: number;
  name: string;
  slug: string;
  description?: string;
  layoutJson?: string | null;
  builtin?: boolean;
  widgets: WidgetSaveDto[];
  grantRoles?: string[];
  grantUsernames?: string[];
}
