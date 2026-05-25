import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { forkJoin, finalize } from 'rxjs';
import { AuthService } from 'src/app/services/auth.service';
import { MenuPermissionService } from 'src/app/services/menu-permission.service';
import {
  EmailTemplateOption,
  NotificationStepConfig,
  UmWorkflowEngineService,
  WorkflowActionOption,
  WorkflowPipelineDetail,
  WorkflowPipelineStep,
} from '../../services/um-workflow-engine.service';

@Component({
  selector: 'app-workflow-engine-editor',
  templateUrl: './workflow-engine-editor.component.html',
  styleUrls: ['./workflow-engine-editor.component.scss'],
})
export class WorkflowEngineEditorComponent implements OnInit {
  loading = true;
  saving = false;
  isNew = false;
  definitionId: number | null = null;
  detail: WorkflowPipelineDetail | null = null;
  actions: WorkflowActionOption[] = [];
  templates: EmailTemplateOption[] = [];
  steps: WorkflowPipelineStep[] = [];
  selectedStepIndex: number | null = null;

  readonly isSuperAdmin: boolean;
  readonly canEdit: boolean;

  metaForm = this.fb.group({
    actionCode: ['', Validators.required],
    displayName: ['', Validators.required],
    notes: [''],
    businessScope: ['business' as 'global' | 'business'],
  });

  stepForm = this.fb.group({
    stepType: ['NOTIFICATION' as 'NOTIFICATION' | 'APPROVAL', Validators.required],
    active: [true],
    timing: ['ON_ACTION_SUCCESS'],
    templateKey: ['', Validators.required],
    channelEmail: [true],
    channelInbox: [false],
    audienceMode: ['CONTEXT'],
    contextRecipient: ['createdUser'],
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly engine: UmWorkflowEngineService,
    private readonly auth: AuthService,
    private readonly menuPerm: MenuPermissionService,
    private readonly snackBar: MatSnackBar
  ) {
    this.isSuperAdmin = this.auth.isSystemAdmin();
    this.canEdit = this.menuPerm.can('/um/workflow-engine', 'edit');
  }

  get isDraft(): boolean {
    return this.detail?.status === 'DRAFT';
  }

  get readOnly(): boolean {
    return !this.canEdit || !this.isDraft;
  }

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    this.isNew = idParam === 'new' || !idParam;
    if (!this.isNew && idParam) {
      this.definitionId = Number(idParam);
    }
    this.loadRefs();
  }

