import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, Subject, from, map, switchMap, tap } from 'rxjs';
import { GlobalConstants } from '../common/GlobalConstants';
import { DeviceIdService } from './device-id.service';

interface LoginPayload {
  token: string;
  refreshToken?: string | null;
  sessionId: string;
  availableRoles?: string[];
  activeRole?: string | null;
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
      .pipe(tap((response) => this.persistSession(response?.data)));
  }

  isAuthenticated(): boolean {
    return !!this.getAccessToken();
  }

  /** Decodes JWT access token payload (unverified) for UI; use `userId` for profile load. */
  getAccessTokenClaims(): { userId: number; firstName?: string; lastName?: string } | null {
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
      const p = JSON.parse(json) as Record<string, unknown>;
      const rawId = p['userId'];
      const userId = typeof rawId === 'number' ? rawId : Number(rawId);
      if (!Number.isFinite(userId)) {
        return null;
      }
      return {
        userId,
        firstName: p['firstName'] != null ? String(p['firstName']) : undefined,
        lastName: p['lastName'] != null ? String(p['lastName']) : undefined,
      };
    } catch {
      return null;
    }
  }

  getAccessToken(): string | null {
    return localStorage.getItem('jwtAccessToken');
  }

  getRefreshToken(): string | null {
    return localStorage.getItem('jwtRefreshToken');
  }

  getSessionId(): string | null {
    return localStorage.getItem('jwtSessionId');
  }

  clearSession(): void {
    localStorage.removeItem('jwtAccessToken');
    localStorage.removeItem('jwtRefreshToken');
    localStorage.removeItem('jwtSessionId');
    localStorage.removeItem('bizlyAvailableRoles');
    localStorage.removeItem('bizlyActiveRole');
  }

  /** Role codes from JWT {@code role} claim (all assignments). */
  getJwtRoleNames(): string[] {
    const p = this.decodeAccessPayload();
    if (!p) {
      return [];
    }
    const r = p['role'];
    if (Array.isArray(r)) {
      return r.map((x) => String(x));
    }
    if (r != null && r !== '') {
      return [String(r)];
    }
    return [];
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
