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
      resetForgotPassword: `${GlobalConstants.API_BASE_URL}/auth/forgot-password/reset`,
      me: `${GlobalConstants.API_BASE_URL}/auth/me`,
      /** Lazy-fetched avatar bytes for the navbar when the JWT can't embed the image. */
      meAvatar: `${GlobalConstants.API_BASE_URL}/auth/me/avatar`,
      /** Public self-service sign-up: creates user + business + role + session in one shot. */
      register: `${GlobalConstants.API_BASE_URL}/auth/register`,
      /** Legacy: completes the business step for a user that already has an account. */
      registerBusiness: `${GlobalConstants.API_BASE_URL}/auth/register-business`,
      welcomeComplete: `${GlobalConstants.API_BASE_URL}/auth/welcome-complete`,
      assignableRoles: `${GlobalConstants.API_BASE_URL}/auth/roles/assignable`,
      social: (provider: 'google' | 'facebook' | 'apple') =>
        `${GlobalConstants.API_BASE_URL}/auth/social/${provider}`,
      /** SUPER_ADMIN context switcher — must run as role-level ADMIN. */
      adminSearchBusinesses: `${GlobalConstants.API_BASE_URL}/auth/admin/businesses/search`,
      adminSearchUsers: `${GlobalConstants.API_BASE_URL}/auth/admin/users/search`
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
    /** Gateway: Path=/bm/** StripPrefix=1 → BM controllers. Unified product + service checkout
     *  has moved to {@code pm.sale.*} ({@code /pm/sale/*}); BM keeps service items, appointments,
     *  notifications, and the header-pulse KPIs. */
    bm: {
      serviceItem: {
        add: `${GlobalConstants.API_BASE_URL}/bm/service-item/add`,
        update: `${GlobalConstants.API_BASE_URL}/bm/service-item/update`,
        deactivate: `${GlobalConstants.API_BASE_URL}/bm/service-item/deactivate`,
        get: `${GlobalConstants.API_BASE_URL}/bm/service-item/get`,
        gets: `${GlobalConstants.API_BASE_URL}/bm/service-item/gets`,
      },
      appointment: {
        add: `${GlobalConstants.API_BASE_URL}/bm/appointments/add`,
        update: `${GlobalConstants.API_BASE_URL}/bm/appointments/update`,
        cancel: `${GlobalConstants.API_BASE_URL}/bm/appointments/cancel`,
        get: `${GlobalConstants.API_BASE_URL}/bm/appointments/get`,
        gets: `${GlobalConstants.API_BASE_URL}/bm/appointments/gets`,
        range: `${GlobalConstants.API_BASE_URL}/bm/appointments/range`,
        calendar: `${GlobalConstants.API_BASE_URL}/bm/appointments/calendar`,
      },
      /** Per-user in-app notifications (recipient resolved from JWT/X-User). */
      notifInbox: {
        recent: `${GlobalConstants.API_BASE_URL}/bm/notif-inbox/recent`,
        unreadCount: `${GlobalConstants.API_BASE_URL}/bm/notif-inbox/unread-count`,
        markRead: `${GlobalConstants.API_BASE_URL}/bm/notif-inbox/mark-read`,
        markAllRead: `${GlobalConstants.API_BASE_URL}/bm/notif-inbox/mark-all-read`,
        /** Diagnostic-only: publishes one test row to the calling user. */
        seedTest: `${GlobalConstants.API_BASE_URL}/bm/notif-inbox/seed-test`,
      },
      /** Small KPI tile data for the top bar ticker. */
      headerPulse: {
        get: `${GlobalConstants.API_BASE_URL}/bm/header-pulse/get`,
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
    /** Broadcast microservice (gateway path prefix `/broadcast`). */
    broadcast: {
      message: {
        create: `${GlobalConstants.API_BASE_URL}/broadcast/message/create`,
        preview: `${GlobalConstants.API_BASE_URL}/broadcast/message/preview`,
        send: `${GlobalConstants.API_BASE_URL}/broadcast/message/send`,
        get: `${GlobalConstants.API_BASE_URL}/broadcast/message/get`,
        gets: `${GlobalConstants.API_BASE_URL}/broadcast/message/gets`,
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
        lastDashboardGet: `${GlobalConstants.API_BASE_URL}/settings/dashboard-runtime/last-dashboard/get`,
        lastDashboardSet: `${GlobalConstants.API_BASE_URL}/settings/dashboard-runtime/last-dashboard/set`,
      },
      /** Reporting engine (runner) + Report Builder admin. */
      reporting: {
        getTypes: `${GlobalConstants.API_BASE_URL}/settings/reporting/getTypes`,
        generate: `${GlobalConstants.API_BASE_URL}/settings/reporting/generate`,
        export: `${GlobalConstants.API_BASE_URL}/settings/reporting/export`,
        builder: {
          list: `${GlobalConstants.API_BASE_URL}/settings/reporting/builder/list`,
          get: `${GlobalConstants.API_BASE_URL}/settings/reporting/builder/get`,
          save: `${GlobalConstants.API_BASE_URL}/settings/reporting/builder/save`,
          delete: `${GlobalConstants.API_BASE_URL}/settings/reporting/builder/delete`,
          listActive: `${GlobalConstants.API_BASE_URL}/settings/reporting/builder/listActive`,
        },
      },
      /** Starred menus + the single one marked as the post-login default. */
      userFavorites: {
        list: `${GlobalConstants.API_BASE_URL}/settings/user-favorites/list`,
        add: `${GlobalConstants.API_BASE_URL}/settings/user-favorites/add`,
        remove: `${GlobalConstants.API_BASE_URL}/settings/user-favorites/remove`,
        setDefault: `${GlobalConstants.API_BASE_URL}/settings/user-favorites/set-default`,
        reorder: `${GlobalConstants.API_BASE_URL}/settings/user-favorites/reorder`,
        /** Lightweight: just the default route or null, used by login redirect. */
        defaultRoute: `${GlobalConstants.API_BASE_URL}/settings/user-favorites/default-route/get`,
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