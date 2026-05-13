import {
  Component,
  ElementRef,
  HostListener,
  OnDestroy,
  OnInit,
  ViewChild,
} from '@angular/core';
import { FormControl } from '@angular/forms';
import { Subject, debounceTime, distinctUntilChanged, switchMap, takeUntil, tap } from 'rxjs';

import {
  AdminBusiness,
  AuthService,
} from 'src/app/services/auth.service';

/**
 * SUPER_ADMIN "global context switcher".
 *
 * Visible only when the cached role-level is {@code ADMIN}. The admin picks any business
 * in the system; the choice is persisted in localStorage and the HTTP interceptor sends
 * it as {@code X-Business-Override} on every downstream API call. The downstream
 * InternalAuthFilter only honours that header when the caller's signed role-level is
 * {@code ADMIN}, so a tampered SPA cannot escalate.
 *
 * Why a header switcher (not per-screen filters)?
 *   - Single source of truth — dashboards, reports, lists, builders all see the same
 *     business automatically.
 *   - No drift between screens that forgot to wire the filter.
 *   - Audit-friendly: every API call carries the impersonated business id in the
 *     header so server-side logs can attribute every action correctly.
 */
@Component({
  selector: 'app-global-context-switcher',
  templateUrl: './global-context-switcher.component.html',
  styleUrls: ['./global-context-switcher.component.scss'],
})
export class GlobalContextSwitcherComponent implements OnInit, OnDestroy {
  readonly searchControl = new FormControl<string>('', { nonNullable: true });

  results: AdminBusiness[] = [];
  isLoading = false;
  open = false;

  /** Currently impersonated business — null when admin is operating in their own context. */
  selectedId: number | null = null;
  selectedName: string | null = null;

  /**
   * Inline style for the floating panel. Uses {@code position: fixed} with a computed
   * {@code left} so the panel never sits under the left drawer (right-aligning only
   * with {@code right} let the panel extend under the sidenav). {@code z-index} in SCSS
   * is above Material drawers.
   */
  panelStyle: { top: string; left: string; width: string } = { top: '0px', left: '0px', width: '320px' };

  @ViewChild('trigger', { static: false }) trigger?: ElementRef<HTMLButtonElement>;

  private destroy$ = new Subject<void>();

  constructor(
    private readonly authService: AuthService,
    private readonly host: ElementRef<HTMLElement>
  ) {}

  ngOnInit(): void {
    this.selectedId = this.authService.getAdminBusinessContext();
    this.selectedName = this.authService.getAdminBusinessContextName();

    this.searchControl.valueChanges
      .pipe(
        debounceTime(180),
        distinctUntilChanged(),
        tap((q) => (this.isLoading = (q ?? '').trim().length >= 1)),
        switchMap((q) => {
          const needle = (q ?? '').trim();
          if (needle.length < 1) {
            this.results = [];
            this.isLoading = false;
            return [];
          }
          return this.authService.adminSearchBusinesses(needle, 12);
        }),
        takeUntil(this.destroy$)
      )
      .subscribe({
        next: (resp) => {
          this.results = resp?.data ?? [];
          this.isLoading = false;
        },
        error: () => {
          this.isLoading = false;
        },
      });
  }

  /** Only visible to system admins. */
  shouldShow(): boolean {
    return this.authService.isSystemAdmin();
  }

  togglePanel(): void {
    this.open = !this.open;
    if (this.open) {
      // Refresh selected display name from cache in case it changed elsewhere.
      this.selectedId = this.authService.getAdminBusinessContext();
      this.selectedName = this.authService.getAdminBusinessContextName();
      // Position the floating panel under the trigger AFTER the *ngIf renders it.
      requestAnimationFrame(() => this.repositionPanel());
    }
  }

  /**
   * Recomputes the fixed-position coordinates of the panel so it visually hangs under the
   * trigger button. Called on open, window resize, and scroll. Right-anchored so the panel
   * never falls off the viewport on narrow screens.
   */
  private repositionPanel(): void {
    const el = this.trigger?.nativeElement;
    if (!el) {
      return;
    }
    const rect = el.getBoundingClientRect();
    const gap = 8;
    const vw = window.innerWidth;
    const panelW = Math.min(320, vw - 2 * gap);

    const sidebar = document.querySelector('mat-sidenav.sidebarNav') as HTMLElement | null;
    const sbRect = sidebar?.getBoundingClientRect();
    const minLeft =
      sbRect != null && sbRect.width > 4 ? Math.round(sbRect.right + gap) : gap;

    let left = Math.round(rect.right - panelW);
    left = Math.max(minLeft, left);
    left = Math.min(left, vw - panelW - gap);

    this.panelStyle = {
      top: `${Math.round(rect.bottom + gap)}px`,
      left: `${Math.max(gap, left)}px`,
      width: `${panelW}px`,
    };
  }

  @HostListener('window:resize')
  @HostListener('window:scroll')
  onViewportChange(): void {
    if (this.open) {
      this.repositionPanel();
    }
  }

  /** Close when clicking anywhere outside the trigger / panel. */
  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (!this.open) return;
    const target = event.target as Node | null;
    const triggerEl = this.trigger?.nativeElement;
    const panelEl = this.host.nativeElement.querySelector('.gcs-panel');
    if (
      target &&
      ((triggerEl && triggerEl.contains(target)) || (panelEl && panelEl.contains(target)))
    ) {
      return;
    }
    this.open = false;
  }

  pick(b: AdminBusiness): void {
    this.authService.setAdminBusinessContext(b.id, b.businessName);
    this.selectedId = b.id;
    this.selectedName = b.businessName;
    this.open = false;
    this.searchControl.setValue('');
    this.results = [];
    // Hard reload: many cached pages have already issued API calls without the new
    // override; the simplest and safest reset is to reload the current route.
    setTimeout(() => window.location.reload(), 100);
  }

  clear(): void {
    this.authService.clearAdminContext();
    this.selectedId = null;
    this.selectedName = null;
    this.open = false;
    setTimeout(() => window.location.reload(), 100);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
