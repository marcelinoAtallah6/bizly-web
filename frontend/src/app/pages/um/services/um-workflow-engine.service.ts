import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { GlobalConstants } from 'src/app/common/GlobalConstants';
import { BusinessApiService } from 'src/app/services/business-api.service';

export type WorkflowPipelineType =
  | 'ACTION_PIPELINE'
  | 'APPROVAL_GATEWAY'
  | 'NOTIFICATION_ONLY'
  | 'CUSTOM';

export type WorkflowStepType =
  | 'NOTIFICATION'
  | 'APPROVAL'
  | 'HTTP_TASK'
  | 'HTTP_POLL'
  | 'SYSTEM';

export interface WorkflowActionOption {
  actionCode: string;
  displayName: string;
  description?: string | null;
  moduleKey?: string | null;
  triggerKind?: string | null;
}

export interface EmailTemplateOption {
  templateKey: string;
  subjectPreview?: string | null;
}

export interface WorkflowTaskCatalogItem {
  taskKey: string;
  title: string;
  description?: string | null;
  stepType: WorkflowStepType;
  categoryKey: string;
  iconKey?: string | null;
  endpointPath?: string | null;
  httpMethod?: string | null;
}

export interface ApplicationActionTarget {
  screenRoute: string;
  menuLabel?: string | null;
  actionCode: string;
}

export interface WorkflowTaskCatalogCategory {
  categoryKey: string;
  categoryLabel: string;
  items: WorkflowTaskCatalogItem[];
}

export interface WorkflowPipelineRow {
  id: number;
  actionCode: string;
  actionDisplayName?: string | null;
  versionNo: number;
  status: string;
  businessId?: number | null;
  displayName?: string | null;
  notes?: string | null;
  publishedAt?: string | null;
  createdBy?: string | null;
  createdAt?: string | null;
  stepCount: number;
  workflowType?: WorkflowPipelineType | null;
  roleRestricted?: boolean;
  allowChildRoleInherit?: boolean;
  roleCodesJson?: string | null;
}

export interface WorkflowPipelineStep {
  id?: number | null;
  stepOrder: number;
  stepType: WorkflowStepType;
  active: boolean;
  configJson: string;
  workflowConfigId?: number | null;
  taskKey?: string | null;
  displayLabel?: string | null;
}

export interface WorkflowPipelineDetail extends WorkflowPipelineRow {
  steps: WorkflowPipelineStep[];
}

export interface NotificationStepConfig {
  timing: string;
  channels: string[];
  templateKey: string;
  audience: {
    mode: string;
    contextRecipient?: string;
    roleNames?: string[];
    userIds?: number[];
  };
}

export interface ApprovalStepConfig {
  taskKind: string;
  /** Registry row used by the API gateway (screen + action must match this endpoint). */
  endpointId?: number | null;
  screenRoute: string;
  actionName: string;
  workflowConfigId?: number | null;
  allowChildRoleInherit?: boolean;
  /** When true, maker/checker use usernames instead of role names. */
  userOverride?: boolean;
  makerRoles?: string[];
  makerUsers?: string[];
  /** Checker tiers by role; each inner array is one approval level. */
  checkerRoleTiers?: string[][];
  checkerUserTiers?: string[][];
  /** @deprecated use checkerRoleTiers — migrated on read */
  roleNames?: string[];
}

export interface HttpStepConfig {
  httpMethod: string;
  endpointPath: string;
  payloadJson: string;
}

@Injectable({ providedIn: 'root' })
export class UmWorkflowEngineService {
  private readonly ep = GlobalConstants.API_ENDPOINTS.um.workflow;

  constructor(private readonly api: BusinessApiService) {}

  listActions(): Observable<WorkflowActionOption[]> {
    return this.api.postEnvelope<WorkflowActionOption[]>(this.ep.engineActionsList, {});
  }

  listTaskCatalog(): Observable<WorkflowTaskCatalogCategory[]> {
    return this.api.postEnvelope<WorkflowTaskCatalogCategory[]>(this.ep.engineTaskCatalog, {});
  }

  listApplicationActions(): Observable<ApplicationActionTarget[]> {
    return this.api.postEnvelope<ApplicationActionTarget[]>(this.ep.engineApplicationActionsList, {});
  }

  static applicationActionKey(t: ApplicationActionTarget): string {
    return `${t.screenRoute}|${t.actionCode}`;
  }

  listTemplates(): Observable<EmailTemplateOption[]> {
    return this.api.postEnvelope<EmailTemplateOption[]>(this.ep.engineTemplatesList, {});
  }

  listDefinitions(): Observable<WorkflowPipelineRow[]> {
    return this.api.postEnvelope<WorkflowPipelineRow[]>(this.ep.engineDefinitionsList, {});
  }

  getDefinition(id: number): Observable<WorkflowPipelineDetail> {
    return this.api.postEnvelope<WorkflowPipelineDetail>(this.ep.engineDefinitionsGet, { id });
  }

  createDefinition(body: {
    actionCode: string;
    displayName?: string;
    notes?: string;
    businessId?: number | null;
    workflowType?: WorkflowPipelineType;
  }): Observable<number> {
    return this.api.postEnvelope<number>(this.ep.engineDefinitionsCreate, body);
  }

