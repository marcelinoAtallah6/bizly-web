import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import { Component, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormControl, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin, finalize } from 'rxjs';
import { GetRoleResponse, GetUserResponse } from 'src/app/core/models/um.models';
import { AuthService } from 'src/app/services/auth.service';
import { MenuPermissionService } from 'src/app/services/menu-permission.service';
import { UmRoleService } from '../../services/um-role.service';
import { UmUserService } from '../../services/um-user.service';
import {
  UmWorkflowService,
  WorkflowCatalogScreen,
  WorkflowEndpointCatalogRow,
} from '../../services/um-workflow.service';
import {
  ApprovalStepConfig,
  EmailTemplateOption,
  HttpStepConfig,
  NotificationStepConfig,
  UmWorkflowEngineService,
  WorkflowActionOption,
  WorkflowPipelineDetail,
  WorkflowPipelineStep,
  WorkflowPipelineType,
  WorkflowTaskCatalogCategory,
  WorkflowTaskCatalogItem,
} from '../../services/um-workflow-engine.service';
import {
  WorkflowTaskPickerDialogComponent,
  WorkflowTaskPickerDialogData,
  WorkflowTaskPickerDialogResult,
} from './workflow-task-picker-dialog.component';
import { hasCheckerAssignment } from './workflow-approval-assignment.utils';

interface CanvasNode {
  clientKey: string;
  step: WorkflowPipelineStep;
}

@Component({
  selector: 'app-workflow-workspace',
  templateUrl: './workflow-workspace.component.html',
  styleUrls: ['./workflow-workspace.component.scss'],
})
export class WorkflowWorkspaceComponent implements OnInit {
  loading = true;
  saving = false;
  isNew = false;
  definitionId: number | null = null;
  detail: WorkflowPipelineDetail | null = null;
  actions: WorkflowActionOption[] = [];
  templates: EmailTemplateOption[] = [];
  taskCategories: WorkflowTaskCatalogCategory[] = [];
  catalog: WorkflowCatalogScreen[] = [];
  endpointCatalog: WorkflowEndpointCatalogRow[] = [];
  roles: GetRoleResponse[] = [];
  users: GetUserResponse[] = [];
  approvalCheckerError: string | null = null;
  nodes: CanvasNode[] = [];
  selectedClientKey: string | null = null;
  inspectorTab: 'workflow' | 'task' = 'workflow';
  inspectorTabIndex = 0;

  readonly workflowTypes: { value: WorkflowPipelineType; label: string }[] = [
    { value: 'ACTION_PIPELINE', label: 'Action pipeline' },
    { value: 'APPROVAL_GATEWAY', label: 'Approval gateway' },
    { value: 'NOTIFICATION_ONLY', label: 'Notification only' },
    { value: 'CUSTOM', label: 'Custom' },
  ];

  readonly isSuperAdmin: boolean;
  readonly canEdit: boolean;

  headerForm = this.fb.group({
    actionCode: ['', Validators.required],
    displayName: ['', Validators.required],
    workflowType: ['ACTION_PIPELINE' as WorkflowPipelineType, Validators.required],
    notes: [''],
    businessScope: ['business' as 'global' | 'business'],
    roleRestricted: [false],
    runRoleNames: [[] as string[]],
  });

  approvalForm = this.fb.group({
    taskKind: ['APPROVAL_MANUAL'],
    endpointId: [null as number | null, Validators.required],
    screenRoute: ['', Validators.required],
    actionName: ['ADD', Validators.required],
    userOverride: [false],
    makerRoles: this.fb.control<string[]>([], { nonNullable: true }),
    makerUsers: this.fb.control<string[]>([], { nonNullable: true }),
    checkerRoleTiers: this.fb.array<FormControl<string[]>>([
      this.fb.control<string[]>([], { nonNullable: true }),
    ]),
    checkerUserTiers: this.fb.array<FormControl<string[]>>([
      this.fb.control<string[]>([], { nonNullable: true }),
    ]),
  });

