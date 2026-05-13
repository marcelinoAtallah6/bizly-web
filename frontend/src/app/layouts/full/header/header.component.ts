import {
  Component,
  Output,
  EventEmitter,
  Input,
  ViewEncapsulation,
  OnInit,
  OnDestroy,
} from '@angular/core';
import { FormControl } from '@angular/forms';
import { Observable, Subscription, combineLatest, finalize } from 'rxjs';
import { map, startWith } from 'rxjs/operators';
import { MatDialog } from '@angular/material/dialog';
import { MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { NavigationEnd, Router } from '@angular/router';
import { NavItem } from '../sidebar/nav-item/nav-item';
import { SharedService } from 'src/app/services/shared.service';
import { AuthService } from 'src/app/services/auth.service';
import { MenuPermissionService } from 'src/app/services/menu-permission.service';
import { NavbarProfileView, UserProfileService } from 'src/app/services/user-profile.service';
import {
  NotifInboxItem,
  NotifInboxService,
  NotifSeverity,
} from 'src/app/services/notif-inbox.service';
import { HeaderPulse, HeaderPulseService } from 'src/app/services/header-pulse.service';
import { FlatMenu, MenuCatalogService } from 'src/app/services/menu-catalog.service';
import { FavoriteView, UserFavoritesService } from 'src/app/services/user-favorites.service';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  encapsulation: ViewEncapsulation.None,
})
export class HeaderComponent implements OnInit, OnDestroy {
  @Input() showToggle = true;
  @Input() toggleChecked = false;
  @Output() toggleMobileNav = new EventEmitter<void>();
  @Output() toggleMobileFilterNav = new EventEmitter<void>();
  @Output() toggleCollapsed = new EventEmitter<void>();

  showFiller = false;
  isSigningOut = false;
  headerProf: NavbarProfileView | null = null;

  notifications: NotifInboxItem[] = [];
  notifUnread = 0;
  notifHasMore = false;
  notifLoadingMore = false;
  pulse: HeaderPulse | null = null;

  /**
   * Permission keys used by the pulse ticker and other cross-feature widgets in the header. The
   * pulse tiles use {@code *hasPermission} so they vanish automatically when the role loses access
   * to the underlying module — no central registry required.
   */
  readonly routeAppointments = '/bm/appointments';
  readonly routeBmServices = '/bm/services';
  readonly routeProducts = '/pm/products';
  /** Sales tile aggregates both PM and BM activity — keep it as long as either feed is visible. */
  readonly routesSalesAny: ReadonlyArray<string> = ['/pm/products', '/bm/services'];

  // -- Search ----------------------------------------------------------------
  /**
   * The search input is permission-aware by construction: the only data it
   * filters is the list of menus the backend has already approved for this
   * user (via /um/menu/gets in {@link MenuCatalogService}). No extra server
   * round trip per keystroke.
   */
  readonly searchControl = new FormControl<string>('', { nonNullable: true });
  filteredMenus$!: Observable<FlatMenu[]>;

  // -- Favorites -------------------------------------------------------------
  favorites: FavoriteView[] = [];
  defaultFavRoute: string | null = null;
  /** Live URL of the page the user is on, used to decide if "Star this page" is allowed. */
  private currentRoute: string = '';

  /*
   * Header-level navigation entries. Intentionally empty — primary navigation lives in
   * the sidebar. The header focuses on identity (role chip), at-a-glance health (KPI
   * ticker), and time-sensitive signals (notifications bell + profile).
   */
  menuItems: NavItem[] = [];

  private subs = new Subscription();

  constructor(
    public dialog: MatDialog,
    public router: Router,
    private sharedService: SharedService,
    private authService: AuthService,
    private readonly userProfile: UserProfileService,
    private readonly notifInbox: NotifInboxService,
    private readonly headerPulse: HeaderPulseService,
    private readonly menuCatalog: MenuCatalogService,
    private readonly favoritesService: UserFavoritesService,
    private readonly menuPerm: MenuPermissionService
  ) {}

  /** True when at least one pulse tile is visible — wraps the whole ticker so it disappears entirely. */
  showPulseTicker(): boolean {
    return (
      this.menuPerm.can(this.routeAppointments, 'view') ||
      this.menuPerm.canAny(this.routesSalesAny, 'view')
    );
  }

