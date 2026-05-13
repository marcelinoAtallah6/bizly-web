import { DOCUMENT } from '@angular/common';
import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { BroadcastTargetType, CreateBroadcastRequest } from 'src/app/core/models/broadcast.models';
import { GetRoleResponse } from 'src/app/core/models/um.models';
import { ToolbarButton } from 'src/app/pages/ui-components/button/toolbar/toolbar.component';
import { MenuPermissionService } from 'src/app/services/menu-permission.service';
import { UmRoleService } from 'src/app/pages/um/services/um-role.service';
import { BroadcastPreviewDialogComponent } from '../../dialogs/broadcast-preview-dialog.component';
import { BroadcastMessageService } from '../../services/broadcast-message.service';

@Component({
  selector: 'app-broadcast-compose',
  templateUrl: './broadcast-compose.component.html',
  styleUrl: './broadcast-compose.component.scss',
})
export class BroadcastComposeComponent implements OnInit, OnDestroy {
  readonly routeBroadcast = '/broadcast';

  /** Outlook-like rich text toolbar (stored as HTML in {@link form.controls.body}). */
  readonly editorConfig: Record<string, unknown>;

  /** CKEditor instance once `ready` fires; used to sync iframe body with app light / dark / dark-blue. */
  private broadcastCkEditor: any = null;
  private bodyThemeObserver: MutationObserver | null = null;

  readonly targetOptions: { value: BroadcastTargetType; label: string }[] = [
    { value: 'ALL_USERS', label: 'All users' },
    { value: 'ALL_CUSTOMERS', label: 'All customers' },
    { value: 'ROLE_BASED', label: 'Users in role' },
    { value: 'CUSTOM_SEGMENT', label: 'Custom segment (JSON)' },
  ];

  roles: GetRoleResponse[] = [];
  loadingRoles = false;
  submitting = false;

