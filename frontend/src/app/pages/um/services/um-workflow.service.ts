import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { GlobalConstants } from 'src/app/common/GlobalConstants';
import { BusinessApiService } from 'src/app/services/business-api.service';

/** Row from {@code POST /um/workflow/instance/queue}. */
export interface WorkflowInstanceRow {
  id: number;
  screenName?: string | null;
  actionName?: string | null;
  triggeredByUsername?: string | null;
  currentLevel?: number | null;
  levelRequired?: number | null;
  levelCompleted?: number | null;
  status?: string | null;
  createdAt?: string | null;
  businessId?: number | null;
  payloadJson?: string | null;
  checkerSummary?: string | null;
  canApprove?: boolean | null;
  /** True when the user is initiator or on maker roles/users (UI: no approve/reject). */
  makerParticipant?: boolean | null;
  checkerComment?: string | null;
}

export interface WorkflowQueueQuery {
  status?: string | null;
  /** Exact match (case-insensitive) on instance screen route. */
  screenRoute?: string | null;
  /** Exact match (case-insensitive) on instance action (e.g. ADD). */
  actionName?: string | null;
  screenNameContains?: string | null;
  actionNameContains?: string | null;
  dateFrom?: string | null;
  dateTo?: string | null;
}

export interface BusinessRegistrationWorkflowBody {
  businessId: number;
  businessName: string;
}

export interface WorkflowConfigRow {
  id: number;
  screenName?: string | null;
  actionName?: string | null;
  hasWorkflow: boolean;
  levelCount: number;
  makerRolesJson?: string | null;
  checkerRolesJson?: string | null;
  makerUsersJson?: string | null;
  checkerUsersJson?: string | null;
  builtInKey?: string | null;
  businessId?: number | null;
  createdBy?: string | null;
  createdAt?: string | null;
  systemLocked: boolean;
}

export interface WorkflowCatalogScreen {
  route: string;
  menuLabel?: string | null;
  actions: string[];
}

/** Row from {@code POST /um/workflow/config/endpoint-catalog} (mutating registry). */
export interface WorkflowEndpointCatalogRow {
  endpointId: number;
  screenRoute?: string | null;
  menuLabel?: string | null;
  actionCode?: string | null;
  httpMethod?: string | null;
  pathAntPattern?: string | null;
  displayLabel?: string | null;
}

export interface CreateWorkflowConfigBody {
  screenName: string;
  actionName: string;
  hasWorkflow: boolean;
  levelCount: number;
  makerRolesJson?: string | null;
  checkerRolesJson?: string | null;
  makerUsersJson?: string | null;
  checkerUsersJson?: string | null;
  businessId?: number | null;
  /** When set, server resolves screen + action from {@code UM_WORKFLOW_API_ENDPOINT}. */
  endpointId?: number | null;
}

export interface UpdateWorkflowConfigBody {
  id: number;
  hasWorkflow?: boolean;
  levelCount?: number;
  makerRolesJson?: string | null;
  checkerRolesJson?: string | null;
  makerUsersJson?: string | null;
  checkerUsersJson?: string | null;
}

@Injectable({ providedIn: 'root' })
export class UmWorkflowService {
  constructor(private readonly api: BusinessApiService) {}

  requestBusinessRegistration(body: BusinessRegistrationWorkflowBody): Observable<number | null> {
    return this.api.postEnvelope<number | null>(
      GlobalConstants.API_ENDPOINTS.um.workflow.businessRegistrationRequest,
      body,
      'errors'
    );
  }

  queue(query: WorkflowQueueQuery = {}): Observable<WorkflowInstanceRow[]> {
    return this.api.postEnvelope<WorkflowInstanceRow[]>(
      GlobalConstants.API_ENDPOINTS.um.workflow.queue,
      query,
      'errors'
    );
  }

  approve(instanceId: number, comment?: string | null): Observable<void> {
    return this.api.postEnvelope<void>(
      GlobalConstants.API_ENDPOINTS.um.workflow.approve,
      { instanceId, comment: comment?.trim() || undefined },
      'success-and-errors'
    );
  }

  reject(instanceId: number, comment?: string | null): Observable<void> {
    return this.api.postEnvelope<void>(
      GlobalConstants.API_ENDPOINTS.um.workflow.reject,
      { instanceId, comment: comment?.trim() || undefined },
      'success-and-errors'
    );
  }

  /** Permission action codes from UM (aggregated from UM_ROLE_MENU_PERM); same list as menu metadata. */
  mutatingActions(): Observable<string[]> {
    return this.api.postEnvelope<string[]>(
      GlobalConstants.API_ENDPOINTS.um.workflow.catalogMutatingActions,
      {},
      'errors'
    );
  }

  listConfigs(): Observable<WorkflowConfigRow[]> {
    return this.api.postEnvelope<WorkflowConfigRow[]>(
      GlobalConstants.API_ENDPOINTS.um.workflow.configList,
      {},
      'errors'
    );
  }

  catalogScreens(): Observable<WorkflowCatalogScreen[]> {
    return this.api.postEnvelope<WorkflowCatalogScreen[]>(
      GlobalConstants.API_ENDPOINTS.um.workflow.configCatalog,
      {},
      'errors'
    );
  }

  /** Active mutating endpoints (ADD/EDIT/DELETE) for workflow target selection. */
  endpointCatalog(): Observable<WorkflowEndpointCatalogRow[]> {
    return this.api.postEnvelope<WorkflowEndpointCatalogRow[]>(
      GlobalConstants.API_ENDPOINTS.um.workflow.configEndpointCatalog,
      {},
      'errors'
    );
  }

  createConfig(body: CreateWorkflowConfigBody): Observable<void> {
    return this.api.postEnvelope<void>(
      GlobalConstants.API_ENDPOINTS.um.workflow.configCreate,
      body,
      'success-and-errors'
    );
  }

  deleteConfig(id: number): Observable<void> {
    return this.api.postEnvelope<void>(
      GlobalConstants.API_ENDPOINTS.um.workflow.configDelete,
      { id },
      'success-and-errors'
    );
  }

  updateConfig(body: UpdateWorkflowConfigBody): Observable<void> {
    return this.api.postEnvelope<void>(
      GlobalConstants.API_ENDPOINTS.um.workflow.configUpdate,
      body,
      'success-and-errors'
    );
  }
}
