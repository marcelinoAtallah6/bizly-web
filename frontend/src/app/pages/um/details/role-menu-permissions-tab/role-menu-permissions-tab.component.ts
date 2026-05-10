import {
  Component,
  Input,
  OnChanges,
  OnInit,
  SimpleChanges,
} from '@angular/core';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import {
  GetRoleResponse,
  RoleMenuPermissionEntryDto,
  RoleMenuPermissionRowResponse,
} from 'src/app/core/models/um.models';
import { ToolbarButton } from 'src/app/pages/ui-components/button/toolbar/toolbar.component';
import { UmRoleService } from '../../services/um-role.service';

interface PermNode {
  row: RoleMenuPermissionRowResponse;
  children: PermNode[];
}

type PermFlagKey = 'allowView' | 'allowAdd' | 'allowEdit' | 'allowDelete';

@Component({
  selector: 'app-role-menu-permissions-tab',
  templateUrl: './role-menu-permissions-tab.component.html',
  styleUrl: './role-menu-permissions-tab.component.scss',
})
export class RoleMenuPermissionsTabComponent implements OnChanges, OnInit {
  @Input() roleId: number | null = null;

  rows: RoleMenuPermissionRowResponse[] = [];
  treeRoots: PermNode[] = [];
  menuFilter = '';

  roles: GetRoleResponse[] = [];
  loading = false;
  saving = false;

  private rowByMenuId = new Map<number, RoleMenuPermissionRowResponse>();
  private descendantIdsByMenuId = new Map<number, number[]>();

  constructor(
    private readonly umRoleService: UmRoleService,
    private readonly router: Router
  ) {}

  /** Visible nodes after menu / route search (structure preserved). */
  get displayTree(): PermNode[] {
    return this.filterTreeBySearch(this.treeRoots, this.menuFilter.trim().toLowerCase());
  }

  get permToolbar(): ToolbarButton[] {
    return [
      {
        id: 'refresh',
        icon: 'refresh',
        tooltip: 'Reload',
        action: () => this.load(),
        disabled: this.loading || this.roleId == null,
      },
      {
        id: 'save',
        icon: 'save',
        tooltip: 'Save permissions',
        action: () => this.save(),
        disabled: this.saving || this.loading || this.roleId == null || this.rows.length === 0,
        color: 'primary',
      },
    ];
  }

  ngOnInit(): void {
    this.umRoleService
      .gets({ pageNumber: 0, pageSize: 500 })
      .subscribe({
        next: (res) => {
          this.roles = res.items ?? [];
        },
        error: () => {
          this.roles = [];
        },
      });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['roleId'] && this.roleId != null) {
      this.load();
    }
  }

  onRoleSelected(id: number): void {
    if (id === this.roleId) {
      return;
    }
    this.router.navigate(['/um', 'role', id]);
  }

  load(): void {
    if (this.roleId == null) {
      return;
    }
    this.loading = true;
    this.umRoleService
      .getMenuPermissions({ roleId: this.roleId })
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (list) => {
          this.rows = list ?? [];
          this.rowByMenuId = new Map(this.rows.map((r) => [r.menuId, r]));
          this.treeRoots = this.buildMenuTree(this.rows);
          this.descendantIdsByMenuId = this.buildDescendantMap(this.treeRoots);
        },
        error: () => {
          this.rows = [];
          this.treeRoots = [];
          this.rowByMenuId.clear();
          this.descendantIdsByMenuId.clear();
        },
      });
  }

  save(): void {
    if (this.roleId == null) {
      return;
    }
    const permissions: RoleMenuPermissionEntryDto[] = this.rows.map((r) => ({
      menuId: r.menuId,
      allowView: !!r.allowView,
      allowAdd: !!r.allowAdd,
      allowEdit: !!r.allowEdit,
      allowDelete: !!r.allowDelete,
    }));

    this.saving = true;
    this.umRoleService
      .saveMenuPermissions({ roleId: this.roleId, permissions })
      .pipe(finalize(() => (this.saving = false)))
      .subscribe({
        next: () => this.load(),
        error: () => {},
      });
  }

  setFlag(row: RoleMenuPermissionRowResponse, key: PermFlagKey, checked: boolean): void {
    row[key] = checked;
    if (key === 'allowView' && !checked) {
      row.allowAdd = false;
      row.allowEdit = false;
      row.allowDelete = false;
    }

    const desc = this.descendantIdsByMenuId.get(row.menuId);
    if (!desc?.length) {
      return;
    }
    for (const id of desc) {
      const child = this.rowByMenuId.get(id);
      if (!child) {
        continue;
      }
      child[key] = checked;
      if (key === 'allowView' && !checked) {
        child.allowAdd = false;
        child.allowEdit = false;
        child.allowDelete = false;
      }
    }
  }

  clearMenuFilter(): void {
    this.menuFilter = '';
  }

  private buildMenuTree(rows: RoleMenuPermissionRowResponse[]): PermNode[] {
    const byParent = new Map<string, RoleMenuPermissionRowResponse[]>();
    for (const r of rows) {
      const key =
        r.parentMenuId != null && r.parentMenuId !== undefined
          ? String(r.parentMenuId)
          : '__root__';
      if (!byParent.has(key)) {
        byParent.set(key, []);
      }
      byParent.get(key)!.push(r);
    }
    const sortFn = (a: RoleMenuPermissionRowResponse, b: RoleMenuPermissionRowResponse) =>
      (a.menuPath ?? '').localeCompare(b.menuPath ?? '', undefined, { sensitivity: 'base' });

    const build = (parentKey: string): PermNode[] => {
      const list = byParent.get(parentKey) ?? [];
      list.sort(sortFn);
      return list.map((row) => ({
        row,
        children: build(String(row.menuId)),
      }));
    };

    return build('__root__');
  }

  private buildDescendantMap(roots: PermNode[]): Map<number, number[]> {
    const map = new Map<number, number[]>();
    const walk = (n: PermNode): number[] => {
      const ids: number[] = [];
      for (const c of n.children) {
        ids.push(c.row.menuId, ...walk(c));
      }
      map.set(n.row.menuId, ids);
      return ids;
    };
    for (const r of roots) {
      walk(r);
    }
    return map;
  }

  private rowMatchesSearch(row: RoleMenuPermissionRowResponse, q: string): boolean {
    const hay = [
      row.menuPath,
      row.route,
      row.applicationName,
      row.applicationId != null ? String(row.applicationId) : '',
    ]
      .filter(Boolean)
      .join(' ')
      .toLowerCase();
    return hay.includes(q);
  }

  /**
   * If a node matches the search, keep its full subtree. Otherwise include only matching descendant branches.
   */
  private filterTreeBySearch(nodes: PermNode[], q: string): PermNode[] {
    if (!q) {
      return nodes;
    }
    const out: PermNode[] = [];
    for (const n of nodes) {
      const selfMatch = this.rowMatchesSearch(n.row, q);
      const childFiltered = this.filterTreeBySearch(n.children, q);
      if (selfMatch) {
        out.push({ ...n, children: n.children });
      } else if (childFiltered.length) {
        out.push({ ...n, children: childFiltered });
      }
    }
    return out;
  }
}