  ngOnInit(): void {
    this.userProfile.refresh();
    this.subs.add(
      this.userProfile.profile$.subscribe((p) => {
        this.headerProf = p;
      })
    );

    this.notifInbox.startPolling();
    this.headerPulse.startPolling();

    this.subs.add(this.notifInbox.items$.subscribe((items) => (this.notifications = items)));
    this.subs.add(this.notifInbox.unreadCount$.subscribe((n) => (this.notifUnread = n)));
    this.subs.add(this.notifInbox.hasMore$.subscribe((v) => (this.notifHasMore = v)));
    this.subs.add(this.notifInbox.loadingMore$.subscribe((v) => (this.notifLoadingMore = v)));
    this.subs.add(this.headerPulse.pulse$.subscribe((p) => (this.pulse = p)));

    // -- Menu search: react to keystrokes AND to catalog reloads (role swap)
    // so the dropdown options stay accurate without a manual refresh. ---------
    this.filteredMenus$ = combineLatest([
      this.searchControl.valueChanges.pipe(startWith('')),
      this.menuCatalog.flat$,
    ]).pipe(map(([q]) => this.menuCatalog.search(q ?? '', 10)));

    // Make sure the catalog (and therefore the search index) is loaded.
    void this.menuCatalog.ensureLoaded();

    // -- Favorites: load once and keep view-models in sync. Inaccessible favorites are filtered
    // out so the dropdown only shows screens the current role can actually reach. ------------
    this.subs.add(
      this.favoritesService.favorites$.subscribe(
        (f) => (this.favorites = f.filter((x) => x.accessible))
      )
    );
    this.subs.add(
      this.favoritesService.defaultRoute$.subscribe((r) => (this.defaultFavRoute = r))
    );
    void this.favoritesService.refresh();

    // Track current route so "Star this page" knows what to pin.
    this.currentRoute = this.router.url.split('?')[0];
    this.subs.add(
      this.router.events.subscribe((evt) => {
        if (evt instanceof NavigationEnd) {
          this.currentRoute = evt.urlAfterRedirects.split('?')[0];
        }
      })
    );

    this.menuItems = [];
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  onMenuClick(menuSelected: any) {
    this.router.navigate([menuSelected]);
  }

  // -- Role chip --------------------------------------------------------------

  roleChoices(): string[] {
    return this.authService.getJwtRoleNames();
  }

  displayRoleLabel(code: string): string {
    const u = code.toUpperCase();
    return u.startsWith('ROLE_') ? u.slice(5) : u;
  }

  activeRoleSummary(): string {
    const a = this.authService.getJwtActiveRole();
    if (!a) {
      return 'All roles';
    }
    return this.displayRoleLabel(a);
  }

  hasMultipleRoles(): boolean {
    return this.roleChoices().length > 1;
  }

  switchRole(role: string | null): void {
    this.authService.setActiveRole(role).subscribe({ error: () => {} });
  }

  // -- Notifications ----------------------------------------------------------

  onBellOpened(): void {
    // Refresh the moment the dropdown opens, so the user sees the latest items
    // even between polling cycles.
    this.notifInbox.refresh().subscribe();
  }

  /**
   * Infinite-scroll trigger. Fires on every scroll inside the dropdown list;
   * once the user is within 80px of the bottom we ask the service for the
   * next page. Service guards against duplicate concurrent loads and against
   * loading past the end, so it's safe to spam.
   */
  onNotifListScroll(event: Event): void {
    if (!this.notifHasMore || this.notifLoadingMore) {
      return;
    }
    const el = event.target as HTMLElement;
    if (!el) return;
    const distanceFromBottom = el.scrollHeight - el.scrollTop - el.clientHeight;
    if (distanceFromBottom <= 80) {
      this.notifInbox.loadMore().subscribe();
    }
  }

  /** Explicit "Load more" button fallback (for users who tap rather than scroll). */
  onLoadMoreClick(event?: Event): void {
    event?.stopPropagation();
    if (!this.notifHasMore || this.notifLoadingMore) {
      return;
    }
    this.notifInbox.loadMore().subscribe();
  }

  notifIcon(severity: NotifSeverity | string | undefined | null): string {
    switch ((severity || 'INFO').toString().toUpperCase()) {
      case 'SUCCESS':
        return 'circle-check';
      case 'WARN':
        return 'alert-triangle';
      case 'ERROR':
        return 'alert-octagon';
      default:
        return 'info-circle';
    }
  }

  severityClass(severity: NotifSeverity | string | undefined | null): string {
    return 'sev-' + ((severity || 'INFO').toString().toLowerCase());
  }

  notifBadgeText(): string | null {
    if (this.notifUnread <= 0) {
      return null;
    }
    return this.notifUnread > 99 ? '99+' : String(this.notifUnread);
  }

  onNotificationClick(n: NotifInboxItem): void {
    if (n.unread && n.id != null) {
      this.notifInbox.markRead(n.id);
    }
    if (n.linkRoute) {
      this.router.navigateByUrl(n.linkRoute);
    }
  }

  onMarkAllRead(event?: Event): void {
    event?.stopPropagation();
    this.notifInbox.markAllRead();
  }

  /**
   * Diagnostic helper: publishes a single test notification to the current
   * user and refreshes the list. If nothing appears after clicking this:
   *   - check DevTools → Console for [notif-inbox] FAILED messages,
   *   - check that UM.NOTIF_INBOX exists (run patch-notif-inbox-oracle.sql),
   *   - check the bm service has been restarted with the new controllers.
   */
  onSendTestNotification(event?: Event): void {
    event?.stopPropagation();
    this.notifInbox.seedTest().subscribe();
  }

  /** trackBy for *ngFor — keeps already-rendered DOM nodes stable when new pages append. */
  trackNotifById(_index: number, n: NotifInboxItem): number {
    return n.id;
  }

  /** Human-friendly "now / 3m / 2h / yesterday / Mar 5" relative timestamp. */
  formatRelative(iso: string | null | undefined): string {
    if (!iso) return '';
    const then = new Date(iso).getTime();
    if (Number.isNaN(then)) return '';
    const diffMs = Date.now() - then;
    const diffMin = Math.floor(diffMs / 60_000);
    if (diffMin < 1) return 'just now';
    if (diffMin < 60) return `${diffMin}m ago`;
    const diffHr = Math.floor(diffMin / 60);
    if (diffHr < 24) return `${diffHr}h ago`;
    const diffDay = Math.floor(diffHr / 24);
    if (diffDay === 1) return 'yesterday';
    if (diffDay < 7) return `${diffDay}d ago`;
    return new Date(iso).toLocaleDateString();
  }

  // -- KPI ticker -------------------------------------------------------------

  formatMoney(v: number | null | undefined): string {
    if (v == null) return '—';
    if (Math.abs(v) >= 1000) {
      return `$${(v / 1000).toFixed(v % 1000 === 0 ? 0 : 1)}k`;
    }
    return `$${v.toFixed(0)}`;
  }

  // -- Search ----------------------------------------------------------------

  /** Hidden display function for {@code mat-autocomplete}; we never echo a selected
   * option back into the input (we want the box to stay empty after navigation). */
  displaySearchOption(): string {
    return '';
  }

  onSearchSelected(event: MatAutocompleteSelectedEvent): void {
    const menu = event.option?.value as FlatMenu | undefined;
    if (!menu?.route) {
      return;
    }
    this.searchControl.setValue('');
    this.router.navigateByUrl(menu.route);
  }

  trackMenuByRoute(_i: number, m: FlatMenu): string {
    return m.route;
  }

  /** Returns the icon name to render in a search result row. */
  searchOptionIcon(m: FlatMenu): string {
    return (m.icon && m.icon.trim()) || 'circle-dot';
  }

  // -- Favorites -------------------------------------------------------------

  trackFavByRoute(_i: number, f: FavoriteView): string {
    return f.route;
  }

  /**
   * Whether the current route can be starred:
   *   - it must map to a real (permission-cleared) menu, AND
   *   - it must not already be starred.
   */
  canStarCurrent(): boolean {
    if (!this.currentRoute) return false;
    if (this.favorites.some((f) => f.route === this.currentRoute)) return false;
    return !!this.menuCatalog.findByRoute(this.currentRoute);
  }

  /** Title of the menu that owns the current route, or a fallback. */
  currentMenuLabel(): string {
    const found = this.menuCatalog.findByRoute(this.currentRoute);
    return found?.name || 'this page';
  }

  onStarCurrent(event?: Event): void {
    event?.stopPropagation();
    if (!this.canStarCurrent()) return;
    void this.favoritesService.add(this.currentRoute);
  }

  goFavorite(fav: FavoriteView, event?: Event): void {
    event?.stopPropagation();
    if (!fav.accessible) return;
    this.router.navigateByUrl(fav.route);
  }

  removeFavorite(fav: FavoriteView, event?: Event): void {
    event?.stopPropagation();
    void this.favoritesService.remove(fav.route);
  }

  /** Toggles the "default on login" flag — clicking the already-default unsets it. */
  toggleDefaultFavorite(fav: FavoriteView, event?: Event): void {
    event?.stopPropagation();
    const next = fav.isDefault ? null : fav.route;
    void this.favoritesService.setDefault(next);
  }

  // -- Sign-out (unchanged) ---------------------------------------------------

  onSignOut() {
    if (this.isSigningOut) {
      return;
    }
    this.isSigningOut = true;
    this.authService
      .logout()
      .pipe(finalize(() => (this.isSigningOut = false)))
      .subscribe({
        next: () => {
          this.userProfile.clear();
          this.favoritesService.reset();
          this.router.navigate(['/authentication/login']);
        },
        error: () => {
          this.authService.clearSession();
          this.userProfile.clear();
          this.favoritesService.reset();
          this.router.navigate(['/authentication/login']);
        },
      });
  }
}
