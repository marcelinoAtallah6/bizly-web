import { HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'  // This makes the service available throughout the app
})
export class GlobalConstants {
  private static readonly API_BASE_URL = environment.apiBaseUrl;

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
      platformConfig: `${GlobalConstants.API_BASE_URL}/auth/platform-config`,
      verifyEmailSetPassword: `${GlobalConstants.API_BASE_URL}/auth/verify-email/set-password`,
      adminBusinessOnboard: `${GlobalConstants.API_BASE_URL}/auth/admin/business/onboard`,
      assignableRoles: `${GlobalConstants.API_BASE_URL}/auth/roles/assignable`,
      /** Public catalogue of business types — each is a role flagged is_business_type=1. */
      businessTypes: `${GlobalConstants.API_BASE_URL}/auth/business-types`,
      social: (provider: 'google') =>
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
    /** Gateway: Path=/travel/** StripPrefix=1 → Travel microservice */
    travel: {
      client: {
        add: `${GlobalConstants.API_BASE_URL}/travel/client/add`,
        update: `${GlobalConstants.API_BASE_URL}/travel/client/update`,
        delete: `${GlobalConstants.API_BASE_URL}/travel/client/delete`,
        get: `${GlobalConstants.API_BASE_URL}/travel/client/get`,
        gets: `${GlobalConstants.API_BASE_URL}/travel/client/gets`,
      },
      package: {
        add: `${GlobalConstants.API_BASE_URL}/travel/package/add`,
        update: `${GlobalConstants.API_BASE_URL}/travel/package/update`,
        delete: `${GlobalConstants.API_BASE_URL}/travel/package/delete`,
        get: `${GlobalConstants.API_BASE_URL}/travel/package/get`,
        gets: `${GlobalConstants.API_BASE_URL}/travel/package/gets`,
      },
      booking: {
        add: `${GlobalConstants.API_BASE_URL}/travel/booking/add`,
        update: `${GlobalConstants.API_BASE_URL}/travel/booking/update`,
        delete: `${GlobalConstants.API_BASE_URL}/travel/booking/delete`,
        get: `${GlobalConstants.API_BASE_URL}/travel/booking/get`,
        gets: `${GlobalConstants.API_BASE_URL}/travel/booking/gets`,
        availability: `${GlobalConstants.API_BASE_URL}/travel/booking/availability`,
      },
      visa: {
        add: `${GlobalConstants.API_BASE_URL}/travel/visa/add`,
        update: `${GlobalConstants.API_BASE_URL}/travel/visa/update`,
        delete: `${GlobalConstants.API_BASE_URL}/travel/visa/delete`,
        get: `${GlobalConstants.API_BASE_URL}/travel/visa/get`,
        gets: `${GlobalConstants.API_BASE_URL}/travel/visa/gets`,
      },
      document: {
        add: `${GlobalConstants.API_BASE_URL}/travel/document/add`,
        update: `${GlobalConstants.API_BASE_URL}/travel/document/update`,
        delete: `${GlobalConstants.API_BASE_URL}/travel/document/delete`,
        get: `${GlobalConstants.API_BASE_URL}/travel/document/get`,
        gets: `${GlobalConstants.API_BASE_URL}/travel/document/gets`,
        download: `${GlobalConstants.API_BASE_URL}/travel/document/download`,
        view: `${GlobalConstants.API_BASE_URL}/travel/document/view`,
        upload: `${GlobalConstants.API_BASE_URL}/travel/document/upload`,
        viewRef: `${GlobalConstants.API_BASE_URL}/travel/document/view-ref`,
        downloadRef: `${GlobalConstants.API_BASE_URL}/travel/document/download-ref`,
      },
      supplier: {
        add: `${GlobalConstants.API_BASE_URL}/travel/supplier/add`,
        update: `${GlobalConstants.API_BASE_URL}/travel/supplier/update`,
        delete: `${GlobalConstants.API_BASE_URL}/travel/supplier/delete`,
        get: `${GlobalConstants.API_BASE_URL}/travel/supplier/get`,
        gets: `${GlobalConstants.API_BASE_URL}/travel/supplier/gets`,
      },
      invoice: {
        add: `${GlobalConstants.API_BASE_URL}/travel/invoice/add`,
        update: `${GlobalConstants.API_BASE_URL}/travel/invoice/update`,
        delete: `${GlobalConstants.API_BASE_URL}/travel/invoice/delete`,
        get: `${GlobalConstants.API_BASE_URL}/travel/invoice/get`,
        gets: `${GlobalConstants.API_BASE_URL}/travel/invoice/gets`,
      },
      payment: {
        add: `${GlobalConstants.API_BASE_URL}/travel/payment/add`,
        update: `${GlobalConstants.API_BASE_URL}/travel/payment/update`,
        delete: `${GlobalConstants.API_BASE_URL}/travel/payment/delete`,
        get: `${GlobalConstants.API_BASE_URL}/travel/payment/get`,
        gets: `${GlobalConstants.API_BASE_URL}/travel/payment/gets`,
      },
      commissionRule: {
        add: `${GlobalConstants.API_BASE_URL}/travel/commission-rule/add`,
        update: `${GlobalConstants.API_BASE_URL}/travel/commission-rule/update`,
        delete: `${GlobalConstants.API_BASE_URL}/travel/commission-rule/delete`,
        get: `${GlobalConstants.API_BASE_URL}/travel/commission-rule/get`,
        gets: `${GlobalConstants.API_BASE_URL}/travel/commission-rule/gets`,
      },
      followUp: {
        add: `${GlobalConstants.API_BASE_URL}/travel/follow-up/add`,
        update: `${GlobalConstants.API_BASE_URL}/travel/follow-up/update`,
        delete: `${GlobalConstants.API_BASE_URL}/travel/follow-up/delete`,
        get: `${GlobalConstants.API_BASE_URL}/travel/follow-up/get`,
        gets: `${GlobalConstants.API_BASE_URL}/travel/follow-up/gets`,
      },
      destination: {
        add: `${GlobalConstants.API_BASE_URL}/travel/destination/add`,
        update: `${GlobalConstants.API_BASE_URL}/travel/destination/update`,
        delete: `${GlobalConstants.API_BASE_URL}/travel/destination/delete`,
        get: `${GlobalConstants.API_BASE_URL}/travel/destination/get`,
        gets: `${GlobalConstants.API_BASE_URL}/travel/destination/gets`,
      },
      lookup: {
        countriesGets: `${GlobalConstants.API_BASE_URL}/travel/lookup/countries/gets`,
      },
      tripRequest: {
        add: `${GlobalConstants.API_BASE_URL}/travel/trip-request/add`,
        update: `${GlobalConstants.API_BASE_URL}/travel/trip-request/update`,
        delete: `${GlobalConstants.API_BASE_URL}/travel/trip-request/delete`,
        get: `${GlobalConstants.API_BASE_URL}/travel/trip-request/get`,
        gets: `${GlobalConstants.API_BASE_URL}/travel/trip-request/gets`,
        quote: `${GlobalConstants.API_BASE_URL}/travel/trip-request/quote`,
        accept: `${GlobalConstants.API_BASE_URL}/travel/trip-request/accept`,
        reject: `${GlobalConstants.API_BASE_URL}/travel/trip-request/reject`,
        convert: `${GlobalConstants.API_BASE_URL}/travel/trip-request/convert`,
      },
    },
    um: {
      user: {
        add: `${GlobalConstants.API_BASE_URL}/um/user/add`,
        update: `${GlobalConstants.API_BASE_URL}/um/user/update`,
        delete: `${GlobalConstants.API_BASE_URL}/um/user/delete`,
        get: `${GlobalConstants.API_BASE_URL}/um/user/get`,
        gets: `${GlobalConstants.API_BASE_URL}/um/user/gets`,
        /** Self-service profile (JWT user only; no menu permission). */
        updateProfile: `${GlobalConstants.API_BASE_URL}/um/user/update-profile`,
      },
      workflow: {
        businessRegistrationRequest: `${GlobalConstants.API_BASE_URL}/um/workflow/instance/business-registration`,
        queue: `${GlobalConstants.API_BASE_URL}/um/workflow/instance/queue`,
        approve: `${GlobalConstants.API_BASE_URL}/um/workflow/instance/approve`,
        reject: `${GlobalConstants.API_BASE_URL}/um/workflow/instance/reject`,
        configList: `${GlobalConstants.API_BASE_URL}/um/workflow/config/list`,
        configUpdate: `${GlobalConstants.API_BASE_URL}/um/workflow/config/update`,
        configCatalog: `${GlobalConstants.API_BASE_URL}/um/workflow/config/catalog`,
        /** Mutating gateway paths registered for workflow (ADD/EDIT/DELETE); labels from menu metadata. */
        configEndpointCatalog: `${GlobalConstants.API_BASE_URL}/um/workflow/config/endpoint-catalog`,
        configCreate: `${GlobalConstants.API_BASE_URL}/um/workflow/config/create`,
        configDelete: `${GlobalConstants.API_BASE_URL}/um/workflow/config/delete`,
        catalogMutatingActions: `${GlobalConstants.API_BASE_URL}/um/workflow/catalog/mutating-actions`,
        /** Admin: DB-driven HTTP endpoint registry (match gateway paths to menu + action). */
        gatewayEndpointsList: `${GlobalConstants.API_BASE_URL}/um/workflow/gateway/endpoints/list`,
        gatewayEndpointsSync: `${GlobalConstants.API_BASE_URL}/um/workflow/gateway/endpoints/sync`,
        gatewayEndpointsUpdate: `${GlobalConstants.API_BASE_URL}/um/workflow/gateway/endpoints/update`,
        engineActionsList: `${GlobalConstants.API_BASE_URL}/um/workflow/engine/actions/list`,
        engineTaskCatalog: `${GlobalConstants.API_BASE_URL}/um/workflow/engine/tasks/catalog`,
        engineApplicationActionsList: `${GlobalConstants.API_BASE_URL}/um/workflow/engine/application-actions/list`,
        engineTemplatesList: `${GlobalConstants.API_BASE_URL}/um/workflow/engine/templates/list`,
        engineDefinitionsList: `${GlobalConstants.API_BASE_URL}/um/workflow/engine/definitions/list`,
        engineDefinitionsGet: `${GlobalConstants.API_BASE_URL}/um/workflow/engine/definitions/get`,
        engineDefinitionsCreate: `${GlobalConstants.API_BASE_URL}/um/workflow/engine/definitions/create`,
        engineDefinitionsUpdate: `${GlobalConstants.API_BASE_URL}/um/workflow/engine/definitions/update`,
        engineDefinitionsSaveSteps: `${GlobalConstants.API_BASE_URL}/um/workflow/engine/definitions/save-steps`,
        engineDefinitionsPublish: `${GlobalConstants.API_BASE_URL}/um/workflow/engine/definitions/publish`,
        engineDefinitionsDisable: `${GlobalConstants.API_BASE_URL}/um/workflow/engine/definitions/disable`,
        engineDefinitionsDelete: `${GlobalConstants.API_BASE_URL}/um/workflow/engine/definitions/delete`,
        engineTrigger: `${GlobalConstants.API_BASE_URL}/um/workflow/engine/trigger`,
      },
      roleLevel: {
        list: `${GlobalConstants.API_BASE_URL}/um/role-level/list`,
      },
      role: {
        add: `${GlobalConstants.API_BASE_URL}/um/role/add`,
        update: `${GlobalConstants.API_BASE_URL}/um/role/update`,
        delete: `${GlobalConstants.API_BASE_URL}/um/role/delete`,
        get: `${GlobalConstants.API_BASE_URL}/um/role/get`,
        gets: `${GlobalConstants.API_BASE_URL}/um/role/gets`,
        nextRoleType: `${GlobalConstants.API_BASE_URL}/um/role/next-role-type`,
        menuPermissionsGet: `${GlobalConstants.API_BASE_URL}/um/role/menu-permissions/get`,
        menuPermissionsSave: `${GlobalConstants.API_BASE_URL}/um/role/menu-permissions/save`,
      },
      teamRole: {
        list: `${GlobalConstants.API_BASE_URL}/um/business/team-role/list`,
        listAssignable: `${GlobalConstants.API_BASE_URL}/um/business/team-role/list-assignable`,
        parentOptions: `${GlobalConstants.API_BASE_URL}/um/business/team-role/parent-options`,
        add: `${GlobalConstants.API_BASE_URL}/um/business/team-role/add`,
        delete: `${GlobalConstants.API_BASE_URL}/um/business/team-role/delete`,
      },
      audit: {
        gets: `${GlobalConstants.API_BASE_URL}/um/audit/gets`,
      },
      menu: {
        permissionMetadata: `${GlobalConstants.API_BASE_URL}/um/menu/permission-metadata`,
      },
      applicationCatalog: {
        catalog: `${GlobalConstants.API_BASE_URL}/um/application-catalog/catalog`,
        applicationSave: `${GlobalConstants.API_BASE_URL}/um/application-catalog/application/save`,
        applicationDelete: `${GlobalConstants.API_BASE_URL}/um/application-catalog/application/delete`,
        menuSave: `${GlobalConstants.API_BASE_URL}/um/application-catalog/menu/save`,
        menuDelete: `${GlobalConstants.API_BASE_URL}/um/application-catalog/menu/delete`,
        reorder: `${GlobalConstants.API_BASE_URL}/um/application-catalog/reorder`,
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
        /** End-user Reports screen only (role/user grants). No CRUD. */
        viewer: {
          list: `${GlobalConstants.API_BASE_URL}/settings/reporting/viewer/list`,
        },
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