  private loadRefs(): void {
    forkJoin({
      actions: this.engine.listActions(),
      templates: this.engine.listTemplates(),
    })
      .pipe(finalize(() => {}))
      .subscribe({
        next: ({ actions, templates }) => {
          this.actions = actions ?? [];
          this.templates = templates ?? [];
          if (this.isNew) {
            this.loading = false;
            this.steps = [];
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
          this.steps = (d.steps ?? []).map((s) => ({ ...s }));
          this.metaForm.patchValue({
            actionCode: d.actionCode,
            displayName: d.displayName ?? d.actionDisplayName ?? d.actionCode,
            notes: d.notes ?? '',
            businessScope: d.businessId == null ? 'global' : 'business',
          });
          if (!this.isDraft) {
            this.metaForm.disable();
          }
          if (this.steps.length) {
            this.selectStep(0);
          }
        },
        error: (err) =>
          this.snackBar.open(err?.message ?? 'Failed to load pipeline', 'Close', { duration: 4000 }),
      });
  }

  back(): void {
    this.router.navigate(['/um/workflow-engine']);
  }

  createPipeline(): void {
    if (this.metaForm.invalid || this.saving) {
      this.metaForm.markAllAsTouched();
      return;
    }
    const v = this.metaForm.getRawValue();
    const businessId =
      this.isSuperAdmin && v.businessScope === 'global' ? null : undefined;
    this.saving = true;
    this.engine
      .createDefinition({
        actionCode: v.actionCode!,
        displayName: v.displayName!,
        notes: v.notes || undefined,
        businessId: businessId ?? undefined,
      })
      .pipe(finalize(() => (this.saving = false)))
      .subscribe({
        next: (id) => {
          this.snackBar.open('Draft pipeline created', 'Close', { duration: 2500 });
          this.router.navigate(['/um/workflow-engine', id, 'edit']);
        },
        error: (err) =>
          this.snackBar.open(err?.message ?? 'Create failed', 'Close', { duration: 4000 }),
      });
  }

  saveMeta(): void {
    if (!this.definitionId || this.metaForm.invalid || this.readOnly) {
      return;
    }
    const v = this.metaForm.getRawValue();
    this.saving = true;
    this.engine
      .updateDefinition({
        id: this.definitionId,
        displayName: v.displayName!,
        notes: v.notes || undefined,
      })
      .pipe(finalize(() => (this.saving = false)))
      .subscribe({
        next: () => this.snackBar.open('Saved', 'Close', { duration: 2000 }),
        error: (err) =>
          this.snackBar.open(err?.message ?? 'Save failed', 'Close', { duration: 4000 }),
      });
  }

  addNotificationStep(): void {
    if (this.readOnly) {
      return;
    }
    const order = this.steps.length ? Math.max(...this.steps.map((s) => s.stepOrder)) + 10 : 10;
    const cfg = UmWorkflowEngineService.defaultNotificationConfig();
    const action = this.metaForm.get('actionCode')?.value ?? this.detail?.actionCode ?? '';
    if (action.includes('CUSTOMER')) {
      cfg.audience.contextRecipient = 'createdCustomer';
    }
    this.steps = [
      ...this.steps,
      {
        stepOrder: order,
        stepType: 'NOTIFICATION',
        active: true,
        configJson: UmWorkflowEngineService.serializeNotificationConfig(cfg),
      },
    ];
    this.selectStep(this.steps.length - 1);
  }

  removeSelectedStep(): void {
    if (this.readOnly || this.selectedStepIndex == null) {
      return;
    }
    this.steps = this.steps.filter((_, i) => i !== this.selectedStepIndex);
    this.selectedStepIndex = this.steps.length ? 0 : null;
    if (this.selectedStepIndex != null) {
      this.loadStepIntoForm(this.steps[0]);
    }
  }

  moveStep(delta: number): void {
    if (this.readOnly || this.selectedStepIndex == null) {
      return;
    }
    const j = this.selectedStepIndex + delta;
    if (j < 0 || j >= this.steps.length) {
      return;
    }
    const copy = [...this.steps];
    [copy[this.selectedStepIndex], copy[j]] = [copy[j], copy[this.selectedStepIndex]];
    this.steps = copy.map((s, i) => ({ ...s, stepOrder: (i + 1) * 10 }));
    this.selectedStepIndex = j;
  }

  selectStep(index: number): void {
    this.applyStepFormToModel();
    this.selectedStepIndex = index;
    this.loadStepIntoForm(this.steps[index]);
  }

  private loadStepIntoForm(step: WorkflowPipelineStep): void {
    if (step.stepType === 'NOTIFICATION') {
      const cfg = UmWorkflowEngineService.parseNotificationConfig(step.configJson);
      this.stepForm.patchValue({
        stepType: 'NOTIFICATION',
        active: step.active,
        timing: cfg.timing,
        templateKey: cfg.templateKey,
        channelEmail: cfg.channels.includes('EMAIL') || cfg.channels.includes('BOTH'),
        channelInbox: cfg.channels.includes('INBOX') || cfg.channels.includes('BOTH'),
        audienceMode: cfg.audience.mode,
        contextRecipient: cfg.audience.contextRecipient ?? 'createdUser',
      });
    } else {
      this.stepForm.patchValue({
        stepType: 'APPROVAL',
        active: step.active,
      });
    }
    if (this.readOnly) {
      this.stepForm.disable();
    } else {
      this.stepForm.enable();
    }
  }

  private applyStepFormToModel(): void {
    if (this.selectedStepIndex == null || this.readOnly) {
      return;
    }
    const v = this.stepForm.getRawValue();
    const step = { ...this.steps[this.selectedStepIndex] };
    step.active = !!v.active;
    step.stepType = v.stepType as 'NOTIFICATION' | 'APPROVAL';
    if (step.stepType === 'NOTIFICATION') {
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
          contextRecipient: v.contextRecipient ?? 'createdUser',
        },
      };
      step.configJson = UmWorkflowEngineService.serializeNotificationConfig(cfg);
    } else {
      step.configJson = step.configJson || '{"note":"Configure approval via Workflow configuration screen."}';
    }
    this.steps[this.selectedStepIndex] = step;
  }

  saveSteps(): void {
    if (!this.definitionId || this.readOnly) {
      return;
    }
    this.applyStepFormToModel();
    this.saving = true;
    this.engine
      .saveSteps(this.definitionId, this.steps)
      .pipe(finalize(() => (this.saving = false)))
      .subscribe({
        next: () => this.snackBar.open('Steps saved', 'Close', { duration: 2000 }),
        error: (err) =>
          this.snackBar.open(err?.message ?? 'Save steps failed', 'Close', { duration: 4000 }),
      });
  }

  publish(): void {
    if (!this.definitionId || this.readOnly) {
      return;
    }
    this.applyStepFormToModel();
    this.saving = true;
    this.engine
      .saveSteps(this.definitionId, this.steps)
      .subscribe({
        next: () => {
          this.engine
            .publish(this.definitionId!)
            .pipe(finalize(() => (this.saving = false)))
            .subscribe({
              next: () => {
                this.snackBar.open('Pipeline published', 'Close', { duration: 2500 });
                this.loadDefinition();
              },
              error: (err) =>
                this.snackBar.open(err?.message ?? 'Publish failed', 'Close', { duration: 4000 }),
            });
        },
        error: (err) => {
          this.saving = false;
          this.snackBar.open(err?.message ?? 'Save before publish failed', 'Close', { duration: 4000 });
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
          this.snackBar.open('Pipeline disabled', 'Close', { duration: 2500 });
          this.loadDefinition();
        },
        error: (err) =>
          this.snackBar.open(err?.message ?? 'Disable failed', 'Close', { duration: 4000 }),
      });
  }

  stepLabel(step: WorkflowPipelineStep, index: number): string {
    if (step.stepType === 'NOTIFICATION') {
      const cfg = UmWorkflowEngineService.parseNotificationConfig(step.configJson);
      return `Notification · ${cfg.templateKey || 'template?'}`;
    }
    return `Step ${index + 1} · ${step.stepType}`;
  }

  onActionChange(): void {
    const code = this.metaForm.get('actionCode')?.value ?? '';
    const action = this.actions.find((a) => a.actionCode === code);
    if (action && !this.metaForm.get('displayName')?.dirty) {
      this.metaForm.patchValue({ displayName: action.displayName });
    }
  }
}