  form = this.fb.nonNullable.group({
    subject: ['', [Validators.required, Validators.maxLength(512)]],
    body: ['', [Validators.required]],
    targetType: this.fb.nonNullable.control<BroadcastTargetType>('ALL_USERS', [Validators.required]),
    targetRoleId: this.fb.control<number | null>(null),
    customSegmentJson: [''],
    queueForSend: this.fb.nonNullable.control(false),
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly broadcastApi: BroadcastMessageService,
    private readonly umRoleService: UmRoleService,
    private readonly router: Router,
    private readonly menuPerm: MenuPermissionService,
    private readonly dialog: MatDialog,
    @Inject(DOCUMENT) private readonly doc: Document
  ) {
    const baseHref = this.doc.querySelector('base')?.getAttribute('href') || '/';
    const cssHref = new URL('assets/broadcast-ckeditor-body.css', this.doc.location?.origin + baseHref).href;
    this.editorConfig = {
      height: 360,
      resize_enabled: true,
      versionCheck: false,
      removePlugins: 'elementspath,notification',
      contentsCss: [cssHref],
      toolbar: [
        { name: 'basicstyles', items: ['Bold', 'Italic', 'Underline', 'Strike'] },
        { name: 'paragraph', items: ['NumberedList', 'BulletedList', '-', 'Outdent', 'Indent'] },
        { name: 'justify', items: ['JustifyLeft', 'JustifyCenter', 'JustifyRight', 'JustifyBlock'] },
        { name: 'links', items: ['Link', 'Unlink'] },
        { name: 'insert', items: ['Table', 'HorizontalRule'] },
        { name: 'clipboard', items: ['Undo', 'Redo'] },
        { name: 'document', items: ['RemoveFormat', 'Source'] },
      ],
    };
  }

  get formToolbar(): ToolbarButton[] {
    return [
      {
        id: 'back',
        icon: 'arrow_back',
        tooltip: 'Back to list',
        action: () => void this.router.navigate(['/broadcast/messages']),
      },
    ];
  }

  ngOnInit(): void {
    // Allow page to open even if route is not seeded into JWT matrix yet.
    if (!this.menuPerm.canIfListedOrAllow(this.routeBroadcast, 'add')) {
      void this.router.navigate(['/broadcast/messages']);
      return;
    }

    this.form.controls.targetType.valueChanges.subscribe(() => this.applyTargetValidators());
    this.applyTargetValidators();

    this.bodyThemeObserver = new MutationObserver(() => this.syncBroadcastEditorBodyTheme());
    this.bodyThemeObserver.observe(this.doc.body, { attributes: true, attributeFilter: ['class'] });

    this.loadingRoles = true;
    this.umRoleService
      .gets({ pageNumber: 0, pageSize: 500 })
      .pipe(finalize(() => (this.loadingRoles = false)))
      .subscribe({
        next: (page) => {
          this.roles = page.items ?? [];
        },
        error: () => {},
      });
  }

  ngOnDestroy(): void {
    this.bodyThemeObserver?.disconnect();
    this.bodyThemeObserver = null;
    this.broadcastCkEditor = null;
  }

  onBroadcastEditorReady(evt: unknown): void {
    const editor = (evt as { editor?: any })?.editor;
    this.broadcastCkEditor = editor ?? null;
    this.syncBroadcastEditorBodyTheme();
  }

  private syncBroadcastEditorBodyTheme(): void {
    const editor = this.broadcastCkEditor;
    if (!editor?.document?.getBody) {
      return;
    }
    const body = editor.document.getBody();
    body.removeClass('bizly-editor-body--light');
    body.removeClass('bizly-editor-body--dark');
    body.removeClass('bizly-editor-body--dark-blue');
    const hostBody = this.doc.body;
    if (hostBody.classList.contains('dark-blue-mode')) {
      body.addClass('bizly-editor-body--dark-blue');
    } else if (hostBody.classList.contains('dark-mode')) {
      body.addClass('bizly-editor-body--dark');
    } else {
      body.addClass('bizly-editor-body--light');
    }
  }

  private applyTargetValidators(): void {
    const t = this.form.controls.targetType.value;
    const roleCtl = this.form.controls.targetRoleId;
    const segCtl = this.form.controls.customSegmentJson;
    if (t === 'ROLE_BASED') {
      roleCtl.setValidators([Validators.required]);
      segCtl.clearValidators();
    } else if (t === 'CUSTOM_SEGMENT') {
      roleCtl.clearValidators();
      segCtl.setValidators([Validators.required]);
    } else {
      roleCtl.clearValidators();
      segCtl.clearValidators();
    }
    roleCtl.updateValueAndValidity({ emitEvent: false });
    segCtl.updateValueAndValidity({ emitEvent: false });
  }

  preview(): void {
    if (!this.menuPerm.canIfListedOrAllow(this.routeBroadcast, 'view')) {
      return;
    }
    if (this.form.controls.subject.invalid || !this.hasBodyContent()) {
      this.form.markAllAsTouched();
      return;
    }
    const { subject, body } = this.form.getRawValue();
    this.broadcastApi.preview({ subject, body }).subscribe({
      next: (res) => {
        this.dialog.open(BroadcastPreviewDialogComponent, {
          width: '600px',
          maxWidth: '95vw',
          data: res,
        });
      },
      error: () => {},
    });
  }

  submit(): void {
    if (this.form.invalid || !this.hasBodyContent()) {
      this.form.markAllAsTouched();
      return;
    }
    const raw = this.form.getRawValue();
    const body: CreateBroadcastRequest = {
      subject: raw.subject.trim(),
      body: raw.body.trim(),
      targetType: raw.targetType,
      queueForSend: raw.queueForSend,
    };
    if (raw.targetType === 'ROLE_BASED') {
      body.targetRoleId = raw.targetRoleId ?? undefined;
    }
    if (raw.targetType === 'CUSTOM_SEGMENT') {
      body.customSegmentJson = raw.customSegmentJson.trim();
    }

    this.submitting = true;
    this.broadcastApi
      .create(body)
      .pipe(finalize(() => (this.submitting = false)))
      .subscribe({
        next: () => void this.router.navigate(['/broadcast/messages']),
        error: () => {},
      });
  }

  private hasBodyContent(): boolean {
    const html = this.form.controls.body.value ?? '';
    const text = html
      .replace(/<[^>]+>/g, ' ')
      .replace(/&nbsp;/gi, ' ')
      .replace(/\s+/g, ' ')
      .trim();
    return text.length > 0;
  }
}

