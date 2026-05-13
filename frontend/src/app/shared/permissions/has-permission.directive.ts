import {
  Directive,
  EmbeddedViewRef,
  Input,
  OnDestroy,
  OnInit,
  TemplateRef,
  ViewContainerRef,
} from '@angular/core';
import { Subscription } from 'rxjs';
import { MenuPermAction, MenuPermissionService } from 'src/app/services/menu-permission.service';

type PermInput = string | ReadonlyArray<string> | null | undefined;

/**
 * Structural directive that renders its template only when the current role's JWT permission matrix
 * grants the requested action on the configured route(s).
 *
 * Examples:
 * <pre>
 * &lt;!-- Hide the Services tab unless the role has view on /bm/services --&gt;
 * &lt;a routerLink="/bm/services" *hasPermission="'/bm/services'"&gt;Services&lt;/a&gt;
 *
 * &lt;!-- Action button: hide unless role has 'add' on the same screen --&gt;
 * &lt;button *hasPermission="'/bm/appointments'; action: 'add'"&gt;New&lt;/button&gt;
 *
 * &lt;!-- Wrap a whole section: needs ALL routes by default --&gt;
 * &lt;div *hasPermission="['/bm/services','/bm/appointments']; mode: 'any'"&gt;...&lt;/div&gt;
 *
 * &lt;!-- Loose mode: shows the element when the route isn't in the matrix at all
 *      (legacy fallback — useful for new modules until UM is configured) --&gt;
 * &lt;button *hasPermission="'/bm/services'; action: 'edit'; strict: false"&gt;Edit&lt;/button&gt;
 * </pre>
 *
 * Permissions come entirely from the JWT, so the directive re-evaluates after login / role switch
 * via {@link MenuPermissionService.changes$}.
 */
@Directive({ selector: '[hasPermission]' })
export class HasPermissionDirective implements OnInit, OnDestroy {
  private routes: string[] = [];
  private action: MenuPermAction = 'view';
  private mode: 'all' | 'any' = 'all';
  private strict = true;

  private viewRef: EmbeddedViewRef<unknown> | null = null;
  private sub?: Subscription;

  constructor(
    private readonly tpl: TemplateRef<unknown>,
    private readonly vcr: ViewContainerRef,
    private readonly perm: MenuPermissionService
  ) {}

  /** Route or list of routes the host element gates on. */
  @Input() set hasPermission(value: PermInput) {
    this.routes = this.normalize(value);
    this.update();
  }

  /** Action to check; defaults to {@code 'view'}. */
  @Input() set hasPermissionAction(value: MenuPermAction | null | undefined) {
    this.action = (value ?? 'view') as MenuPermAction;
    this.update();
  }

  /** Combine multiple routes with {@code 'all'} (default) or {@code 'any'}. */
  @Input() set hasPermissionMode(value: 'all' | 'any' | null | undefined) {
    this.mode = value === 'any' ? 'any' : 'all';
    this.update();
  }

  /**
   * When {@code true} (default), a route that isn't listed in the JWT matrix is treated as denied —
   * the element is hidden. Set to {@code false} to fall back to "show until UM blocks it server-side"
   * (legacy {@link MenuPermissionService.canIfListedOrAllow} behaviour).
   */
  @Input() set hasPermissionStrict(value: boolean | null | undefined) {
    this.strict = value !== false;
    this.update();
  }

  ngOnInit(): void {
    this.sub = this.perm.changes$.subscribe(() => this.update());
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }

  private normalize(v: PermInput): string[] {
    if (v == null) {
      return [];
    }
    if (Array.isArray(v)) {
      return v.map((x) => String(x)).filter((x) => x.length > 0);
    }
    const s = String(v).trim();
    return s ? [s] : [];
  }

  private update(): void {
    const allow = this.evaluate();
    if (allow && !this.viewRef) {
      this.viewRef = this.vcr.createEmbeddedView(this.tpl);
    } else if (!allow && this.viewRef) {
      this.vcr.clear();
      this.viewRef = null;
    }
  }

  private evaluate(): boolean {
    if (this.routes.length === 0) {
      return false;
    }
    const check = (r: string) =>
      this.strict ? this.perm.can(r, this.action) : this.perm.canIfListedOrAllow(r, this.action);
    return this.mode === 'any' ? this.routes.some(check) : this.routes.every(check);
  }
}
