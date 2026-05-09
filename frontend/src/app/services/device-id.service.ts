import { Injectable } from '@angular/core';

/**
 * Browser environments cannot read true hardware identifiers (IMEI, Android ID,
 * iOS identifierForVendor, etc.) for privacy and security. Chromium-based browsers
 * (Chrome on Android/desktop) expose User-Agent Client Hints; we use those plus a
 * stored random suffix so the value is stable per browser profile and reasonably descriptive.
 */
@Injectable({
  providedIn: 'root',
})
export class DeviceIdService {
  static readonly STORAGE_KEY = 'deviceId';

  private static readonly HINT_TIMEOUT_MS = 2500;

  /**
   * Run from APP_INITIALIZER so the first HTTP call already has a persisted id.
   */
  init(): Promise<void> {
    return Promise.resolve();
  }

  getDeviceId(): string {
    if (typeof localStorage === 'undefined' || typeof sessionStorage === 'undefined') {
      return this.fallbackEphemeralId();
    }
    const stored = localStorage.getItem(DeviceIdService.STORAGE_KEY);
    if (stored) {
      return stored;
    }
    const sessionStored = sessionStorage.getItem(DeviceIdService.STORAGE_KEY);
    if (sessionStored) {
      return sessionStored;
    }
    const created = this.buildIdSync();
    localStorage.setItem(DeviceIdService.STORAGE_KEY, created);
    return created;
  }

  async getOrCreateDeviceId(rememberDevice: boolean): Promise<string> {
    if (typeof localStorage === 'undefined' || typeof sessionStorage === 'undefined') {
      return this.fallbackEphemeralId();
    }

    const local = localStorage.getItem(DeviceIdService.STORAGE_KEY);
    const session = sessionStorage.getItem(DeviceIdService.STORAGE_KEY);
    const existing = local ?? session;
    const id = existing ?? (await this.buildIdWithClientHints());

    if (rememberDevice) {
      localStorage.setItem(DeviceIdService.STORAGE_KEY, id);
      sessionStorage.removeItem(DeviceIdService.STORAGE_KEY);
    } else {
      sessionStorage.setItem(DeviceIdService.STORAGE_KEY, id);
      localStorage.removeItem(DeviceIdService.STORAGE_KEY);
    }

    return id;
  }

  private async buildIdWithClientHints(): Promise<string> {
    const uuid = globalThis.crypto?.randomUUID?.() ?? `u-${Date.now()}-${Math.random()}`;
    const uad = this.getUserAgentData();

    const mobile = uad?.mobile === true ? 'mobile' : 'desktop';
    const platform = this.slug(uad?.platform || navigator.platform || 'unknown', 48);

    let model = '';
    let platformVersion = '';

    if (uad?.getHighEntropyValues) {
      try {
        const hints = await Promise.race([
          uad.getHighEntropyValues(['model', 'platformVersion', 'architecture']),
          new Promise<never>((_, reject) =>
            setTimeout(() => reject(new Error('client-hints-timeout')), DeviceIdService.HINT_TIMEOUT_MS)
          ),
        ]);
        if (hints && typeof hints === 'object') {
          const h = hints as Record<string, string | undefined>;
          if (h['model']) {
            model = this.slug(String(h['model']), 64);
          }
          if (h['platformVersion']) {
            platformVersion = this.slug(String(h['platformVersion']), 24);
          }
        }
      } catch {
        // Low-entropy or timeout — still produce a valid id
      }
    }

    const parts = ['bizly', mobile, platform, model || 'nomodel', platformVersion || 'na', uuid];
    return parts.join('|');
  }

  private buildIdSync(): string {
    const uuid = globalThis.crypto?.randomUUID?.() ?? `u-${Date.now()}-${Math.random()}`;
    const uad = this.getUserAgentData();
    const mobile = uad?.mobile === true ? 'mobile' : 'desktop';
    const platform = this.slug(uad?.platform || navigator.platform || 'unknown', 48);
    return ['bizly', mobile, platform, 'nomodel', 'na', uuid].join('|');
  }

  private getUserAgentData():
    | {
        mobile?: boolean;
        platform?: string;
        getHighEntropyValues?: (keys: string[]) => Promise<unknown>;
      }
    | undefined {
    return (navigator as Navigator & { userAgentData?: { mobile?: boolean; platform?: string; getHighEntropyValues?: (keys: string[]) => Promise<unknown> } })
      .userAgentData;
  }

  private slug(value: string, maxLen: number): string {
    const s = value
      .trim()
      .replace(/[\s|]+/g, '_')
      .replace(/[^a-zA-Z0-9._-]/g, '-')
      .replace(/-+/g, '-')
      .replace(/^-|-$/g, '');
    return s.slice(0, maxLen) || 'x';
  }

  private fallbackEphemeralId(): string {
    return `bizly|ephemeral|${Date.now()}|${Math.random()}`;
  }
}
