import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { GlobalConstants } from 'src/app/common/GlobalConstants';
import {
  AddTravelCommissionRuleRequest,
  GetsTravelCommissionRulesResponse,
  TravelPageRequest,
  UpdateTravelCommissionRuleRequest,
} from 'src/app/core/models/travel.models';
import { BusinessApiService } from 'src/app/services/business-api.service';

@Injectable({ providedIn: 'root' })
export class TravelCommissionService {
  constructor(private readonly api: BusinessApiService) {}

  gets(body: TravelPageRequest): Observable<GetsTravelCommissionRulesResponse> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.commissionRule.gets, body);
  }

  add(body: AddTravelCommissionRuleRequest): Observable<{ id: number }> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.commissionRule.add, body, 'success-and-errors');
  }

  update(body: UpdateTravelCommissionRuleRequest): Observable<unknown> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.commissionRule.update, body, 'success-and-errors');
  }

  delete(body: { id: number }): Observable<unknown> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.commissionRule.delete, body, 'success-and-errors');
  }
}