  notificationForm = this.fb.group({
    active: [true],
    timing: ['ON_ACTION_SUCCESS'],
    templateKey: ['', Validators.required],
    channelEmail: [true],
    channelInbox: [false],
    audienceMode: ['CONTEXT'],
    contextRecipient: ['createdUser'],
    audienceRoleNames: [[] as string[]],
  });

  httpForm = this.fb.group({
    active: [true],
    httpMethod: ['POST'],
    endpointPath: ['', Validators.required],
    payloadJson: ['{}'],
  });

  private nextClientKey = 1;

  constructor(
    private readonly fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly engine: UmWorkflowEngineService,
    private readonly workflow: UmWorkflowService,
    private readonly roleService: UmRoleService,
    private readonly usersApi: UmUserService,
    private readonly auth: AuthService,
    private readonly menuPerm: MenuPermissionService,
    private readonly snackBar: MatSnackBar,
    private readonly dialog: MatDialog
  ) {
    this.isSuperAdmin = this.auth.isSystemAdmin();
    this.canEdit = this.menuPerm.can('/um/workflow-engine', 'edit');
  }

  get isDraft(): boolean {
    return this.detail?.status === 'DRAFT';
  }

  get readOnly(): boolean {
    return !this.canEdit;
  }

  get isPublished(): boolean {
    return this.detail?.status === 'PUBLISHED';
  }

  get isDisabled(): boolean {
    return this.detail?.status === 'DISABLED';
  }

  get publishButtonLabel(): string {
    if (this.isDisabled) {
      return 'Republish';
    }
    if (this.isPublished) {
      return 'Apply';
    }
    return 'Publish';
  }

  workflowTypeLabel(type: string | null | undefined): string {
    return this.workflowTypes.find((t) => t.value === type)?.label ?? type ?? '';
  }

  /** Pipeline trigger (domain event or EP:id) — notifications run for this published definition. */
  pipelineTriggerLabel(): string {
    const code = this.detail?.actionCode ?? this.headerForm.get('actionCode')?.value;
    if (!code) {
      return '—';
    }
    const hit = this.actions.find((a) => a.actionCode === code);
    return hit?.displayName ?? String(code);
  }

  notificationTimingHint(): string {
    const t = this.notificationForm.get('timing')?.value;
    if (t === 'AFTER_APPROVAL') {
      return 'Runs when the last checker approves (before the API is replayed).';
    }
    return 'Runs when the pipeline trigger fires (user/customer created, API success, daily schedule such as birthday, or after approved API replay).';
  }

  get selectedNode(): CanvasNode | null {
    return this.nodes.find((n) => n.clientKey === this.selectedClientKey) ?? null;
  }

  get checkerRoleTiers(): FormArray<FormControl<string[]>> {
    return this.approvalForm.get('checkerRoleTiers') as FormArray<FormControl<string[]>>;
  }

