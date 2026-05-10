import {
  AfterViewInit,
  Component,
  ElementRef,
  NgZone,
  OnDestroy,
  OnInit,
  ViewChild,
} from '@angular/core';
import { PageEvent } from '@angular/material/paginator';
import { MatSnackBar } from '@angular/material/snack-bar';
import type { editor } from 'monaco-editor';
import loader from '@monaco-editor/loader';
import { ColDef, ICellRendererParams } from 'ag-grid-community';
import { QueryDefDto } from 'src/app/core/models/settings.models';
import { SettingsApiService } from 'src/app/services/settings-api.service';

@Component({
  selector: 'app-querybuilder',
  templateUrl: './querybuilder.component.html',
  styleUrl: './querybuilder.component.scss',
})
export class QuerybuilderComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('monacoCreateContainer') monacoCreateContainer!: ElementRef<HTMLDivElement>;

  loading = false;
  saving = false;
  queries: QueryDefDto[] = [];
  /** Server-side name filter (debounced). */
  queryNameSearch = '';

  queryTotalCount = 0;
  queryPageNumber = 0;
  queryPageSize = 15;

  /** Null when creating a new query; set when editing from the list. */
  editingId: number | null = null;

  name = '';
  description = '';

  testRows: Record<string, unknown>[] = [];
  testing = false;

  queryGridColumnDefs: ColDef[] = [];
  testGridColumnDefs: ColDef[] = [];

  /** Saved-queries grid: server paging — no client floating filters (avoids overlay / click issues). */
  readonly savedListColDef: ColDef = {
    sortable: false,
    filter: false,
    resizable: true,
    floatingFilter: false,
  };

  readonly defaultGridColDef: ColDef = {
    sortable: true,
    filter: true,
    resizable: true,
    floatingFilter: true,
  };

  private monacoNs: typeof import('monaco-editor') | null = null;
  private createEditor: editor.IStandaloneCodeEditor | null = null;
  private searchDebounceHandle: ReturnType<typeof setTimeout> | null = null;
  private bodyThemeObserver: MutationObserver | null = null;

  constructor(
    private readonly settingsApi: SettingsApiService,
    private readonly snackBar: MatSnackBar,
    private readonly ngZone: NgZone,
  ) {
    this.queryGridColumnDefs = [
      { field: 'name', headerName: 'Name', flex: 2, minWidth: 160 },
      { field: 'description', headerName: 'Description', flex: 2, minWidth: 160 },
      {
        headerName: '',
        width: 96,
        maxWidth: 96,
        sortable: false,
        filter: false,
        floatingFilter: false,
        cellRenderer: (p: ICellRendererParams<QueryDefDto>) => {
          const host = p.context?.host as QuerybuilderComponent | undefined;
          if (!host || !p.data) {
            return document.createElement('span');
          }
          const wrap = document.createElement('div');
          wrap.className = 'qb-grid-actions';

          const editBtn = document.createElement('button');
          editBtn.type = 'button';
          editBtn.className = 'qb-grid-actions__btn qb-grid-actions__btn--edit';
          editBtn.title = 'Edit';
          editBtn.setAttribute('aria-label', 'Edit query');
          editBtn.innerHTML =
            '<span class="material-icons qb-grid-actions__icon">edit_note</span>';
          editBtn.addEventListener('click', (ev) => {
            ev.stopPropagation();
            host.invokeEdit(p.data!);
          });

          const delBtn = document.createElement('button');
          delBtn.type = 'button';
          delBtn.className = 'qb-grid-actions__btn qb-grid-actions__btn--delete';
          delBtn.title = 'Delete';
          delBtn.setAttribute('aria-label', 'Delete query');
          delBtn.innerHTML =
            '<span class="material-icons qb-grid-actions__icon">close</span>';
          delBtn.addEventListener('click', (ev) => {
            ev.stopPropagation();
            host.invokeRemove(p.data!);
          });

          wrap.appendChild(editBtn);
          wrap.appendChild(delBtn);
          return wrap;
        },
      },
    ];
  }

  ngOnInit(): void {
    void this.reloadList();
  }

  ngAfterViewInit(): void {
    void this.initCreateMonaco();
  }

  ngOnDestroy(): void {
    this.bodyThemeObserver?.disconnect();
    this.bodyThemeObserver = null;
    this.createEditor?.dispose();
    if (this.searchDebounceHandle) {
      clearTimeout(this.searchDebounceHandle);
    }
  }

  /** Run inside Angular zone so clicks from ag-grid custom DOM work reliably after refresh/search. */
  invokeEdit(q: QueryDefDto): void {
    this.ngZone.run(() => this.edit(q));
  }

  invokeRemove(q: QueryDefDto): void {
    this.ngZone.run(() => void this.remove(q));
  }

  private async getMonaco(): Promise<typeof import('monaco-editor')> {
    if (!this.monacoNs) {
      this.monacoNs = await loader.init();
    }
    return this.monacoNs;
  }

  private async initCreateMonaco(): Promise<void> {
    if (!this.monacoCreateContainer?.nativeElement || this.createEditor) {
      return;
    }
    const monaco = await this.getMonaco();
    this.registerBizlyMonacoThemes(monaco);
    this.createEditor = monaco.editor.create(this.monacoCreateContainer.nativeElement, {
      value: '',
      language: 'sql',
      theme: 'vs',
      automaticLayout: true,
      minimap: { enabled: true },
      fontSize: 14,
      lineNumbers: 'on',
      scrollBeyondLastLine: false,
      wordWrap: 'on',
      tabSize: 2,
      padding: { top: 12, bottom: 12 },
    });
    this.createEditor.onDidChangeModelContent(() =>
      this.ngZone.run(() => undefined),
    );
    this.syncMonacoTheme();
    this.attachMonacoThemeObserver();
  }

  /**
   * Dark Blue uses the app card blue-grey (#2a3447) instead of Monaco’s near-black vs-dark canvas so SQL reads clearly.
   * Dark uses editor chrome aligned with `--cardbg` (#1e1e1e).
   */
  private registerBizlyMonacoThemes(monaco: typeof import('monaco-editor')): void {
    monaco.editor.defineTheme('bizly-dark-blue', {
      base: 'vs-dark',
      inherit: true,
      rules: [],
      colors: {
        'editor.background': '#2a3447',
        'editorGutter.background': '#253042',
        'editorLineNumber.foreground': '#8b96ab',
        'editorLineNumber.activeForeground': '#dce3ef',
        'minimap.background': '#253042',
      },
    });
    monaco.editor.defineTheme('bizly-dark', {
      base: 'vs-dark',
      inherit: true,
      rules: [],
      colors: {
        'editor.background': '#1e1e1e',
        'editorGutter.background': '#181818',
        'editorLineNumber.foreground': '#858585',
        'minimap.background': '#181818',
      },
    });
  }

  /** Matches Customizer body classes on `document.body`. */
  private syncMonacoTheme(): void {
    const monaco = this.monacoNs;
    if (!monaco || !this.createEditor) {
      return;
    }
    const body = document.body;
    let themeId = 'vs';
    if (body.classList.contains('dark-blue-mode')) {
      themeId = 'bizly-dark-blue';
    } else if (body.classList.contains('dark-mode')) {
      themeId = 'bizly-dark';
    }
    monaco.editor.setTheme(themeId);
  }

  private attachMonacoThemeObserver(): void {
    if (typeof MutationObserver === 'undefined' || this.bodyThemeObserver) {
      return;
    }
    this.bodyThemeObserver = new MutationObserver(() =>
      this.ngZone.run(() => this.syncMonacoTheme()),
    );
    this.bodyThemeObserver.observe(document.body, {
      attributes: true,
      attributeFilter: ['class'],
    });
  }

  private getSql(): string {
    return this.createEditor?.getValue() ?? '';
  }

  isSqlEmpty(): boolean {
    return !this.getSql().trim();
  }

  /** Debounced reload — resets to page 0 when filter text changes. */
  scheduleSearchReload(): void {
    if (this.searchDebounceHandle) {
      clearTimeout(this.searchDebounceHandle);
    }
    this.searchDebounceHandle = setTimeout(() => {
      this.searchDebounceHandle = null;
      this.queryPageNumber = 0;
      void this.reloadList();
    }, 400);
  }

  onQueryPage(ev: PageEvent): void {
    this.queryPageNumber = ev.pageIndex;
    this.queryPageSize = ev.pageSize;
    void this.reloadList();
  }

  async reloadList(): Promise<void> {
    this.loading = true;
    try {
      let res = await this.settingsApi.listQueryDefsPage({
        pageNumber: this.queryPageNumber,
        pageSize: this.queryPageSize,
        nameSearch: this.queryNameSearch.trim() || undefined,
      });
      const totalPages = res.totalPages ?? 0;
      if (totalPages === 0) {
        this.queryPageNumber = 0;
      } else if (this.queryPageNumber >= totalPages) {
        this.queryPageNumber = Math.max(0, totalPages - 1);
        res = await this.settingsApi.listQueryDefsPage({
          pageNumber: this.queryPageNumber,
          pageSize: this.queryPageSize,
          nameSearch: this.queryNameSearch.trim() || undefined,
        });
      }
      this.queries = res.items ?? [];
      this.queryTotalCount = res.totalCount ?? 0;
    } finally {
      this.loading = false;
    }
  }

  /** Clears the editor form for a new query (must run before starting a new definition). */
  newQuery(): void {
    this.editingId = null;
    this.name = '';
    this.description = '';
    this.testRows = [];
    this.testGridColumnDefs = [];
    if (this.createEditor) {
      this.createEditor.setValue('');
    }
  }

  edit(q: QueryDefDto): void {
    this.editingId = q.id;
    this.name = q.name;
    this.description = q.description ?? '';
    this.testRows = [];
    this.testGridColumnDefs = [];
    if (this.createEditor) {
      this.createEditor.setValue(q.sqlText ?? '');
    }
    requestAnimationFrame(() => this.createEditor?.layout());
  }

  async validate(): Promise<void> {
    const sql = this.getSql().trim();
    if (!sql) {
      return;
    }
    await this.settingsApi.validateSql(sql);
    this.snackBar.open('SQL syntax is valid.', 'Dismiss', { duration: 3500 });
  }

  async runTest(): Promise<void> {
    const sql = this.getSql().trim();
    if (!sql) {
      return;
    }
    this.testing = true;
    try {
      this.testRows = await this.settingsApi.executeQueryTest(sql);
      const cols =
        this.testRows.length > 0 ? Object.keys(this.testRows[0]) : [];
      this.testGridColumnDefs = cols.map((field) => ({
        field,
        flex: 1,
        minWidth: 120,
      }));
    } finally {
      this.testing = false;
    }
  }

  async save(): Promise<void> {
    const sql = this.getSql();
    if (!this.name.trim() || !sql.trim()) {
      return;
    }
    const wasEdit = this.editingId != null;
    this.saving = true;
    try {
      await this.settingsApi.saveQuery({
        id: this.editingId ?? undefined,
        name: this.name.trim(),
        description: this.description.trim() || undefined,
        sqlText: sql,
      });
      await this.reloadList();
      this.newQuery();
      this.snackBar.open(wasEdit ? 'Query updated.' : 'Query saved.', 'Dismiss', { duration: 3000 });
    } finally {
      this.saving = false;
    }
  }

  async remove(q: QueryDefDto): Promise<void> {
    if (!confirm(`Delete saved query "${q.name}"?`)) {
      return;
    }
    await this.settingsApi.deleteQuery(q.id);
    await this.reloadList();
    if (this.editingId === q.id) {
      this.newQuery();
    }
  }
}
