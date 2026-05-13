import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, firstValueFrom } from 'rxjs';
import { GlobalConstants } from '../common/GlobalConstants';
import { BusinessApiService } from './business-api.service';
import { FlatMenu, MenuCatalogService } from './menu-catalog.service';

/** What the backend sends back for /list. */
interface UserFavoritesResponseDto {
  items: UserFavoriteItemDto[];
  defaultRoute: string | null;
}

interface UserFavoriteItemDto {
  menuRoute: string;
  pinOrder: number;
  isDefault: boolean;
}

/** What the header dropdown renders for each pinned menu. */
export interface FavoriteView extends FlatMenu {
  isDefault: boolean;
  pinOrder: number;
  accessible: boolean;
}

const ENDPOINTS = GlobalConstants.API_ENDPOINTS.settings.userFavorites;

/**
 * Owns the user's pinned-menus state. Pairs each starred route with metadata
 * pulled from {@link MenuCatalogService} (icon, display name, group) so the
 * UI can render rich rows without doing the join itself.
 *
 * Favorites whose route the user can no longer reach are kept on the server
 * (so re-granted access restores them) but flagged {@code accessible=false}
 * so the dropdown can grey them out / hide them.
 */
@Injectable({ providedIn: 'root' })
export class UserFavoritesService {
  private readonly favoritesSubject = new BehaviorSubject<FavoriteView[]>([]);
  private readonly defaultRouteSubject = new BehaviorSubject<string | null>(null);
  private readonly loadingSubject = new BehaviorSubject<boolean>(false);

  /** All pinned items, ordered by pin_order (asc). */
  readonly favorites$: Observable<FavoriteView[]> = this.favoritesSubject.asObservable();
  readonly defaultRoute$: Observable<string | null> = this.defaultRouteSubject.asObservable();
  readonly loading$: Observable<boolean> = this.loadingSubject.asObservable();

  constructor(
    private readonly api: BusinessApiService,
    private readonly menuCatalog: MenuCatalogService
  ) {
    // When the menu list changes (initial load or role switch), re-decorate
    // the existing favorites so accessibility flags / display names stay in
    // sync without a server round trip.
    this.menuCatalog.flat$.subscribe(() => this.redecorate());
  }

  /** Loads /list once, then decorates with menu-catalog data. */
  async refresh(): Promise<void> {
    this.loadingSubject.next(true);
    try {
      // Make sure the menu catalog is hydrated before we decorate.
      await this.menuCatalog.ensureLoaded();
      const res = await firstValueFrom(
        this.api.postEnvelope<UserFavoritesResponseDto>(ENDPOINTS.list, {}, 'silent')
      );
      this.lastServerItems = (res?.items ?? []).slice();
      this.defaultRouteSubject.next(res?.defaultRoute ?? null);
      this.redecorate();
    } catch (err) {
      console.warn('[USER_FAV] failed to load favorites', err);
      this.lastServerItems = [];
      this.favoritesSubject.next([]);
      this.defaultRouteSubject.next(null);
    } finally {
      this.loadingSubject.next(false);
    }
  }

  isStarred(route: string | null | undefined): boolean {
    if (!route) return false;
    return this.favoritesSubject.value.some((f) => f.route === route);
  }

  /**
   * Toggles a star. Optimistically updates local state, then calls the server.
   * Reverts on failure so the UI doesn't lie about persisted state.
   */
  async toggle(route: string): Promise<void> {
    if (!route) return;
    const isStarred = this.isStarred(route);
    if (isStarred) {
      await this.remove(route);
    } else {
      await this.add(route);
    }
  }

  async add(route: string): Promise<void> {
    if (!route || this.isStarred(route)) return;
    try {
      await firstValueFrom(
        this.api.postEnvelope<void>(ENDPOINTS.add, { route }, 'errors')
      );
      await this.refresh();
    } catch (err) {
      console.warn('[USER_FAV] add failed', err);
    }
  }

  async remove(route: string): Promise<void> {
    if (!route) return;
    try {
      await firstValueFrom(
        this.api.postEnvelope<void>(ENDPOINTS.remove, { route }, 'errors')
      );
      // If we just removed the default, the server already cleared it; mirror
      // that in local state so the UI updates without a second round trip.
      if (this.defaultRouteSubject.value === route) {
        this.defaultRouteSubject.next(null);
      }
      await this.refresh();
    } catch (err) {
      console.warn('[USER_FAV] remove failed', err);
    }
  }

  /** Pass {@code route = null} to clear any existing default. */
  async setDefault(route: string | null): Promise<void> {
    try {
      await firstValueFrom(
        this.api.postEnvelope<void>(ENDPOINTS.setDefault, { route: route ?? null }, 'errors')
      );
      await this.refresh();
    } catch (err) {
      console.warn('[USER_FAV] set-default failed', err);
    }
  }

  /**
   * Fetches just the default route — used by the post-login flow so we don't
   * pay for hydrating the menu catalog on the login screen.
   */
  async fetchDefaultRoute(): Promise<string | null> {
    try {
      const res = await firstValueFrom(
        this.api.postEnvelope<{ route: string | null }>(ENDPOINTS.defaultRoute, {}, 'silent')
      );
      return res?.route ?? null;
    } catch (err) {
      console.warn('[USER_FAV] fetch-default failed', err);
      return null;
    }
  }

  /** Cached raw rows so re-decorate doesn't have to re-fetch. */
  private lastServerItems: UserFavoriteItemDto[] = [];

  private redecorate(): void {
    const defaultRoute = this.defaultRouteSubject.value;
    const decorated: FavoriteView[] = this.lastServerItems.map((row) => {
      const menu = this.menuCatalog.findByRoute(row.menuRoute);
      const isDefault = defaultRoute != null && row.menuRoute === defaultRoute;
      return {
        id: menu?.id,
        name: menu?.name ?? row.menuRoute,
        route: row.menuRoute,
        icon: menu?.icon,
        groupName: menu?.groupName,
        pinOrder: row.pinOrder,
        isDefault,
        accessible: !!menu,
      };
    });
    decorated.sort((a, b) => a.pinOrder - b.pinOrder);
    this.favoritesSubject.next(decorated);
  }

  /** Convenience: clear local cache on logout. */
  reset(): void {
    this.lastServerItems = [];
    this.favoritesSubject.next([]);
    this.defaultRouteSubject.next(null);
  }
}
