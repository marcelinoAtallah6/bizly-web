import { HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'  // This makes the service available throughout the app
})
export class GlobalConstants {
  // Base API URL from environment
  private static readonly API_BASE_URL = "http://localhost:8080";

  // Common headers as static readonly objects
  public static readonly JSON_HEADERS = new HttpHeaders({
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  });

  public static readonly FORM_HEADERS = new HttpHeaders({
    'Content-Type': 'application/x-www-form-urlencoded',
    'Accept': 'application/json'
  });

  // API endpoints
  public static readonly API_ENDPOINTS = {
    auth: {
      login: `${GlobalConstants.API_BASE_URL}/auth/login`,
      logout: `${GlobalConstants.API_BASE_URL}/auth/logout`,
      refresh: `${GlobalConstants.API_BASE_URL}/auth/refresh`,
      sessionActiveRole: `${GlobalConstants.API_BASE_URL}/auth/session/active-role`,
      forgotPassword: `${GlobalConstants.API_BASE_URL}/auth/forgot-password`,
      verifyForgotPasswordToken: `${GlobalConstants.API_BASE_URL}/auth/forgot-password/verify`,
      resetForgotPassword: `${GlobalConstants.API_BASE_URL}/auth/forgot-password/reset`
    },
    // Add other endpoint categories as needed
    users: {
      getAll: `${GlobalConstants.API_BASE_URL}/users`,
      getById: (id: string) => `${GlobalConstants.API_BASE_URL}/users/${id}`,
      create: `${GlobalConstants.API_BASE_URL}/users`
    },
    kyc: {
      customer: {
        add: `${GlobalConstants.API_BASE_URL}/kyc/customer/add`,
        update: `${GlobalConstants.API_BASE_URL}/kyc/customer/update`,
        delete: `${GlobalConstants.API_BASE_URL}/kyc/customer/delete`,
        get: `${GlobalConstants.API_BASE_URL}/kyc/customer/get`,
        gets: `${GlobalConstants.API_BASE_URL}/kyc/customer/gets`,
      },
    },
    pm: {
      product: {
        add: `${GlobalConstants.API_BASE_URL}/pm/product/add`,
        update: `${GlobalConstants.API_BASE_URL}/pm/product/update`,
        delete: `${GlobalConstants.API_BASE_URL}/pm/product/delete`,
        get: `${GlobalConstants.API_BASE_URL}/pm/product/get`,
        gets: `${GlobalConstants.API_BASE_URL}/pm/product/gets`,
      },
      sale: {
        checkout: `${GlobalConstants.API_BASE_URL}/pm/sale/checkout`,
        get: `${GlobalConstants.API_BASE_URL}/pm/sale/get`,
        gets: `${GlobalConstants.API_BASE_URL}/pm/sale/gets`,
      },
    },
    um: {
      user: {
        add: `${GlobalConstants.API_BASE_URL}/um/user/add`,
        update: `${GlobalConstants.API_BASE_URL}/um/user/update`,
        delete: `${GlobalConstants.API_BASE_URL}/um/user/delete`,
        get: `${GlobalConstants.API_BASE_URL}/um/user/get`,
        gets: `${GlobalConstants.API_BASE_URL}/um/user/gets`,
      },
      role: {
        add: `${GlobalConstants.API_BASE_URL}/um/role/add`,
        update: `${GlobalConstants.API_BASE_URL}/um/role/update`,
        delete: `${GlobalConstants.API_BASE_URL}/um/role/delete`,
        get: `${GlobalConstants.API_BASE_URL}/um/role/get`,
        gets: `${GlobalConstants.API_BASE_URL}/um/role/gets`,
        menuPermissionsGet: `${GlobalConstants.API_BASE_URL}/um/role/menu-permissions/get`,
        menuPermissionsSave: `${GlobalConstants.API_BASE_URL}/um/role/menu-permissions/save`,
      },
      audit: {
        gets: `${GlobalConstants.API_BASE_URL}/um/audit/gets`,
      },
    },
    application: {
      getAll: `${GlobalConstants.API_BASE_URL}/um/menu/gets`,
    },
    settings: {
      queryDef: {
        gets: `${GlobalConstants.API_BASE_URL}/settings/query-def/gets`,
        get: `${GlobalConstants.API_BASE_URL}/settings/query-def/get`,
        save: `${GlobalConstants.API_BASE_URL}/settings/query-def/save`,
        delete: `${GlobalConstants.API_BASE_URL}/settings/query-def/delete`,
        validate: `${GlobalConstants.API_BASE_URL}/settings/query-def/validate`,
        executeTest: `${GlobalConstants.API_BASE_URL}/settings/query-def/execute-test`,
      },
      dashboardAdmin: {
        definitions: `${GlobalConstants.API_BASE_URL}/settings/dashboard-admin/definitions`,
        save: `${GlobalConstants.API_BASE_URL}/settings/dashboard-admin/save`,
        delete: `${GlobalConstants.API_BASE_URL}/settings/dashboard-admin/delete`,
      },
      dashboardRuntime: {
        forUser: `${GlobalConstants.API_BASE_URL}/settings/dashboard-runtime/for-user`,
        load: `${GlobalConstants.API_BASE_URL}/settings/dashboard-runtime/load`,
        widgetData: `${GlobalConstants.API_BASE_URL}/settings/dashboard-runtime/widget-data`,
        navPref: `${GlobalConstants.API_BASE_URL}/settings/dashboard-runtime/nav-pref`,
      },
    },
  };

  // Helper method to create headers with authentication token
  public static getAuthHeaders(token: string): HttpHeaders {
    return GlobalConstants.JSON_HEADERS.set('Authorization', `Bearer ${token}`);
  }

  // Helper method to create custom headers
  public static createCustomHeaders(headers: { [key: string]: string }): HttpHeaders {
    let httpHeaders = new HttpHeaders();
    Object.entries(headers).forEach(([key, value]) => {
      httpHeaders = httpHeaders.set(key, value);
    });
    return httpHeaders;
  }
}

/** Angular routes aligned with UM_MENUS.route for JWT menu permission matrix. */
export const UM_SCREEN_ROUTES = {
  users: '/um/user',
  roles: '/um/role',
  audit: '/um/audit',
  customers: '/kyc/customers',
  products: '/pm/products',
} as const;