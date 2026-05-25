import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, Subject, catchError, firstValueFrom, from, map, of, switchMap, tap } from 'rxjs';
import { GlobalConstants } from '../common/GlobalConstants';
import { environment } from '../../environments/environment';
import { DeviceIdService } from './device-id.service';

interface LoginPayload {
  token: string;
  refreshToken?: string | null;
  sessionId: string;
  availableRoles?: string[];
  activeRole?: string | null;
  /** True until the user finishes the welcome wizard. Drives the post-login redirect. */
  firstLogin?: boolean | null;
  /** Tenant id; null until business registration is complete. */
  businessId?: number | null;
  businessName?: string | null;
  /** ADMIN | BUSINESS — used by guards to decide whether to allow the registration flow. */
  roleLevel?: string | null;
  /** True while the business row is pending super-admin approval (governance gate). */
  pendingBusinessApproval?: boolean | null;
  /** Raw business status from auth (e.g. PENDING_APPROVAL, ACTIVE). */
  businessStatus?: string | null;
}

export interface MeResponse {
  userId: number;
  username: string;
  email?: string;
  firstName?: string;
  lastName?: string;
  mobileNumber?: string | null;
  profileImageMime?: string | null;
  profileImageBase64?: string | null;
  firstLogin: boolean;
  businessId?: number | null;
  businessName?: string | null;
  businessStatus?: string | null;
  pendingBusinessApproval?: boolean;
  roles: string[];
  roleLevel?: string | null;
  canRegisterBusiness: boolean;
}

export interface AssignableRole {
  id: number;
  name: string;
  levelCode: string;
  isDefault: boolean;
}

/**
 * One row in the public business-type catalog returned by
 * {@code POST /auth/business-types}. Each entry is just a role flagged
 * {@code is_business_type = 1} on the backend — the user picking one here
 * is the same as picking their starting role.
 */
export interface BusinessTypeOption {
  id: number;
  /** Canonical role name in DB (e.g. {@code RESTAURANT}). */
  name: string;
  /** Optional human label (e.g. {@code Beauty Center}). Falls back to {@code name}. */
  label?: string | null;
}

export interface RegisterBusinessRequest {
  businessName: string;
  /** Chosen business-type role id (mandatory in the new flow). */
  roleId?: number | null;
  /** Legacy free-form code — kept so old clients keep working. */
  businessType?: string;
}

export interface RegisterBusinessResponse {
  businessId: number;
  businessName: string;
  session: LoginPayload;
  /** When true, start the UM business-registration approval workflow (non-ACTIVE businesses). */
  requiresBusinessApprovalWorkflow?: boolean;
}

/** Public sign-up payload. {@code authProvider} / {@code providerUserId} are filled when
 *  the user clicked Google/Facebook on the Create-Account screen — otherwise omitted. */
export interface RegisterRequest {
  username: string;
  email: string;
  password?: string;
  confirmPassword?: string;
  firstName: string;
  lastName: string;
  mobileNumber?: string;
  businessName: string;
  /** Chosen business-type role id (mandatory in the new flow). */
  roleId?: number | null;
  /** Legacy free-form code (e.g. {@code RESTAURANT}) for older clients. */
  businessType?: string;
  authProvider?: 'GOOGLE' | null;
  providerUserId?: string | null;
}

export interface RegisterResponse {
  userId: number;
  username: string;
  businessId: number;
  businessName: string;
  session: LoginPayload;
}

export interface AdminBusiness {
  id: number;
  businessName: string;
  businessType?: string | null;
  status?: string | null;
  createdAt?: string | null;
}

export interface AdminUser {
  id: number;
  username: string;
  email?: string | null;
  firstName?: string | null;
  lastName?: string | null;
  businessId?: number | null;
}

/** JWT {@code perms} row — short keys to keep token size down. */
export interface JwtMenuPermRow {
  m: number;
  r?: string;
  v?: boolean;
  a?: boolean;
  e?: boolean;
  d?: boolean;
}

/** Unverified payload fields from the access JWT (shell + guards). Issued at login / refresh / role switch. */
export interface JwtAccessClaims {
  userId: number;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  businessId?: number | null;
  roles: string[];
  activeRole?: string | null;
  firstLogin?: boolean;
  roleLevel?: string | null;
  permMatrix?: boolean;
  perms?: JwtMenuPermRow[];
  profileImageMime?: string;
  profileImageBase64?: string;
  profileImageInJwt?: boolean;
  /** Backend issued the JWT without the image because it exceeds the embed budget. */
  profileImageOversized?: boolean;
  /** When true, the tenant is blocked at the governance gate until an administrator activates the business. */
  pendingBusinessApproval?: boolean;
  /** Canonical business row status from auth (e.g. ACTIVE, PENDING_APPROVAL). */
  businessStatus?: string | null;
}