  get checkerUserTiers(): FormArray<FormControl<string[]>> {
    return this.approvalForm.get('checkerUserTiers') as FormArray<FormControl<string[]>>;
  }

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    this.isNew = idParam === 'new' || !idParam;
    if (!this.isNew && idParam) {
      this.definitionId = Number(idParam);
    }
    if (this.isNew && this.route.snapshot.queryParamMap.get('kind') === 'approval') {
      this.headerForm.patchValue({ workflowType: 'APPROVAL_GATEWAY' as WorkflowPipelineType });
    }
    this.loadRefs();
  }

  private loadRefs(): void {
    forkJoin({
      actions: this.engine.listActions(),
      templates: this.engine.listTemplates(),
      taskCatalog: this.engine.listTaskCatalog(),
      screens: this.workflow.catalogScreens(),
      endpoints: this.workflow.endpointCatalog(),
      roles: this.roleService.gets({ pageNumber: 0, pageSize: 500 }),
      users: this.usersApi.gets({ pageNumber: 0, pageSize: 500 }),
    }).subscribe({
      next: (r) => {
        this.actions = r.actions ?? [];
        this.templates = r.templates ?? [];
        this.taskCategories = r.taskCatalog ?? [];
        this.catalog = r.screens ?? [];
        this.endpointCatalog = r.endpoints ?? [];
        this.roles = r.roles?.items ?? [];
        this.users = r.users?.items ?? [];
        if (this.isNew) {
          this.loading = false;
          return;
        }
        this.loadDefinition();
      },
      error: (err) => {
        this.loading = false;
        this.snackBar.open(err?.message ?? 'Failed to load', 'Close', { duration: 4000 });
      },
    });
  }

  private loadDefinition(): void {
    if (this.definitionId == null) {
      return;
    }
    this.engine
      .getDefinition(this.definitionId)
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (d) => {
          this.detail = d;
          this.nodes = (d.steps ?? []).map((s) => this.toNode(s));
          const runRoles = UmWorkflowEngineService.parseRoleCodesJson(d.roleCodesJson);
          this.headerForm.patchValue({
            actionCode: d.actionCode,
            displayName: d.displayName ?? d.actionDisplayName ?? d.actionCode,
            workflowType: (d.workflowType as WorkflowPipelineType) ?? 'ACTION_PIPELINE',
            notes: d.notes ?? '',
            businessScope: d.businessId == null ? 'global' : 'business',
            roleRestricted: !!d.roleRestricted,
            runRoleNames: runRoles,
          });
          if (this.readOnly) {
            this.lockInspectorForms();
          } else {
            this.headerForm.enable({ emitEvent: false });
          }
          this.ensureApprovalGatewayStep();
          if (this.nodes.length) {
            this.selectNode(this.nodes[0].clientKey);
          }
        },
        error: (err) =>
          this.snackBar.open(err?.message ?? 'Failed to load pipeline', 'Close', { duration: 4000 }),
      });
  }

  private toNode(step: WorkflowPipelineStep): CanvasNode {
    return { clientKey: `n-${this.nextClientKey++}`, step: { ...step } };
  }

  back(): void {
    this.router.navigate(['/um/workflow-engine']);
  }

  createPipeline(): void {
    if (this.headerForm.invalid || this.saving) {
      this.headerForm.markAllAsTouched();
      return;
    }
    const v = this.headerForm.getRawValue();
    const businessId = this.isSuperAdmin && v.businessScope === 'global' ? null : undefined;
    this.saving = true;
    this.engine
      .createDefinition({
        actionCode: v.actionCode!,
        displayName: v.displayName!,
        notes: v.notes || undefined,
        businessId: businessId ?? undefined,
        workflowType: v.workflowType!,
      })
      .pipe(finalize(() => (this.saving = false)))
      .subscribe({
        next: (id) => {
          this.snackBar.open('Draft created', 'Close', { duration: 2500 });
          this.router.navigate(['/um/workflow-engine', id, 'edit']);
        },
        error: (err) =>
          this.snackBar.open(err?.message ?? 'Create failed', 'Close', { duration: 4000 }),
      });
  }

  saveAll(): void {
    if (!this.definitionId || this.readOnly) {
      return;
    }
    this.applyInspectorToSelected();
    const approvalErr = this.validateApprovalSteps();
    if (approvalErr) {
      this.approvalCheckerError = approvalErr;
      this.snackBar.open(approvalErr, 'Close', { duration: 5000 });
      return;
    }
    this.approvalCheckerError = null;
    const v = this.headerForm.getRawValue();
    this.saving = true;
    const roleJson = v.roleRestricted
      ? UmWorkflowEngineService.serializeRoleCodes(v.runRoleNames ?? [])
      : null;
    this.engine
      .updateDefinition({
        id: this.definitionId,
        displayName: v.displayName!,
        notes: v.notes || undefined,
        workflowType: v.workflowType!,
        roleRestricted: !!v.roleRestricted,
        roleCodesJson: roleJson,
      })
      .subscribe({
        next: () => {
          this.engine
            .saveSteps(this.definitionId!, this.nodes.map((n, i) => this.nodeToStep(n, i)))
            .pipe(finalize(() => (this.saving = false)))
            .subscribe({
              next: () => {
                const msg = this.isPublished
                  ? 'Saved — gateway approval updated'
                  : this.isDisabled
                    ? 'Saved — use Republish to turn approval back on'
                    : 'Saved';
                this.snackBar.open(msg, 'Close', { duration: this.isDisabled ? 4500 : 2500 });
              },
              error: (err) =>
                this.snackBar.open(err?.message ?? 'Save steps failed', 'Close', { duration: 4000 }),
            });
        },
        error: (err) => {
          this.saving = false;
          this.snackBar.open(err?.message ?? 'Save failed', 'Close', { duration: 4000 });
        },
      });
  }

  publish(): void {
    if (!this.definitionId || this.readOnly) {
      return;
    }
    this.applyInspectorToSelected();
    const approvalErr = this.validateApprovalSteps();
    if (approvalErr) {
      this.approvalCheckerError = approvalErr;
      this.snackBar.open(approvalErr, 'Close', { duration: 5000 });
      return;
    }
    this.approvalCheckerError = null;
    this.saving = true;
    const v = this.headerForm.getRawValue();
    const roleJson = v.roleRestricted
      ? UmWorkflowEngineService.serializeRoleCodes(v.runRoleNames ?? [])
      : null;
    this.engine
      .updateDefinition({
        id: this.definitionId,
        displayName: v.displayName!,
        notes: v.notes || undefined,
        workflowType: v.workflowType!,
        roleRestricted: !!v.roleRestricted,
        roleCodesJson: roleJson,
      })
      .subscribe({
        next: () => {
          this.engine.saveSteps(this.definitionId!, this.nodes.map((n, i) => this.nodeToStep(n, i))).subscribe({
            next: () => {
              this.engine
                .publish(this.definitionId!)
                .pipe(finalize(() => (this.saving = false)))
                .subscribe({
                  next: () => {
                    const msg =
                      this.detail?.status === 'DISABLED'
                        ? 'Republished'
                        : this.detail?.status === 'PUBLISHED'
                          ? 'Changes applied'
                          : 'Published';
                    this.snackBar.open(msg, 'Close', { duration: 2500 });
                    this.loadDefinition();
                  },
                  error: (err) =>
                    this.snackBar.open(err?.message ?? 'Publish failed', 'Close', { duration: 4000 }),
                });
            },
            error: (err) => {
              this.saving = false;
              this.snackBar.open(err?.message ?? 'Save failed', 'Close', { duration: 4000 });
            },
          });
        },
        error: (err) => {
          this.saving = false;
          this.snackBar.open(err?.message ?? 'Save failed', 'Close', { duration: 4000 });
        },
      });
  }

  disablePipeline(): void {
    if (!this.definitionId || !this.canEdit) {
      return;
    }
    this.saving = true;
    this.engine
      .disable(this.definitionId)
      .pipe(finalize(() => (this.saving = false)))
      .subscribe({
        next: () => {
          this.snackBar.open('Disabled', 'Close', { duration: 2500 });
          this.loadDefinition();
        },
        error: (err) =>
          this.snackBar.open(err?.message ?? 'Disable failed', 'Close', { duration: 4000 }),
      });
  }

  deletePipeline(): void {
    if (!this.definitionId || !this.canEdit) {
      return;
    }
    const name = this.detail?.displayName ?? `Workflow #${this.definitionId}`;
    if (!confirm(`Delete "${name}"? Gateway approval rules for this pipeline will be removed.`)) {
      return;
    }
    this.saving = true;
    this.engine
      .deleteDraft(this.definitionId)
      .pipe(finalize(() => (this.saving = false)))
      .subscribe({
        next: () => {
          this.snackBar.open('Deleted', 'Close', { duration: 2500 });
          this.router.navigate(['/um/workflow-engine']);
        },
        error: (err) =>
          this.snackBar.open(err?.message ?? 'Delete failed', 'Close', { duration: 4000 }),
      });
  }

  openTaskPicker(): void {
    if (this.readOnly || this.isNew) {
      return;
    }
    const data: WorkflowTaskPickerDialogData = { categories: this.taskCategories };
    this.dialog
      .open(WorkflowTaskPickerDialogComponent, {
        width: '760px',
        maxWidth: '95vw',
        panelClass: 'wf-engine-dialog',
        data,
      })
      .afterClosed()
      .subscribe((res: WorkflowTaskPickerDialogResult | undefined) => {
        if (res?.item) {
          this.addTask(res.item);
        }
      });
  }

  addTask(item: WorkflowTaskCatalogItem): void {
    const order = this.nodes.length ? Math.max(...this.nodes.map((n) => n.step.stepOrder)) + 10 : 10;
    let configJson = '{}';
    let workflowConfigId: number | null = null;
    if (item.stepType === 'NOTIFICATION') {
      const cfg = UmWorkflowEngineService.defaultNotificationConfig();
      if (item.taskKey === 'NOTIF_INBOX') {
        cfg.channels = ['INBOX'];
      } else if (item.taskKey === 'NOTIF_BOTH') {
        cfg.channels = ['EMAIL', 'INBOX'];
      }
      configJson = UmWorkflowEngineService.serializeNotificationConfig(cfg);
    } else if (item.stepType === 'APPROVAL') {
      const cfg = UmWorkflowEngineService.defaultApprovalConfig();
      cfg.taskKind = item.taskKey;
      configJson = UmWorkflowEngineService.serializeApprovalConfig(cfg);
    } else if (item.stepType === 'HTTP_TASK' || item.stepType === 'HTTP_POLL') {
      const cfg = UmWorkflowEngineService.defaultHttpConfig();
      cfg.httpMethod = item.httpMethod ?? 'POST';
      cfg.endpointPath = item.endpointPath ?? '';
      configJson = UmWorkflowEngineService.serializeHttpConfig(cfg);
    }
    const step: WorkflowPipelineStep = {
      stepOrder: order,
      stepType: item.stepType,
      active: true,
      configJson,
      workflowConfigId,
      taskKey: item.taskKey,
      displayLabel: item.title,
    };
    const node = this.toNode(step);
    this.nodes = [...this.nodes, node];
    this.selectNode(node.clientKey);
  }

  dropStep(event: CdkDragDrop<CanvasNode[]>): void {
    if (this.readOnly) {
      return;
    }
    const copy = [...this.nodes];
    moveItemInArray(copy, event.previousIndex, event.currentIndex);
    this.nodes = copy.map((n, i) => ({
      ...n,
      step: { ...n.step, stepOrder: (i + 1) * 10 },
    }));
  }

  selectNode(clientKey: string): void {
    this.applyInspectorToSelected();
    this.selectedClientKey = clientKey;
    this.inspectorTab = 'task';
    const node = this.nodes.find((n) => n.clientKey === clientKey);
    if (!node) {
      return;
    }
    const s = node.step;
    if (s.stepType === 'APPROVAL') {
      const cfg = UmWorkflowEngineService.parseApprovalConfig(s.configJson);
      this.patchApprovalFormFromConfig(cfg);
      if (!this.readOnly) {
        this.approvalForm.enable({ emitEvent: false });
      }
    } else if (s.stepType === 'NOTIFICATION') {
      if (!this.readOnly) {
        this.notificationForm.enable({ emitEvent: false });
      }
      const cfg = UmWorkflowEngineService.parseNotificationConfig(s.configJson);
      this.notificationForm.patchValue({
        active: s.active,
        timing: cfg.timing,
        templateKey: cfg.templateKey,
        channelEmail: cfg.channels.includes('EMAIL') || cfg.channels.includes('BOTH'),
        channelInbox: cfg.channels.includes('INBOX') || cfg.channels.includes('BOTH'),
        audienceMode: cfg.audience.mode,
        contextRecipient: cfg.audience.contextRecipient ?? 'createdUser',
        audienceRoleNames: cfg.audience.roleNames ?? [],
      });
    } else if (s.stepType === 'HTTP_TASK' || s.stepType === 'HTTP_POLL') {
      const cfg = UmWorkflowEngineService.parseHttpConfig(s.configJson);
      this.httpForm.patchValue({
        active: s.active,
        httpMethod: cfg.httpMethod,
        endpointPath: cfg.endpointPath,
        payloadJson: cfg.payloadJson,
      });
      if (!this.readOnly) {
        this.httpForm.enable({ emitEvent: false });
      }
    }
    if (this.readOnly) {
      this.lockInspectorForms();
    }
  }

  private lockInspectorForms(): void {
    this.headerForm.disable({ emitEvent: false });
    this.approvalForm.disable({ emitEvent: false });
    this.notificationForm.disable({ emitEvent: false });
    this.httpForm.disable({ emitEvent: false });
  }

  removeSelectedNode(): void {
    if (this.readOnly || !this.selectedClientKey) {
      return;
    }
    this.nodes = this.nodes.filter((n) => n.clientKey !== this.selectedClientKey);
    this.selectedClientKey = this.nodes.length ? this.nodes[0].clientKey : null;
    this.inspectorTab = this.selectedClientKey ? 'task' : 'workflow';
  }

  private applyInspectorToSelected(): void {
    const node = this.selectedNode;
    if (!node || this.readOnly) {
      return;
    }
    const s = { ...node.step };
    if (s.stepType === 'APPROVAL') {
      const cfg = this.buildApprovalConfigFromForm(s);
      s.configJson = UmWorkflowEngineService.serializeApprovalConfig(cfg);
    } else if (s.stepType === 'NOTIFICATION') {
      const v = this.notificationForm.getRawValue();
      const channels: string[] = [];
      if (v.channelEmail) {
        channels.push('EMAIL');
      }
      if (v.channelInbox) {
        channels.push('INBOX');
      }
      if (!channels.length) {
        channels.push('EMAIL');
      }
      const cfg: NotificationStepConfig = {
        timing: v.timing ?? 'ON_ACTION_SUCCESS',
        channels,
        templateKey: v.templateKey ?? '',
        audience: {
          mode: v.audienceMode ?? 'CONTEXT',
          contextRecipient: v.contextRecipient ?? undefined,
          roleNames: v.audienceRoleNames?.length ? v.audienceRoleNames : undefined,
        },
      };
      s.active = !!v.active;
      s.configJson = UmWorkflowEngineService.serializeNotificationConfig(cfg);
    } else if (s.stepType === 'HTTP_TASK' || s.stepType === 'HTTP_POLL') {
      const v = this.httpForm.getRawValue();
      const cfg: HttpStepConfig = {
        httpMethod: v.httpMethod ?? 'POST',
        endpointPath: v.endpointPath ?? '',
        payloadJson: v.payloadJson ?? '{}',
      };
      s.active = !!v.active;
      s.configJson = UmWorkflowEngineService.serializeHttpConfig(cfg);
    }
    const idx = this.nodes.findIndex((n) => n.clientKey === node.clientKey);
    if (idx >= 0) {
      this.nodes = this.nodes.map((n, i) => (i === idx ? { ...n, step: s } : n));
    }
  }

  private nodeToStep(node: CanvasNode, index: number): WorkflowPipelineStep {
    return {
      ...node.step,
      stepOrder: (index + 1) * 10,
    };
  }

  nodeTitle(node: CanvasNode): string {
    return node.step.displayLabel || node.step.taskKey || node.step.stepType;
  }

  nodeSubtitle(node: CanvasNode): string {
    return node.step.stepType;
  }

  nodeIcon(node: CanvasNode): string {
    const t = node.step.stepType;
    if (t === 'APPROVAL') {
      return 'shield-check';
    }
    if (t === 'NOTIFICATION') {
      return 'mail';
    }
    if (t === 'HTTP_TASK' || t === 'HTTP_POLL') {
      return 'world';
    }
    return 'settings';
  }

  gatewayEndpoints(): WorkflowEndpointCatalogRow[] {
    return this.endpointCatalog ?? [];
  }

  endpointLabel(e: WorkflowEndpointCatalogRow): string {
    const menu = e.menuLabel?.trim() || e.screenRoute || '—';
    const path = e.pathAntPattern?.trim() || '';
    const method = e.httpMethod?.trim() || '';
    const verb = e.actionCode?.trim() || '';
    const pathPart = path ? ` · ${method} ${path}` : '';
    return `${menu} · ${verb}${pathPart}`;
  }

  onApprovalEndpointChange(endpointId: number | string | null): void {
    if (endpointId == null || endpointId === '') {
      return;
    }
    const id = typeof endpointId === 'string' ? Number(endpointId) : endpointId;
    if (!Number.isFinite(id)) {
      return;
    }
    const hit = this.gatewayEndpoints().find((e) => e.endpointId === id);
    if (hit) {
      this.approvalForm.patchValue({
        endpointId: id,
        screenRoute: (hit.screenRoute ?? '').trim(),
        actionName: (hit.actionCode ?? '').trim(),
      });
      const trigger = `EP:${id}`;
      if (this.headerForm.get('actionCode')?.value !== trigger) {
        this.headerForm.patchValue({ actionCode: trigger });
      }
    }
  }

  roleNames(): string[] {
    return this.roles.map((r) => r.name).filter((n): n is string => !!n);
  }

  userLabel(u: GetUserResponse): string {
    const name = `${u.firstName ?? ''} ${u.lastName ?? ''}`.trim();
    return name ? `${u.username} — ${name}` : u.username;
  }

  onApprovalOverrideToggle(): void {
    this.syncCheckerUserTierCount(!!this.approvalForm.get('userOverride')?.value);
  }

  addCheckerTier(): void {
    if (this.checkerRoleTiers.length >= 10) {
      return;
    }
    this.checkerRoleTiers.push(this.fb.control<string[]>([], { nonNullable: true }));
    if (this.approvalForm.get('userOverride')?.value) {
      this.checkerUserTiers.push(this.fb.control<string[]>([], { nonNullable: true }));
    }
  }

  removeCheckerTier(index: number): void {
    if (this.checkerRoleTiers.length <= 1) {
      return;
    }
    this.checkerRoleTiers.removeAt(index);
    if (this.checkerUserTiers.length > index) {
      this.checkerUserTiers.removeAt(index);
    }
  }

  private syncCheckerUserTierCount(overrideOn: boolean): void {
    if (!overrideOn) {
      while (this.checkerUserTiers.length > this.checkerRoleTiers.length) {
        this.checkerUserTiers.removeAt(this.checkerUserTiers.length - 1);
      }
      while (this.checkerUserTiers.length < this.checkerRoleTiers.length) {
        this.checkerUserTiers.push(this.fb.control<string[]>([], { nonNullable: true }));
      }
      for (let i = 0; i < this.checkerUserTiers.length; i++) {
        this.checkerUserTiers.at(i).setValue([]);
      }
      return;
    }
    while (this.checkerUserTiers.length < this.checkerRoleTiers.length) {
      this.checkerUserTiers.push(this.fb.control<string[]>([], { nonNullable: true }));
    }
    while (this.checkerUserTiers.length > this.checkerRoleTiers.length) {
      this.checkerUserTiers.removeAt(this.checkerUserTiers.length - 1);
    }
  }

  private patchApprovalFormFromConfig(cfg: ApprovalStepConfig): void {
    const tierCount = Math.max(cfg.checkerRoleTiers?.length ?? 1, cfg.checkerUserTiers?.length ?? 1, 1);
    this.checkerRoleTiers.clear();
    this.checkerUserTiers.clear();
    for (let i = 0; i < tierCount; i++) {
      this.checkerRoleTiers.push(
        this.fb.control<string[]>([...(cfg.checkerRoleTiers?.[i] ?? [])], { nonNullable: true })
      );
      this.checkerUserTiers.push(
        this.fb.control<string[]>([...(cfg.checkerUserTiers?.[i] ?? [])], { nonNullable: true })
      );
    }
    let endpointId = cfg.endpointId ?? null;
    if (endpointId == null && cfg.screenRoute && cfg.actionName) {
      const hit = this.gatewayEndpoints().find(
        (e) =>
          (e.screenRoute ?? '').trim().toLowerCase() === cfg.screenRoute.trim().toLowerCase() &&
          (e.actionCode ?? '').trim().toUpperCase() === cfg.actionName.trim().toUpperCase()
      );
      endpointId = hit?.endpointId ?? null;
    }
    this.approvalForm.patchValue({
      taskKind: cfg.taskKind,
      endpointId,
      screenRoute: cfg.screenRoute,
      actionName: cfg.actionName,
      userOverride: !!cfg.userOverride,
      makerRoles: cfg.makerRoles ?? [],
      makerUsers: cfg.makerUsers ?? [],
    });
  }

  private buildApprovalConfigFromForm(step: WorkflowPipelineStep): ApprovalStepConfig {
    const v = this.approvalForm.getRawValue();
    const roleTiers = this.checkerRoleTiers.controls.map((c) => [...(c.value ?? [])]);
    const userTiers = this.checkerUserTiers.controls.map((c) => [...(c.value ?? [])]);
    return {
      taskKind: v.taskKind ?? 'APPROVAL_MANUAL',
      endpointId: v.endpointId ?? null,
      screenRoute: (v.screenRoute ?? '').trim(),
      actionName: (v.actionName ?? 'ADD').trim(),
      workflowConfigId: step.workflowConfigId ?? null,
      userOverride: !!v.userOverride,
      makerRoles: v.makerRoles ?? [],
      makerUsers: v.makerUsers ?? [],
      checkerRoleTiers: roleTiers,
      checkerUserTiers: userTiers,
    };
  }

  private validateApprovalSteps(): string | null {
    for (const n of this.nodes) {
      if (n.step.stepType !== 'APPROVAL' || !n.step.active) {
        continue;
      }
      const cfg = UmWorkflowEngineService.parseApprovalConfig(n.step.configJson);
      if (!cfg.endpointId && (!cfg.screenRoute?.trim() || !cfg.actionName?.trim())) {
        return `Approval step "${this.nodeTitle(n)}" requires a gateway API endpoint.`;
      }
      if (
        !hasCheckerAssignment(
          !!cfg.userOverride,
          cfg.checkerRoleTiers ?? [[]],
          cfg.checkerUserTiers ?? [[]]
        )
      ) {
        return `Approval step "${this.nodeTitle(n)}" requires at least one checker on level 1.`;
      }
    }
    return null;
  }

  private ensureApprovalGatewayStep(): void {
    if (!this.isDraft || this.headerForm.get('workflowType')?.value !== 'APPROVAL_GATEWAY') {
      return;
    }
    if (this.nodes.some((n) => n.step.stepType === 'APPROVAL')) {
      return;
    }
    const cfg = UmWorkflowEngineService.defaultApprovalConfig();
    const step: WorkflowPipelineStep = {
      stepOrder: 10,
      stepType: 'APPROVAL',
      active: true,
      configJson: UmWorkflowEngineService.serializeApprovalConfig(cfg),
      taskKey: 'APPROVAL_MANUAL',
      displayLabel: 'Approval',
    };
    const node = this.toNode(step);
    this.nodes = [node];
    this.selectedClientKey = node.clientKey;
    this.inspectorTab = 'task';
    this.patchApprovalFormFromConfig(cfg);
  }

  onInspectorTab(index: number): void {
    this.inspectorTabIndex = index;
    this.inspectorTab = index === 0 ? 'workflow' : 'task';
    if (index === 1 && this.selectedClientKey) {
      this.selectNode(this.selectedClientKey);
    }
  }
}
