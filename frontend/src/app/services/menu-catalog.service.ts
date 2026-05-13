import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, Subscription, firstValueFrom } from 'rxjs';
import { Router } from '@angular/router';
import { BusinessApiService } from './business-api.service';
import { AuthService } from './auth.service';
import { GlobalConstants } from '../common/GlobalConstants';
import { NavGroupItem, NavItem } from '../layouts/full/sidebar/nav-item/nav-item';

/**
 * Flat view of one menu the current user can reach. We keep the original
 * group label so search results can show e.g. "Roles · User Management" and
 * the favorites dropdown can render an icon.
 */
export interface FlatMenu {
  id?: number;
  name: string;
  route: string;
  icon?: string;
  groupName?: string;
}

/**
 * Single source of truth for "what menus can the current user reach?" — used by
 * the sidebar, the header search box, and the favorites dropdown. We keep it
 * here (rather than letting each consumer fetch /um/menu/gets independently)
 * so a role switch reloads the list once, and every dependent UI updates in
 * lockstep.
 *
 * The backend already enforces permissions on /um/menu/gets, so anything we
 * cache here is by definition something this user is allowed to navigate to.
 */
@Injectable({ providedIn: 'root' })
export class MenuCatalogService {
  private readonly groupsSubject = new BehaviorSubject<NavGroupItem[]>([]);
  private readonly flatSubject = new BehaviorSubject<FlatMenu[]>([]);
  private readonly loadingSubject = new BehaviorSubject<boolean>(false);

  /** Hot observable — replays the latest snapshot to late subscribers. */
  readonly groups$: Observable<NavGroupItem[]> = this.groupsSubject.asObservable();
  readonly flat$: Observable<FlatMenu[]> = this.flatSubject.asObservable();
  readonly loading$: Observable<boolean> = this.loadingSubject.asObservable();

  private inflight: Promise<NavGroupItem[]> | null = null;
  private roleRefreshSub?: Subscription;

  constructor(
    private readonly api: BusinessApiService,
    private readonly authService: AuthService,
    private readonly router: Router
  ) {
    this.roleRefreshSub = this.authService.roleRefresh$.subscribe(() => {
      this.reload();
    });
  }

  /** Returns the cached list, fetching on the first call. */
  async ensureLoaded(): Promise<NavGroupItem[]> {
    if (this.groupsSubject.value.length > 0) {
      return this.groupsSubject.value;
    }
    return this.reload();
  }

  reload(): Promise<NavGroupItem[]> {
    if (this.inflight) {
      return this.inflight;
    }
    this.loadingSubject.next(true);
    this.inflight = firstValueFrom(
      this.api.post<NavGroupItem[]>(GlobalConstants.API_ENDPOINTS.application.getAll, {})
    )
      .then((groups) => {
        const safe = Array.isArray(groups) ? groups : [];
        this.groupsSubject.next(safe);
        this.flatSubject.next(this.flatten(safe));
        return safe;
      })
      .catch((err) => {
        console.error('[MENU_CATALOG] failed to load /um/menu/gets', err);
        this.groupsSubject.next([]);
        this.flatSubject.next([]);
        return [];
      })
      .finally(() => {
        this.loadingSubject.next(false);
        this.inflight = null;
      });
    return this.inflight;
  }

  /**
   * Case-insensitive substring search across menu name and route. Returns up
   * to `limit` matches sorted by best-match heuristic (prefix > infix).
   *
   * When the query is empty, returns the first `limit` accessible menus
   * sorted alphabetically — so the autocomplete still has something to show
   * the moment the user clicks the input, before they've typed a character.
   */
  search(query: string, limit = 8): FlatMenu[] {
    const flat = this.flatSubject.value;
    const q = (query ?? '').trim().toLowerCase();
    if (!q) {
      return flat
        .slice()
        .sort((a, b) => a.name.localeCompare(b.name))
        .slice(0, limit);
    }
    const scored: { item: FlatMenu; score: number }[] = [];
    for (const m of flat) {
      const name = (m.name ?? '').toLowerCase();
      const route = (m.route ?? '').toLowerCase();
      const grp = (m.groupName ?? '').toLowerCase();
      let score = 0;
      if (name.startsWith(q)) score = 100;
      else if (name.includes(q)) score = 60;
      else if (route.includes(q)) score = 40;
      else if (grp.includes(q)) score = 20;
      if (score > 0) {
        scored.push({ item: m, score });
      }
    }
    scored.sort((a, b) => b.score - a.score || a.item.name.localeCompare(b.item.name));
    return scored.slice(0, limit).map((s) => s.item);
  }

  findByRoute(route: string | null | undefined): FlatMenu | undefined {
    if (!route) return undefined;
    const t = route.trim();
    return this.flatSubject.value.find(
      (m) => m.route === t || m.route.toLowerCase() === t.toLowerCase()
    );
  }

  /** Resolves {@code UM_MENUS.id} → {@code route} from the last loaded catalog (same payload as JWT enrichment). */
  findRouteByMenuId(menuId: number): string | undefined {
    const hit = this.flatSubject.value.find((m) => m.id != null && Number(m.id) === Number(menuId));
    const r = hit?.route?.trim();
    return r && r.length > 0 ? r : undefined;
  }

  /** Navigates to a menu route, no-op if it can't be reached anymore. */
  navigateTo(route: string | null | undefined): void {
    const found = this.findByRoute(route);
    if (!found) return;
    this.router.navigateByUrl(found.route);
  }

  private flatten(groups: NavGroupItem[]): FlatMenu[] {
    const out: FlatMenu[] = [];
    const seen = new Set<string>();
    for (const g of groups) {
      const groupName = g.name;
      for (const m of g.menus ?? []) {
        this.collect(m, groupName, out, seen);
      }
    }
    return out;
  }

  private collect(
    item: NavItem,
    groupName: string | undefined,
    acc: FlatMenu[],
    seen: Set<string>
  ): void {
    if (item.route && !item.disabled && !seen.has(item.route)) {
      seen.add(item.route);
      acc.push({
        id: item.id,
        name: item.name ?? item.route,
        route: item.route,
        icon: item.icon,
        groupName,
      });
    }
    for (const child of item.menus ?? []) {
      this.collect(child, groupName, acc, seen);
    }
  }
}