/** Response of {@code POST /auth/me/avatar} — full avatar bytes when JWT couldn't embed them. */
export interface MeAvatarResponse {
  userId: number;
  mime: string;
  base64: string;
}

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly roleRefreshSubject = new Subject<void>();
  /** Emits after login refresh or active-role switch — reload menus when subscribed. */
  readonly roleRefresh$ = this.roleRefreshSubject.asObservable();

  // Public certificate exported from backend JKS (alias: my-server-key).
  // The frontend encrypts the password before sending it to /auth/login.
  private readonly publicCertificatePem = `-----BEGIN CERTIFICATE-----
MIIDeTCCAmGgAwIBAgIEMnngMTANBgkqhkiG9w0BAQsFADBtMQswCQYDVQQGEwJM
QjEQMA4GA1UECBMHbGViYW5vbjEQMA4GA1UEBxMHbGViYW5vbjEOMAwGA1UEChMF
Yml6bHkxDjAMBgNVBAsTBWJpemx5MRowGAYDVQQDExFNYXJjZWxpbm8gQXRhbGxh
aDAeFw0yNTEyMDEyMDA1NThaFw0zNTExMjkyMDA1NThaMG0xCzAJBgNVBAYTAkxC
MRAwDgYDVQQIEwdsZWJhbm9uMRAwDgYDVQQHEwdsZWJhbm9uMQ4wDAYDVQQKEwVi
aXpseTEOMAwGA1UECxMFYml6bHkxGjAYBgNVBAMTEU1hcmNlbGlubyBBdGFsbGFo
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAiEiJUBM+bHegcTdnPzfO
/ykHKJNSVPjz6mPrPZvCNvl0+v7s5N4TTGrcO7BSGTUJzrgh1Bd7MyXRvmPLs2Sb
uPGhIwoyhDborSoRtr+3E0HQar7MaFnF+o9MVt9Cm40FJFVaeKaJGtd2Cua1PtWY
craR4QlhDoWJsLCKVMgzjLmAdaffzZFVVklx4AJGYrdVQld33xGHt1qr0ZOFqKzS
imoBcTlRuaJFZhw0vJBteYj8YIrF2NnYeYYvqTmHCOuTIIx/243D0xN9nThD+ZNH
+GmzDvc1q1YbmsZ9CqD4nHCT/YPn4xg3fRCH5HoQthNn4iOjzoUb4nKHShRhJBNA
wQIDAQABoyEwHzAdBgNVHQ4EFgQUYW4V069CJvrR2uleTsEfWIol8XUwDQYJKoZI
hvcNAQELBQADggEBAAGapenmyeA9qfqy0UysGK4bBiDly3bylLNlUgh5Gq89sG5A
zQXsu/CCoQpOGk63ZzIaG3+APN5BHVB6X96ml0jwkniLwMX9LALw/McQfEX5igUz
6cJjB1aH7oZfP9T8HJtYlX8Nqs5ErmX6FRbt8CRdnB+x1VuKKEmd9izmtB0jGb6g
J3A78TRa1aX7GNeBsoOkZSLgCVZqtDPmCq5RE0SEa8KRctZFuWgZz5rff4goaFe9
aFXNrD7u6kSaXqtI89LlGBUmxtKVX07kJ+ya9aloqFEPpVzINXaG/R1PLy/eWnVs
DdonpI93CG9kkKqwaKPQnsYX3PyFEH2aA3I7N/0=
-----END CERTIFICATE-----`;

  constructor(
    private readonly http: HttpClient,
    private readonly deviceIdService: DeviceIdService
  ) {}

  login(
    username: string,
    password: string,
    rememberDevice: boolean
  ): Observable<ApiResponse<LoginPayload>> {
    return from(
      Promise.all([
        this.encryptPassword(password),
        this.deviceIdService.getOrCreateDeviceId(rememberDevice),
      ])
    ).pipe(
      switchMap(([encryptedPassword, deviceId]) => {
        const params = new HttpParams()
          .set('username', username)
          .set('password', encryptedPassword);

        const headers = new HttpHeaders({
          'Content-Type': 'application/x-www-form-urlencoded',
          Accept: 'application/json',
          'X-DEVICE-ID': deviceId,
        });

        return this.http.post<ApiResponse<LoginPayload>>(
          GlobalConstants.API_ENDPOINTS.auth.login,
          params.toString(),
          { headers }
        );
      }),
      tap((response) => {
        this.persistSession(response?.data);
        this.roleRefreshSubject.next();
      })
    );
  }

  /**
   * Narrows effective permissions server-side via {@code um_user_session.active_role_name}.
   * Refresh token is unchanged; omitted from API response — existing refresh stays valid.
   */
  setActiveRole(activeRoleName: string | null): Observable<ApiResponse<LoginPayload>> {
    const sessionId = this.getSessionId();
    if (!sessionId) {
      throw new Error('Missing session');
    }

    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      Accept: 'application/json',
      'X-DEVICE-ID': this.deviceIdService.getDeviceId(),
    });

    return this.http
      .post<ApiResponse<LoginPayload>>(
        GlobalConstants.API_ENDPOINTS.auth.sessionActiveRole,
        { sessionId, activeRoleName: activeRoleName ?? '' },
        { headers }
      )
      .pipe(
        tap((response) => this.persistSession(response?.data)),
        tap(() => this.roleRefreshSubject.next())
      );
  }

  forgotPassword(username: string): Observable<ApiResponse<string>> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      Accept: 'application/json',
    });

    return this.http.post<ApiResponse<string>>(
      GlobalConstants.API_ENDPOINTS.auth.forgotPassword,
      { username },
      { headers }
    );
  }

  verifyForgotPasswordToken(token: string): Observable<ApiResponse<string>> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      Accept: 'application/json',
    });

    return this.http.post<ApiResponse<string>>(
      GlobalConstants.API_ENDPOINTS.auth.verifyForgotPasswordToken,
      { token },
      { headers }
    );
  }
  verifyEmailSetPassword(token: string, password: string): Observable<ApiResponse<string>> {
    return from(this.encryptPassword(password)).pipe(
      switchMap((encryptedPassword) => {
        const headers = new HttpHeaders({
          'Content-Type': 'application/json',
          Accept: 'application/json',
        });
        return this.http.post<ApiResponse<string>>(
          GlobalConstants.API_ENDPOINTS.auth.verifyEmailSetPassword,
          { token, password: encryptedPassword },
          { headers }
        );
      })
    );
  }

  resetForgotPassword(
    token: string,
    newPassword: string,
    confirmPassword: string
  ): Observable<ApiResponse<string>> {
    return from(this.encryptPassword(newPassword)).pipe(
      switchMap((encryptedPassword) =>
        from(this.encryptPassword(confirmPassword)).pipe(
          switchMap((encryptedConfirmPassword) => {
            const headers = new HttpHeaders({
              'Content-Type': 'application/json',
              Accept: 'application/json',
            });
  
            return this.http.post<ApiResponse<string>>(
              GlobalConstants.API_ENDPOINTS.auth.resetForgotPassword,
              {
                token,
                newPassword: encryptedPassword,
                confirmPassword: encryptedConfirmPassword,
              },
              { headers }
            );
          })
        )
      )
    );
  }
  // resetForgotPassword(token: string, newPassword: string): Observable<ApiResponse<string>> {
  //   const headers = new HttpHeaders({
  //     'Content-Type': 'application/json',
  //     Accept: 'application/json',
  //   });

  //   return this.http.post<ApiResponse<string>>(
  //     GlobalConstants.API_ENDPOINTS.auth.resetForgotPassword,
  //     { token, newPassword },
  //     { headers }
  //   );
  // }

  logout(): Observable<ApiResponse<string>> {
    const sessionId = this.getSessionId();
    if (!sessionId) {
      this.clearSession();
      return from(Promise.resolve({ success: true, message: 'Already logged out', data: '' }));
    }

    const params = new HttpParams().set('sessionId', sessionId);
    const headers = new HttpHeaders({
      Accept: 'application/json',
      'X-DEVICE-ID': this.deviceIdService.getDeviceId(),
    });

    return this.http
      .post<ApiResponse<string>>(GlobalConstants.API_ENDPOINTS.auth.logout, null, {
        headers,
        params,
      })
      .pipe(
        tap(() => this.clearSession()),
        map((response) => response)
      );
  }

  refreshToken(): Observable<ApiResponse<LoginPayload>> {
    const refreshToken = this.getRefreshToken();
    const sessionId = this.getSessionId();
    if (!refreshToken || !sessionId) {
      throw new Error('Missing refresh token/session');
    }

    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      Accept: 'application/json',
      'X-DEVICE-ID': this.deviceIdService.getDeviceId(),
    });

    return this.http
      .post<ApiResponse<LoginPayload>>(
        GlobalConstants.API_ENDPOINTS.auth.refresh,
        { refreshToken, sessionId },
        { headers }
      )
      .pipe(
        tap((response) => this.persistSession(response?.data)),
        tap(() => this.roleRefreshSubject.next())
      );
  }

  /**
   * Re-issues the access token from the server (picks up role-matrix edits). Called once per
   * browser session before the first menu catalog load so F5 reflects UM permission edits.
   */
  syncSessionPermissionsOnce(): Promise<void> {
    if (this.sessionPermsSynced || !this.isAuthenticated()) {
      return Promise.resolve();
    }
    const refreshToken = this.getRefreshToken();
    const sessionId = this.getSessionId();
    if (!refreshToken || !sessionId) {
      return Promise.resolve();
    }
    this.sessionPermsSynced = true;
    return firstValueFrom(
      this.refreshToken().pipe(
        map(() => undefined),
        catchError(() => of(undefined))
      )
    );
  }

  private sessionPermsSynced = false;

  isAuthenticated(): boolean {
    return !!this.getAccessToken();
  }

  /**
   * Decodes JWT access token payload (unverified) for UI — identity, tenant, roles, permissions, profile.
   * Prefer this over calling UM {@code /user/get} for the layout shell so users without UM view rights still
   * render the header correctly.
   */
  getAccessTokenClaims(): JwtAccessClaims | null {
    const p = this.decodeAccessPayload();
    if (!p) {
      return null;
    }
    const rawId = p['userId'] ?? p['user_id'];
    const userId = typeof rawId === 'number' ? rawId : Number(rawId);
    if (!Number.isFinite(userId)) {
      return null;
    }
    const rawRoles = p['roles'] ?? p['role'];
    const roles = Array.isArray(rawRoles)
      ? rawRoles.map((x) => String(x))
      : rawRoles != null && rawRoles !== ''
        ? [String(rawRoles)]
        : [];
    const rawBiz = p['businessId'];
    let businessId: number | null | undefined;
    if (rawBiz === undefined) {
      businessId = undefined;
    } else if (rawBiz === null) {
      businessId = null;
    } else {
      const n = typeof rawBiz === 'number' ? rawBiz : Number(rawBiz);
      businessId = Number.isFinite(n) ? n : null;
    }
    const fl = p['firstLogin'];
    const permsRaw = p['perms'];
    const perms = Array.isArray(permsRaw) ? (permsRaw as JwtMenuPermRow[]) : undefined;
    const ar = p['activeRole'];
    return {
      userId,
      username: p['username'] != null ? String(p['username']) : '',
      email: p['email'] != null ? String(p['email']) : '',
      firstName: p['firstName'] != null ? String(p['firstName']) : undefined,
      lastName: p['lastName'] != null ? String(p['lastName']) : undefined,
      businessId,
      roles,
      activeRole: ar == null || ar === '' ? undefined : String(ar),
      firstLogin: typeof fl === 'boolean' ? fl : fl === 'true' ? true : fl === 'false' ? false : undefined,
      roleLevel: p['roleLevel'] != null ? String(p['roleLevel']) : undefined,
      permMatrix: p['permMatrix'] === true,
      perms,
      profileImageMime: p['profileImageMime'] != null ? String(p['profileImageMime']) : undefined,
      profileImageBase64: p['profileImageBase64'] != null ? String(p['profileImageBase64']) : undefined,
      profileImageInJwt: p['profileImageInJwt'] === true,
      profileImageOversized: p['profileImageOversized'] === true,
      pendingBusinessApproval: p['pendingBusinessApproval'] === true,
    };
  }

  /**
   * Lazy fetch the navbar avatar when the JWT couldn't carry it (oversized or absent). Hits the
   * gateway-protected {@code /auth/me/avatar} so the user is resolved from the verified X-User
   * header — the SPA never has to send a userId for someone else's picture.
   */
  fetchMyAvatar(): Observable<ApiResponse<MeAvatarResponse>> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      Accept: 'application/json',
      'X-DEVICE-ID': this.deviceIdService.getDeviceId(),
    });
    return this.http.post<ApiResponse<MeAvatarResponse>>(
      GlobalConstants.API_ENDPOINTS.auth.meAvatar,
      {},
      { headers }
    );
  }

  getAccessToken(): string | null {
    return localStorage.getItem('jwtAccessToken');
  }

  /** Grace period after access-token expiry during which the user may refresh or sign out via the dialog. */
  getSessionExtendGraceMs(): number {
    return environment.sessionExtendGraceMs ?? 15 * 60 * 1000;
  }

  /** JWT {@code exp} (seconds since epoch), or null when missing / undecodable. */
  getAccessTokenExpEpochSec(): number | null {
    const p = this.decodeAccessPayload();
    const exp = p?.['exp'];
    if (typeof exp === 'number' && Number.isFinite(exp)) {
      return exp;
    }
    if (typeof exp === 'string' && exp.trim() !== '') {
      const n = Number(exp);
      return Number.isFinite(n) ? n : null;
    }
    return null;
  }

  /**
   * Milliseconds since the access token expired. Negative if still valid; null if unknown.
   */
  getAccessTokenExpiredForMs(): number | null {
    const expSec = this.getAccessTokenExpEpochSec();
    if (expSec == null) {
      return null;
    }
    return Date.now() - expSec * 1000;
  }

  /** Remaining time (ms) to show the extend-session dialog before forced sign-out. */
  getSessionExtendGraceRemainingMs(): number {
    const expiredFor = this.getAccessTokenExpiredForMs();
    const grace = this.getSessionExtendGraceMs();
    if (expiredFor == null) {
      return 0;
    }
    if (expiredFor <= 0) {
      return grace;
    }
    return Math.max(0, grace - expiredFor);
  }

  /**
   * True when the session has been expired longer than the grace window — caller should sign out
   * without prompting.
   */
  isSessionExtendGraceExceeded(): boolean {
    const expiredFor = this.getAccessTokenExpiredForMs();
    if (expiredFor == null) {
      return true;
    }
    if (expiredFor <= 0) {
      return false;
    }
    return expiredFor > this.getSessionExtendGraceMs();
  }

  getRefreshToken(): string | null {
    return localStorage.getItem('jwtRefreshToken');
  }

  getSessionId(): string | null {
    return localStorage.getItem('jwtSessionId');
  }

  clearSession(): void {
    this.sessionPermsSynced = false;
    localStorage.removeItem('jwtAccessToken');
    localStorage.removeItem('jwtRefreshToken');
    localStorage.removeItem('jwtSessionId');
    localStorage.removeItem('bizlyAvailableRoles');
    localStorage.removeItem('bizlyActiveRole');
    localStorage.removeItem('bizlyFirstLogin');
    localStorage.removeItem('bizlyBusinessId');
    localStorage.removeItem('bizlyBusinessName');
    localStorage.removeItem('bizlyRoleLevel');
    localStorage.removeItem('bizlyPendingBusinessApproval');
    localStorage.removeItem('bizlyAdminBusinessOverride');
    localStorage.removeItem('bizlyAdminBusinessOverrideName');
  }

  /** Role codes from JWT {@code role} claim (all assignments). */
  getJwtRoleNames(): string[] {
    const p = this.decodeAccessPayload();
    if (!p) {
      return [];
    }
    /* Auth service issues {@code roles} (array). Legacy tokens used singular {@code role}; we still
       honour it so a stale token in localStorage doesn't break the role-switcher dropdown. */
    const r = p['roles'] ?? p['role'];
    if (Array.isArray(r)) {
      return r.map((x) => String(x)).filter((x) => x.length > 0);
    }
    if (r != null && r !== '') {
      return [String(r)];
    }
    return [];
  }

  /**
   * Role level (ADMIN / BUSINESS / …) of the **currently active** role, taken straight from the
   * fresh JWT — not from the localStorage cache that {@link getCachedRoleLevel} reads. The cache
   * is updated lazily by {@link persistSession}; reading the JWT here guarantees correctness right
   * after a role switch when guards / permission service need the new value.
   */
  getJwtRoleLevel(): string | null {
    const p = this.decodeAccessPayload();
    if (!p) {
      return null;
    }
    const v = p['roleLevel'];
    if (v == null || v === '') {
      return null;
    }
    return String(v);
  }

  /** True when the active JWT role is ADMIN-level (portal provisioning, business switcher). */
  isJwtAdminLevel(): boolean {
    const lvl = this.getJwtRoleLevel();
    return !!lvl && lvl.toUpperCase() === 'ADMIN';
  }

  /**
   * True only for the portal root admin ({@code tenantBypass} in JWT). Delegated internal admin roles
   * use the menu matrix even though {@link isJwtAdminLevel} may still be true.
   */
  isJwtTenantBypass(): boolean {
    const p = this.decodeAccessPayload();
    if (!p) {
      return false;
    }
    const tb = p['tenantBypass'];
    return tb === true;
  }

  /** Session-selected role from JWT {@code activeRole} claim (mirrors DB session). */
  getJwtActiveRole(): string | null {
    const p = this.decodeAccessPayload();
    if (!p) {
      return null;
    }
    const a = p['activeRole'];
    if (a == null || a === '') {
      return null;
    }
    return String(a);
  }

  /** True when this role has at least one row in {@code UM_ROLE_MENU_PERM} (strict UI + APIs). */
  getJwtPermMatrix(): boolean {
    const p = this.decodeAccessPayload();
    if (!p) {
      return false;
    }
    return p['permMatrix'] === true;
  }

  getJwtMenuPerms(): JwtMenuPermRow[] {
    const p = this.decodeAccessPayload();
    const raw = p?.['perms'];
    if (!Array.isArray(raw)) {
      return [];
    }
    return raw as JwtMenuPermRow[];
  }

  private decodeAccessPayload(): Record<string, unknown> | null {
    const t = this.getAccessToken();
    if (!t) {
      return null;
    }
    try {
      const parts = t.split('.');
      if (parts.length !== 3) {
        return null;
      }
      const json = atob(parts[1].replace(/-/g, '+').replace(/_/g, '/'));
      return JSON.parse(json) as Record<string, unknown>;
    } catch {
      return null;
    }
  }

  private persistSession(data?: LoginPayload): void {
    if (!data) {
      return;
    }
    if (data.token) {
      this.sessionPermsSynced = true;
      localStorage.setItem('jwtAccessToken', data.token);
    }
    if (data.refreshToken != null && data.refreshToken !== '') {
      localStorage.setItem('jwtRefreshToken', data.refreshToken);
    }
    if (data.sessionId) {
      localStorage.setItem('jwtSessionId', data.sessionId);
    }
    if (data.availableRoles != null) {
      localStorage.setItem('bizlyAvailableRoles', JSON.stringify(data.availableRoles));
    }
    if (data.activeRole !== undefined) {
      if (data.activeRole == null || data.activeRole === '') {
        localStorage.removeItem('bizlyActiveRole');
      } else {
        localStorage.setItem('bizlyActiveRole', data.activeRole);
      }
    }
    // Cache tenant + onboarding state for routing guards. The backend remains the source of
    // truth (re-validated on every /auth/me call), but caching avoids a network round-trip
    // before every navigation.
    if (data.firstLogin != null) {
      localStorage.setItem('bizlyFirstLogin', data.firstLogin ? '1' : '0');
    }
    if (data.businessId != null) {
      localStorage.setItem('bizlyBusinessId', String(data.businessId));
    } else if (data.businessId === null) {
      localStorage.removeItem('bizlyBusinessId');
    }
    if (data.businessName != null) {
      localStorage.setItem('bizlyBusinessName', data.businessName);
    } else if (data.businessName === null) {
      localStorage.removeItem('bizlyBusinessName');
    }
    // Clearing matters here: an onboarding-stage JWT (e.g. a brand-new Google
    // user with no role yet) reports {@code roleLevel = null}. If we kept the
    // previous tab's cached "ADMIN" string the onboarding guard would skip the
    // /authentication/register-business redirect and drop the user on a screen
    // their (empty) role cannot reach.
    if (data.roleLevel != null && data.roleLevel !== '') {
      localStorage.setItem('bizlyRoleLevel', data.roleLevel);
    } else if (data.roleLevel === null || data.roleLevel === '') {
      localStorage.removeItem('bizlyRoleLevel');
    }
    if (data.pendingBusinessApproval === true || this.loginPayloadIndicatesPending(data)) {
      localStorage.setItem('bizlyPendingBusinessApproval', '1');
    } else if (data.pendingBusinessApproval === false && !this.loginPayloadIndicatesPending(data)) {
      localStorage.removeItem('bizlyPendingBusinessApproval');
    } else if (data.token) {
      const p = this.decodeAccessPayload();
      if (p && this.jwtPayloadIndicatesPendingApproval(p)) {
        localStorage.setItem('bizlyPendingBusinessApproval', '1');
      }
    }
  }

  private loginPayloadIndicatesPending(data: LoginPayload): boolean {
    return AuthService.isBusinessStatusPendingApproval(
      (data as LoginPayload & { businessStatus?: string | null }).businessStatus
    );
  }

  private jwtPayloadIndicatesPendingApproval(p: Record<string, unknown>): boolean {
    if (p['pendingBusinessApproval'] === true) {
      return true;
    }
    return AuthService.isBusinessStatusPendingApproval(p['businessStatus']);
  }

  private static isBusinessStatusPendingApproval(status: unknown): boolean {
    return typeof status === 'string' && status.trim().toUpperCase() === 'PENDING_APPROVAL';
  }

  /** Cached helpers backed by the values persistSession wrote. */
  getCachedFirstLogin(): boolean | null {
    const v = localStorage.getItem('bizlyFirstLogin');
    if (v == null) return null;
    return v === '1';
  }
  getCachedBusinessId(): number | null {
    const v = localStorage.getItem('bizlyBusinessId');
    if (!v) return null;
    const n = Number(v);
    return Number.isFinite(n) ? n : null;
  }
  getCachedBusinessName(): string | null { return localStorage.getItem('bizlyBusinessName'); }
  getCachedRoleLevel(): string | null { return localStorage.getItem('bizlyRoleLevel'); }

  /** True when the session / JWT indicates a pending business approval gate (must stay off main app). */
  getCachedPendingBusinessApproval(): boolean {
    if (localStorage.getItem('bizlyPendingBusinessApproval') === '1') {
      return true;
    }
    const p = this.decodeAccessPayload();
    if (!p) {
      return false;
    }
    if (p['pendingBusinessApproval'] === true) {
      return true;
    }
    return AuthService.isBusinessStatusPendingApproval(p['businessStatus']);
  }

  /** Fresh state from backend — call after login and after every register-business / welcome-complete. */
  fetchMe() {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      Accept: 'application/json',
      'X-DEVICE-ID': this.deviceIdService.getDeviceId(),
    });
    return this.http
      .post<ApiResponse<MeResponse>>(GlobalConstants.API_ENDPOINTS.auth.me, {}, { headers })
      .pipe(
        tap((response) => {
          const d = response?.data;
          if (!d) return;
          localStorage.setItem('bizlyFirstLogin', d.firstLogin ? '1' : '0');
          if (d.businessId != null) {
            localStorage.setItem('bizlyBusinessId', String(d.businessId));
          } else {
            localStorage.removeItem('bizlyBusinessId');
          }
          if (d.businessName) {
            localStorage.setItem('bizlyBusinessName', d.businessName);
          }
          if (d.roleLevel) {
            localStorage.setItem('bizlyRoleLevel', d.roleLevel);
          } else {
            // See persistSession() — clear so an onboarding user can't inherit
            // a stale "ADMIN" cached from the previous session.
            localStorage.removeItem('bizlyRoleLevel');
          }
          if (d.pendingBusinessApproval === true || AuthService.isBusinessStatusPendingApproval(d.businessStatus)) {
            localStorage.setItem('bizlyPendingBusinessApproval', '1');
          } else {
            localStorage.removeItem('bizlyPendingBusinessApproval');
          }
        })
      );
  }

  listAssignableRoles() {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      Accept: 'application/json',
      'X-DEVICE-ID': this.deviceIdService.getDeviceId(),
    });
    return this.http.post<ApiResponse<AssignableRole[]>>(
      GlobalConstants.API_ENDPOINTS.auth.assignableRoles, {}, { headers }
    );
  }

  /**
   * Public catalogue of business-type roles for the registration wizards.
   * Anonymous endpoint — works on the public "Create Account" page before the
   * user has any session, and on the post-login "Set up your business" page
   * after a social sign-up.
   *
   * The backend filters out SUPER_ADMIN / system / non-BUSINESS-level rows;
   * any tampered roleId sent later in the register payload is re-validated on
   * the server before being assigned.
   */
  listBusinessTypes() {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      Accept: 'application/json',
      'X-DEVICE-ID': this.deviceIdService.getDeviceId(),
    });
    return this.http.post<ApiResponse<BusinessTypeOption[]>>(
      GlobalConstants.API_ENDPOINTS.auth.businessTypes, {}, { headers }
    );
  }

  registerBusiness(req: RegisterBusinessRequest) {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      Accept: 'application/json',
      'X-DEVICE-ID': this.deviceIdService.getDeviceId(),
    });
    return this.http
      .post<ApiResponse<RegisterBusinessResponse>>(
        GlobalConstants.API_ENDPOINTS.auth.registerBusiness, req, { headers })
      .pipe(tap((response) => this.persistSession(response?.data?.session)));
  }

  /**
   * Public self-service sign-up — creates the user, business, role assignment, and an
   * authenticated session in one atomic backend transaction. The wizard collects the
   * fields across two steps but submits them together; the password is encrypted before
   * leaving the browser, exactly the same way /auth/login does it.
   *
   * On success the SPA is already logged-in (the response includes a full LoginPayload),
   * so the caller should navigate to the dashboard directly.
   */
  register(req: RegisterRequest, rememberDevice: boolean): Observable<ApiResponse<RegisterResponse>> {
    return from(
      Promise.all([
        req.password ? this.encryptPassword(req.password) : Promise.resolve(''),
        req.confirmPassword ? this.encryptPassword(req.confirmPassword) : Promise.resolve(''),
        this.deviceIdService.getOrCreateDeviceId(rememberDevice),
      ])
    ).pipe(
      switchMap(([encryptedPwd, encryptedConfirm, deviceId]) => {
        const headers = new HttpHeaders({
          'Content-Type': 'application/json',
          Accept: 'application/json',
          'X-DEVICE-ID': deviceId,
        });
        // We intentionally send the encrypted password (same envelope as /auth/login).
        // Social sign-ups send an empty password; the backend generates a random one.
        const body: RegisterRequest = {
          ...req,
          password: req.authProvider ? undefined : encryptedPwd,
          confirmPassword: req.authProvider ? undefined : encryptedConfirm,
        };
        return this.http
          .post<ApiResponse<RegisterResponse>>(
            GlobalConstants.API_ENDPOINTS.auth.register, body, { headers })
          .pipe(tap((response) => this.persistSession(response?.data?.session)));
      })
    );
  }

  /* ------- Admin context switcher (SUPER_ADMIN only). ------- */
  adminSearchBusinesses(q: string, limit = 20): Observable<ApiResponse<AdminBusiness[]>> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json', Accept: 'application/json',
      'X-DEVICE-ID': this.deviceIdService.getDeviceId(),
    });
    return this.http.post<ApiResponse<AdminBusiness[]>>(
      GlobalConstants.API_ENDPOINTS.auth.adminSearchBusinesses, { q, limit }, { headers });
  }

  adminSearchUsers(q: string, limit = 20): Observable<ApiResponse<AdminUser[]>> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json', Accept: 'application/json',
      'X-DEVICE-ID': this.deviceIdService.getDeviceId(),
    });
    return this.http.post<ApiResponse<AdminUser[]>>(
      GlobalConstants.API_ENDPOINTS.auth.adminSearchUsers, { q, limit }, { headers });
  }

  /**
   * Selected "act-as" business id for admin power-users. Stored in localStorage so it
   * survives a refresh, picked up by the CustomHTTPInterceptor and sent as
   * {@code X-Business-Override} on every downstream API call. Cleared by
   * {@link clearAdminContext} or {@link clearSession}.
   *
   * Server-side, the downstream {@code InternalAuthFilter} only honours this header when
   * the caller's role level is ADMIN — a tampered SPA cannot bypass tenancy.
   */
  setAdminBusinessContext(businessId: number | null, businessName: string | null): void {
    if (businessId == null) {
      localStorage.removeItem('bizlyAdminBusinessOverride');
      localStorage.removeItem('bizlyAdminBusinessOverrideName');
      return;
    }
    localStorage.setItem('bizlyAdminBusinessOverride', String(businessId));
    if (businessName) {
      localStorage.setItem('bizlyAdminBusinessOverrideName', businessName);
    }
  }
  getAdminBusinessContext(): number | null {
    const v = localStorage.getItem('bizlyAdminBusinessOverride');
    if (!v) return null;
    const n = Number(v);
    return Number.isFinite(n) ? n : null;
  }
  getAdminBusinessContextName(): string | null {
    return localStorage.getItem('bizlyAdminBusinessOverrideName');
  }
  clearAdminContext(): void {
    localStorage.removeItem('bizlyAdminBusinessOverride');
    localStorage.removeItem('bizlyAdminBusinessOverrideName');
  }
  /** True when the cached role level is ADMIN — used to show/hide admin-only UI. */
  isSystemAdmin(): boolean {
    const lvl = this.getCachedRoleLevel();
    return !!lvl && lvl.toUpperCase() === 'ADMIN';
  }

  /** Business tenant (has business_id, not portal ADMIN). Uses team-role APIs for roles. */
  isBusinessTenant(): boolean {
    return this.getCachedBusinessId() != null && !this.isSystemAdmin();
  }

  completeWelcome() {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      Accept: 'application/json',
      'X-DEVICE-ID': this.deviceIdService.getDeviceId(),
    });
    return this.http
      .post<ApiResponse<RegisterBusinessResponse>>(
        GlobalConstants.API_ENDPOINTS.auth.welcomeComplete, {}, { headers })
      .pipe(tap((response) => this.persistSession(response?.data?.session)));
  }

  /**
   * Exchange a provider-issued token for a Bizly session. The backend verifies the token with
   * the provider before issuing the JWT, so a forged client-side token cannot grant access.
   */
  socialLogin(
    provider: 'google',
    idToken: string | null,
    accessToken: string | null,
    rememberDevice: boolean
  ): Observable<ApiResponse<LoginPayload>> {
    return from(this.deviceIdService.getOrCreateDeviceId(rememberDevice)).pipe(
      switchMap((deviceId) => {
        const headers = new HttpHeaders({
          'Content-Type': 'application/json',
          Accept: 'application/json',
          'X-DEVICE-ID': deviceId,
        });
        const body: Record<string, string | null> = {};
        if (idToken) body['idToken'] = idToken;
        if (accessToken) body['accessToken'] = accessToken;
        return this.http
          .post<ApiResponse<LoginPayload>>(
            GlobalConstants.API_ENDPOINTS.auth.social(provider),
            body,
            { headers }
          )
          .pipe(tap((response) => this.persistSession(response?.data)));
      })
    );
  }
  

  private async encryptPassword(password: string): Promise<string> {
    const certificateDer = this.pemToArrayBuffer(this.publicCertificatePem);
    const publicKeySpki = this.extractSpkiFromCertificate(certificateDer);

    const key = await globalThis.crypto.subtle.importKey(
      'spki',
      publicKeySpki,
      {
        name: 'RSA-OAEP',
        hash: 'SHA-256',
      },
      false,
      ['encrypt']
    );

    const encrypted = await globalThis.crypto.subtle.encrypt(
      { name: 'RSA-OAEP' },
      key,
      new TextEncoder().encode(password)
    );

    return this.arrayBufferToBase64(encrypted);
  }

  private pemToArrayBuffer(pem: string): ArrayBuffer {
    const base64 = pem
      .replace('-----BEGIN CERTIFICATE-----', '')
      .replace('-----END CERTIFICATE-----', '')
      .replace(/\s/g, '');

    const raw = atob(base64);
    const bytes = new Uint8Array(raw.length);
    for (let i = 0; i < raw.length; i += 1) {
      bytes[i] = raw.charCodeAt(i);
    }
    return bytes.buffer;
  }

  // Minimal DER parser to extract SubjectPublicKeyInfo from X.509 certificate.
  private extractSpkiFromCertificate(certDer: ArrayBuffer): ArrayBuffer {
    const certBytes = new Uint8Array(certDer);
    const readTlv = (start: number) => {
      const tag = certBytes[start];
      let lengthByte = certBytes[start + 1];
      let length = 0;
      let lengthBytes = 1;

      if ((lengthByte & 0x80) === 0) {
        length = lengthByte;
      } else {
        const count = lengthByte & 0x7f;
        lengthBytes = 1 + count;
        for (let i = 0; i < count; i += 1) {
          length = (length << 8) | certBytes[start + 2 + i];
        }
      }

      const headerLength = 1 + lengthBytes;
      const valueStart = start + headerLength;

      return {
        tag,
        start,
        valueStart,
        valueLength: length,
        totalLength: headerLength + length,
      };
    };

    const certSeq = readTlv(0);
    if (certSeq.tag !== 0x30) {
      throw new Error('Invalid certificate format.');
    }

    const tbsSeq = readTlv(certSeq.valueStart);
    if (tbsSeq.tag !== 0x30) {
      throw new Error('Invalid certificate format.');
    }

    let cursor = tbsSeq.valueStart;
    if (certBytes[cursor] === 0xa0) {
      const version = readTlv(cursor);
      cursor += version.totalLength;
    }

    const serial = readTlv(cursor);
    cursor += serial.totalLength;

    const signature = readTlv(cursor);
    cursor += signature.totalLength;

    const issuer = readTlv(cursor);
    cursor += issuer.totalLength;

    const validity = readTlv(cursor);
    cursor += validity.totalLength;

    const subject = readTlv(cursor);
    cursor += subject.totalLength;

    const spki = readTlv(cursor);
    if (spki.tag !== 0x30) {
      throw new Error('Invalid certificate format.');
    }

    return certBytes.slice(spki.start, spki.start + spki.totalLength).buffer;
  }

  private arrayBufferToBase64(buffer: ArrayBuffer): string {
    const bytes = new Uint8Array(buffer);
    let binary = '';
    bytes.forEach((byte) => {
      binary += String.fromCharCode(byte);
    });
    return btoa(binary);
  }
}
