import { PageResponse } from './api.types';

/** com.um.api.dto.user.gets.GetsUsersRequest */
export interface GetsUsersRequest {
  pageNumber: number;
  pageSize: number;
}

/** com.um.api.dto.user.get.GetUserRequest */
export interface GetUserRequest {
  id: number;
}

/** com.um.api.dto.user.add.AddUserRequest */
export interface AddUserRequest {
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  mobileNumber: string;
  password: string;
  roleIds: number[];
  status: string;
  profileImageMimeType?: string;
  profileImageBase64?: string;
}

/** com.um.api.dto.user.update.UpdateUserRequest */
export interface UpdateUserRequest {
  id: number;
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  mobileNumber: string;
  status: string;
  roleIds: number[];
  clearProfileImage?: boolean;
  profileImageMimeType?: string;
  profileImageBase64?: string;
}

/** com.um.api.dto.user.delete.DeleteUserRequest */
export interface DeleteUserRequest {
  id: number;
}

/** com.um.api.dto.user.get.GetUserResponse */
export interface GetUserResponse {
  id: number;
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  mobileNumber: string;
  status: string;
  createdAt?: string;
  roleIds?: number[];
  profileImageMimeType?: string;
  profileImageBase64?: string;
}

/** com.um.common.PageResponse<GetUserResponse> */
export type GetsUsersResponse = PageResponse<GetUserResponse>;

/** com.um.api.dto.user.add.AddUserResponse */
export interface AddUserResponse {
  id: number;
}

/** com.um.api.dto.user.update.UpdateUserResponse */
export interface UpdateUserResponse {
  id: number;
}

/** com.um.api.dto.user.delete.DeleteUserResponse */
export interface DeleteUserResponse {
  id: number;
}

/** com.um.api.dto.role.gets.GetsRolesRequest */
export interface GetsRolesRequest {
  pageNumber: number;
  pageSize: number;
}

/** com.um.api.dto.role.add.AddRoleRequest */
export interface AddRoleRequest {
  name: string;
  roleType: number;
}

/** com.um.api.dto.role.update.UpdateRoleRequest */
export interface UpdateRoleRequest {
  id: number;
  name: string;
  roleType: number;
}

/** com.um.api.dto.role.delete.DeleteRoleRequest */
export interface DeleteRoleRequest {
  id: number;
}

/** com.um.api.dto.role.get.GetRoleRequest */
export interface GetRoleRequest {
  id: number;
}

/** com.um.api.dto.role.get.GetRoleResponse */
export interface GetRoleResponse {
  id: number;
  name: string;
  roleType?: number;
  createdAt?: string;
}

/** com.um.common.PageResponse<GetRoleResponse> */
export type GetsRolesResponse = PageResponse<GetRoleResponse>;

/** com.um.api.dto.role.add.AddRoleResponse */
export interface AddRoleResponse {
  id: number;
}

/** com.um.api.dto.role.update.UpdateRoleResponse */
export interface UpdateRoleResponse {
  id: number;
}

/** com.um.api.dto.role.delete.DeleteRoleResponse */
export interface DeleteRoleResponse {
  id: number;
}

/** com.um.api.dto.audit.AuditLogRowResponse */
export interface AuditLogRowResponse {
  id: number;
  username?: string;
  actionCode?: string;
  resourceType?: string;
  resourceId?: string;
  oldValues?: string;
  newValues?: string;
  httpMethod?: string;
  requestPath?: string;
  ipAddress?: string;
  sessionId?: string;
  createdAt?: string;
}

/** com.um.common.PageResponse<AuditLogRowResponse> */
export type GetsAuditLogsResponse = PageResponse<AuditLogRowResponse>;

/** com.um.api.dto.role.permission.GetRoleMenuPermissionsRequest */
export interface GetRoleMenuPermissionsRequest {
  roleId: number;
}

/** com.um.api.dto.role.permission.RoleMenuPermissionRowResponse */
export interface RoleMenuPermissionRowResponse {
  menuId: number;
  /** Null / omitted for top-level menus under an application. */
  parentMenuId?: number | null;
  applicationId?: number;
  applicationName?: string;
  menuPath?: string;
  route?: string;
  allowView: boolean;
  allowAdd: boolean;
  allowEdit: boolean;
  allowDelete: boolean;
}

/** com.um.api.dto.role.permission.RoleMenuPermissionEntryDto */
export interface RoleMenuPermissionEntryDto {
  menuId: number;
  allowView: boolean;
  allowAdd: boolean;
  allowEdit: boolean;
  allowDelete: boolean;
}

/** com.um.api.dto.role.permission.SaveRoleMenuPermissionsRequest */
export interface SaveRoleMenuPermissionsRequest {
  roleId: number;
  permissions: RoleMenuPermissionEntryDto[];
}