  updateDefinition(body: {
    id: number;
    displayName?: string;
    notes?: string;
    workflowType?: WorkflowPipelineType;
    roleRestricted?: boolean;
    allowChildRoleInherit?: boolean;
    roleCodesJson?: string | null;
  }): Observable<void> {
    return this.api.postEnvelope<void>(this.ep.engineDefinitionsUpdate, body);
  }

  saveSteps(definitionId: number, steps: WorkflowPipelineStep[]): Observable<void> {
    return this.api.postEnvelope<void>(this.ep.engineDefinitionsSaveSteps, { definitionId, steps });
  }

  publish(id: number): Observable<void> {
    return this.api.postEnvelope<void>(this.ep.engineDefinitionsPublish, { id });
  }

  disable(id: number): Observable<void> {
    return this.api.postEnvelope<void>(this.ep.engineDefinitionsDisable, { id });
  }

  deleteDraft(id: number): Observable<void> {
    return this.api.postEnvelope<void>(this.ep.engineDefinitionsDelete, { id });
  }

  static defaultNotificationConfig(): NotificationStepConfig {
    return {
      timing: 'ON_ACTION_SUCCESS',
      channels: ['EMAIL'],
      templateKey: '',
      audience: { mode: 'CONTEXT', contextRecipient: 'createdUser' },
    };
  }

  static defaultApprovalConfig(): ApprovalStepConfig {
    return {
      taskKind: 'APPROVAL_MANUAL',
      screenRoute: '',
      actionName: 'ADD',
      allowChildRoleInherit: false,
      userOverride: false,
      makerRoles: [],
      makerUsers: [],
      checkerRoleTiers: [[]],
      checkerUserTiers: [[]],
    };
  }

  static defaultHttpConfig(): HttpStepConfig {
    return { httpMethod: 'POST', endpointPath: '', payloadJson: '{}' };
  }

  static parseNotificationConfig(json: string | null | undefined): NotificationStepConfig {
    const base = UmWorkflowEngineService.defaultNotificationConfig();
    if (!json?.trim()) {
      return base;
    }
    try {
      const o = JSON.parse(json) as Partial<NotificationStepConfig>;
      return {
        timing: o.timing ?? base.timing,
        channels: Array.isArray(o.channels) && o.channels.length ? o.channels : base.channels,
        templateKey: o.templateKey ?? base.templateKey,
        audience: {
          mode: o.audience?.mode ?? base.audience.mode,
          contextRecipient: o.audience?.contextRecipient ?? base.audience.contextRecipient,
          roleNames: o.audience?.roleNames ?? base.audience.roleNames,
          userIds: o.audience?.userIds ?? base.audience.userIds,
        },
      };
    } catch {
      return base;
    }
  }

  static parseApprovalConfig(json: string | null | undefined): ApprovalStepConfig {
    const base = UmWorkflowEngineService.defaultApprovalConfig();
    if (!json?.trim()) {
      return base;
    }
    try {
      const o = JSON.parse(json) as Partial<ApprovalStepConfig> & { roleNames?: string[] };
      let checkerRoleTiers = o.checkerRoleTiers;
      if (!checkerRoleTiers?.length && o.roleNames?.length) {
        checkerRoleTiers = [o.roleNames];
      }
      if (!checkerRoleTiers?.length) {
        checkerRoleTiers = [[]];
      }
      let checkerUserTiers = o.checkerUserTiers;
      if (!checkerUserTiers?.length) {
        checkerUserTiers = checkerRoleTiers.map(() => [] as string[]);
      }
      return {
        ...base,
        ...o,
        endpointId: o.endpointId ?? base.endpointId,
        userOverride: !!o.userOverride,
        makerRoles: o.makerRoles ?? base.makerRoles,
        makerUsers: o.makerUsers ?? base.makerUsers,
        checkerRoleTiers,
        checkerUserTiers,
      };
    } catch {
      return base;
    }
  }

  static parseHttpConfig(json: string | null | undefined): HttpStepConfig {
    const base = UmWorkflowEngineService.defaultHttpConfig();
    if (!json?.trim()) {
      return base;
    }
    try {
      return { ...base, ...(JSON.parse(json) as Partial<HttpStepConfig>) };
    } catch {
      return base;
    }
  }

  static serializeNotificationConfig(cfg: NotificationStepConfig): string {
    return JSON.stringify(cfg, null, 2);
  }

  static serializeApprovalConfig(cfg: ApprovalStepConfig): string {
    return JSON.stringify(cfg, null, 2);
  }

  static serializeHttpConfig(cfg: HttpStepConfig): string {
    return JSON.stringify(cfg, null, 2);
  }

  static parseRoleCodesJson(json: string | null | undefined): string[] {
    if (!json?.trim()) {
      return [];
    }
    try {
      const arr = JSON.parse(json) as unknown;
      return Array.isArray(arr) ? arr.filter((x): x is string => typeof x === 'string') : [];
    } catch {
      return [];
    }
  }

  static serializeRoleCodes(names: string[]): string {
    return JSON.stringify(names.filter((n) => !!n?.trim()));
  }
}
