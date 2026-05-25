import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { finalize } from 'rxjs/operators';
import { MenuCatalogService } from 'src/app/services/menu-catalog.service';
import {
  ApplicationCatalogService,
  CatalogApplicationDto,
  CatalogMenuDto,
} from '../../services/application-catalog.service';

type EditorMode = 'application' | 'menu';

interface ParentMenuOption {
  id: number;
  label: string;
}

@Component({
  selector: 'app-application-builder',
  templateUrl: './application-builder.component.html',
  styleUrls: ['./application-builder.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationBuilderComponent implements OnInit {
  loading = false;
  saving = false;
  applications: CatalogApplicationDto[] = [];
  selectedApplication: CatalogApplicationDto | null = null;
  selectedMenu: CatalogMenuDto | null = null;
  editorMode: EditorMode = 'application';
  parentMenuOptions: ParentMenuOption[] = [];
  /** Collapsed by default — expand nodes to load children and avoid rendering huge trees at once. */
  expandedMenuIds = new Set<number>();
  private menuDragActive = false;
  private menuReorderInFlight = false;
  private sidebarReloadTimer: ReturnType<typeof setTimeout> | null = null;

  applicationForm: FormGroup;
  menuForm: FormGroup;

  constructor(
    private readonly catalog: ApplicationCatalogService,
    private readonly menuCatalog: MenuCatalogService,
    private readonly fb: FormBuilder,
    private readonly snack: MatSnackBar,
    private readonly cdr: ChangeDetectorRef
  ) {
    this.applicationForm = this.fb.group({
      id: [null],
      name: ['', Validators.required],
      description: [''],
      icon: [''],
      route: [''],
      isActive: [true],
      sortOrder: [null],
    });
    this.menuForm = this.fb.group({
      id: [null],
      applicationId: [null, Validators.required],
      parentId: [null],
      name: ['', Validators.required],
      route: [''],
      icon: [''],
      isActive: [true],
      sortOrder: [null],
    });
  }

  ngOnInit(): void {
    this.reload();
  }

  get rootMenus(): CatalogMenuDto[] {
    return this.selectedApplication?.menus ?? [];
  }

  trackByMenuId(_index: number, menu: CatalogMenuDto): number {
    return menu.id ?? _index;
  }

  trackByAppId(_index: number, app: CatalogApplicationDto): number {
    return app.id ?? _index;
  }

  isMenuSelected(menu: CatalogMenuDto): boolean {
    return this.selectedMenu?.id != null && menu.id != null && this.selectedMenu.id === menu.id;
  }

  hasChildren(menu: CatalogMenuDto): boolean {
    return (menu.children?.length ?? 0) > 0;
  }

  isMenuExpanded(menu: CatalogMenuDto): boolean {
    return menu.id != null && this.expandedMenuIds.has(menu.id);
  }

  onMenuRowClick(menu: CatalogMenuDto): void {
    if (this.menuDragActive) {
      return;
    }
    this.selectMenu(menu);
  }

  onMenuDragStarted(): void {
    this.menuDragActive = true;
  }

  onMenuDragEnded(): void {
    this.menuDragActive = false;
    this.cdr.markForCheck();
  }

  toggleMenuExpand(menu: CatalogMenuDto, event: Event): void {
    event.stopPropagation();
    if (menu.id == null) {
      return;
    }
    if (this.expandedMenuIds.has(menu.id)) {
      this.expandedMenuIds.delete(menu.id);
    } else {
      this.expandedMenuIds.add(menu.id);
    }
    this.cdr.markForCheck();
  }

  reload(): void {
    this.loading = true;
    this.cdr.markForCheck();
    this.catalog
      .loadCatalog()
      .pipe(finalize(() => {
        this.loading = false;
        this.cdr.markForCheck();
      }))
      .subscribe({
        next: (res) => {
          this.applications = this.normalizeApplications(res.applications ?? []);
          if (this.selectedApplication?.id) {
            this.selectedApplication =
              this.applications.find((a) => a.id === this.selectedApplication?.id) ?? null;
          }
          if (this.selectedMenu?.id && this.selectedApplication) {
            this.selectedMenu = this.findMenuById(this.selectedApplication, this.selectedMenu.id) ?? null;
          }
          this.rebuildParentMenuOptions();
        },
        error: (err) => this.snack.open(err?.message ?? 'Failed to load catalog', 'Close', { duration: 5000 }),
      });
  }

  selectApplication(app: CatalogApplicationDto, event?: Event): void {
    event?.stopPropagation();
    this.selectedApplication = app;
    this.selectedMenu = null;
    this.editorMode = 'application';
    this.expandedMenuIds.clear();
    this.rebuildParentMenuOptions();
    this.patchApplicationForm(app);
    this.cdr.markForCheck();
  }

  newApplication(): void {
    this.selectedApplication = null;
    this.selectedMenu = null;
    this.editorMode = 'application';
    this.expandedMenuIds.clear();
    this.parentMenuOptions = [];
    this.applicationForm.reset({
      id: null,
      name: '',
      description: '',
      icon: '',
      route: '',
      isActive: true,
      sortOrder: null,
    });
    this.cdr.markForCheck();
  }

  selectMenu(menu: CatalogMenuDto, event?: Event): void {
    event?.stopPropagation();
    this.selectedMenu = menu;
    this.editorMode = 'menu';
    this.rebuildParentMenuOptions();
    this.patchMenuForm(menu);
    this.cdr.markForCheck();
  }

  newMenu(): void {
    if (!this.selectedApplication?.id) {
      this.snack.open('Select an application first', 'Close', { duration: 4000 });
      return;
    }
    this.selectedMenu = null;
    this.editorMode = 'menu';
    this.rebuildParentMenuOptions();
    this.menuForm.reset({
      id: null,
      applicationId: this.selectedApplication.id,
      parentId: null,
      name: '',
      route: '',
      icon: '',
      isActive: true,
      sortOrder: null,
    });
    this.cdr.markForCheck();
  }

  onMenuApplicationChange(appId: number | null): void {
    if (appId == null) {
      return;
    }
    this.selectedApplication = this.applications.find((a) => a.id === appId) ?? this.selectedApplication;
    this.expandedMenuIds.clear();
    this.rebuildParentMenuOptions();
    const parentId = this.menuForm.get('parentId')?.value as number | null;
    if (parentId != null && !this.parentMenuOptions.some((p) => p.id === parentId)) {
      this.menuForm.patchValue({ parentId: null });
    }
    this.cdr.markForCheck();
  }

  saveApplication(): void {
    if (this.applicationForm.invalid) {
      this.applicationForm.markAllAsTouched();
      return;
    }
    const v = this.applicationForm.getRawValue();
    this.saving = true;
    this.cdr.markForCheck();
    this.catalog
      .saveApplication({
        id: v.id,
        name: v.name,
        description: v.description || null,
        icon: this.normalizeIcon(v.icon),
        route: this.normalizeRoute(v.route),
        isActive: v.isActive,
        sortOrder: v.sortOrder,
      })
      .pipe(finalize(() => {
        this.saving = false;
        this.cdr.markForCheck();
      }))
      .subscribe({
        next: (res) => {
          this.snack.open('Application saved', 'Close', { duration: 3000 });
          this.reload();
          this.menuCatalog.reload();
          if (res?.id) {
            const saved = this.applications.find((a) => a.id === res.id);
            if (saved) {
              this.selectApplication(saved);
            }
          }
        },
        error: (err) => this.snack.open(err?.message ?? 'Save failed', 'Close', { duration: 5000 }),
      });
  }

  saveMenu(): void {
    if (this.menuForm.invalid) {
      this.menuForm.markAllAsTouched();
      return;
    }
    const v = this.menuForm.getRawValue();
    this.saving = true;
    this.cdr.markForCheck();
    this.catalog
      .saveMenu({
        id: v.id,
        applicationId: v.applicationId,
        parentId: v.parentId || null,
        name: v.name,
        route: this.normalizeRoute(v.route),
        icon: this.normalizeIcon(v.icon),
        isActive: v.isActive,
        sortOrder: v.sortOrder,
      })
      .pipe(finalize(() => {
        this.saving = false;
        this.cdr.markForCheck();
      }))
      .subscribe({
        next: () => {
          this.snack.open('Menu saved', 'Close', { duration: 3000 });
          this.reload();
          this.menuCatalog.reload();
        },
        error: (err) => this.snack.open(err?.message ?? 'Save failed', 'Close', { duration: 5000 }),
      });
  }

  deleteApplicationFromList(app: CatalogApplicationDto, event: Event): void {
    event.stopPropagation();
    if (!app.id) {
      return;
    }
    this.deleteApplicationById(app.id, app.name);
  }

  deleteMenuFromList(menu: CatalogMenuDto, event: Event): void {
    event.stopPropagation();
    if (!menu.id) {
      return;
    }
    this.deleteMenuById(menu.id, menu.name);
  }

  deleteApplication(): void {
    const id = this.applicationForm.get('id')?.value as number | null;
    if (!id) {
      return;
    }
    const name = this.applicationForm.get('name')?.value as string;
    this.deleteApplicationById(id, name);
  }

  deleteMenu(): void {
    const id = this.menuForm.get('id')?.value as number | null;
    if (!id) {
      return;
    }
    const name = this.menuForm.get('name')?.value as string;
    this.deleteMenuById(id, name);
  }

  private deleteApplicationById(id: number, name?: string): void {
    const label = name ? `"${name}"` : 'this application';
    if (!confirm(`Delete ${label} and all its menus? This cannot be undone.`)) {
      return;
    }
    this.saving = true;
    this.cdr.markForCheck();
    this.catalog
      .deleteApplication(id)
      .pipe(finalize(() => {
        this.saving = false;
        this.cdr.markForCheck();
      }))
      .subscribe({
        next: () => {
          this.snack.open('Application deleted', 'Close', { duration: 3000 });
          this.newApplication();
          this.reload();
          this.menuCatalog.reload();
        },
        error: (err) => this.snack.open(err?.message ?? 'Delete failed', 'Close', { duration: 5000 }),
      });
  }

  private deleteMenuById(id: number, name?: string): void {
    const label = name ? `"${name}"` : 'this menu';
    if (!confirm(`Delete ${label}? Role permissions for it will be removed.`)) {
      return;
    }
    this.saving = true;
    this.cdr.markForCheck();
    this.catalog
      .deleteMenu(id)
      .pipe(finalize(() => {
        this.saving = false;
        this.cdr.markForCheck();
      }))
      .subscribe({
        next: () => {
          this.snack.open('Menu deleted', 'Close', { duration: 3000 });
          this.newMenu();
          this.reload();
          this.menuCatalog.reload();
        },
        error: (err) => this.snack.open(err?.message ?? 'Delete failed', 'Close', { duration: 5000 }),
      });
  }

  onApplicationDrop(event: CdkDragDrop<CatalogApplicationDto[]>): void {
    if (event.previousIndex === event.currentIndex) {
      return;
    }
    moveItemInArray(this.applications, event.previousIndex, event.currentIndex);
    this.applyLocalSortOrder(this.applications);
    const ids = this.applications.map((a) => a.id!).filter((id) => id != null);
    this.catalog.reorderApplications(ids).subscribe({
      next: () => this.scheduleSidebarReload(),
      error: () => {
        this.snack.open('Failed to save application order', 'Close', { duration: 4000 });
        this.reload();
      },
    });
    this.cdr.markForCheck();
  }

  onMenuDrop(event: CdkDragDrop<CatalogMenuDto[]>, parentId: number | null): void {
    const appId = this.selectedApplication?.id;
    if (!appId || event.previousIndex === event.currentIndex || this.menuReorderInFlight) {
      return;
    }
    const siblings = event.container.data;
    if (!siblings?.length) {
      return;
    }
    moveItemInArray(siblings, event.previousIndex, event.currentIndex);
    this.applyLocalSortOrder(siblings);
    const ids = siblings.map((m) => m.id!).filter((id) => id != null);
    if (!ids.length) {
      return;
    }
    this.menuReorderInFlight = true;
    this.catalog.reorderMenus(appId, ids, parentId).subscribe({
      next: () => {
        this.menuReorderInFlight = false;
        this.scheduleSidebarReload();
        this.cdr.markForCheck();
      },
      error: () => {
        this.menuReorderInFlight = false;
        this.snack.open('Failed to save menu order', 'Close', { duration: 4000 });
        this.reload();
      },
    });
    this.cdr.markForCheck();
  }

  private applyLocalSortOrder(items: Array<{ sortOrder?: number }>): void {
    const step = 10;
    items.forEach((item, index) => {
      item.sortOrder = (index + 1) * step;
    });
  }

  private scheduleSidebarReload(): void {
    if (this.sidebarReloadTimer != null) {
      clearTimeout(this.sidebarReloadTimer);
    }
    this.sidebarReloadTimer = setTimeout(() => {
      this.sidebarReloadTimer = null;
      this.menuCatalog.reload();
    }, 400);
  }

  private rebuildParentMenuOptions(): void {
    const appId = (this.menuForm?.get('applicationId')?.value ?? this.selectedApplication?.id) as
      | number
      | null
      | undefined;
    const app = appId != null ? this.applications.find((a) => a.id === appId) : this.selectedApplication;
    const out: ParentMenuOption[] = [];
    const walk = (items: CatalogMenuDto[] | undefined, depth: number) => {
      if (!items) {
        return;
      }
      for (const m of items) {
        if (m.id != null) {
          const prefix = depth > 0 ? `${'  '.repeat(depth)}└ ` : '';
          out.push({ id: m.id, label: `${prefix}${m.name ?? ''}` });
        }
        walk(m.children, depth + 1);
      }
    };
    walk(app?.menus, 0);
    const editingId = this.menuForm.get('id')?.value as number | null;
    this.parentMenuOptions = out.filter((m) => m.id !== editingId);
  }

  private normalizeApplications(apps: CatalogApplicationDto[]): CatalogApplicationDto[] {
    return apps.map((app) => ({
      ...app,
      menus: this.normalizeMenuTree(app.menus),
    }));
  }

  private normalizeMenuTree(menus: CatalogMenuDto[] | undefined): CatalogMenuDto[] {
    if (!menus) {
      return [];
    }
    return menus.map((m) => ({
      ...m,
      children: m.children ? this.normalizeMenuTree(m.children) : [],
    }));
  }

  private patchApplicationForm(app: CatalogApplicationDto): void {
    this.applicationForm.reset({
      id: app.id ?? null,
      name: app.name ?? '',
      description: app.description ?? '',
      icon: app.icon ?? '',
      route: app.route ?? '',
      isActive: app.isActive !== false,
      sortOrder: app.sortOrder ?? null,
    });
  }

  private patchMenuForm(menu: CatalogMenuDto): void {
    const appId = menu.applicationId ?? this.selectedApplication?.id;
    if (appId != null) {
      this.selectedApplication = this.applications.find((a) => a.id === appId) ?? this.selectedApplication;
    }
    this.menuForm.reset({
      id: menu.id ?? null,
      applicationId: appId,
      parentId: menu.parentId ?? null,
      name: menu.name ?? '',
      route: menu.route ?? '',
      icon: menu.icon ?? '',
      isActive: menu.isActive !== false,
      sortOrder: menu.sortOrder ?? null,
    });
  }

  private findMenuById(app: CatalogApplicationDto, menuId: number): CatalogMenuDto | undefined {
    const walk = (items: CatalogMenuDto[] | undefined): CatalogMenuDto | undefined => {
      if (!items) {
        return undefined;
      }
      for (const m of items) {
        if (m.id === menuId) {
          return m;
        }
        const nested = walk(m.children);
        if (nested) {
          return nested;
        }
      }
      return undefined;
    };
    return walk(app.menus);
  }

  private normalizeIcon(icon: string | null | undefined): string | null {
    if (icon == null) {
      return null;
    }
    const t = icon.trim();
    if (t.length < 2 || !/^[a-z][a-z0-9\-]+$/i.test(t)) {
      return null;
    }
    return t;
  }

  private normalizeRoute(route: string | null | undefined): string | null {
    if (route == null) {
      return null;
    }
    const t = route.trim();
    if (!t) {
      return null;
    }
    return t.startsWith('/') ? t : `/${t}`;
  }
}
