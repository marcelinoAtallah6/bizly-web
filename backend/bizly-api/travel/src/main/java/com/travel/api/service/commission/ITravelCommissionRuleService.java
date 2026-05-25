package com.travel.api.service.commission;

import com.travel.api.dto.commission.TravelCommissionRuleDtos.*;
import com.travel.common.PageResponse;

public interface ITravelCommissionRuleService {

	AddTravelCommissionRuleResponse add(AddTravelCommissionRuleRequest request);

	UpdateTravelCommissionRuleResponse update(UpdateTravelCommissionRuleRequest request);

	DeleteTravelCommissionRuleResponse delete(DeleteTravelCommissionRuleRequest request);

	GetTravelCommissionRuleResponse get(GetTravelCommissionRuleRequest request);

	PageResponse<GetTravelCommissionRuleResponse> gets(GetsTravelCommissionRulesRequest request);
}
