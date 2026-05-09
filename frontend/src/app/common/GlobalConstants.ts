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
      refresh: `${GlobalConstants.API_BASE_URL}/auth/refresh`
    },
    // Add other endpoint categories as needed
    users: {
      getAll: `${GlobalConstants.API_BASE_URL}/users`,
      getById: (id: string) => `${GlobalConstants.API_BASE_URL}/users/${id}`,
      create: `${GlobalConstants.API_BASE_URL}/users`
    },
    kyc:{
      getAllCustomers: `${GlobalConstants.API_BASE_URL}/kyc/getAllCustomers`,
      createCustomer: `${GlobalConstants.API_BASE_URL}/kyc/createCustomer`,
      deleteCustomer: (id: string) =>  `${GlobalConstants.API_BASE_URL}/kyc/deleteCustomer/${id}`,
      updateCustomer: (id: string) =>  `${GlobalConstants.API_BASE_URL}/kyc/updateCustomer/${id}`,
    },
    application:{
      getAll: `${GlobalConstants.API_BASE_URL}/um/menu/get`,

    }
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