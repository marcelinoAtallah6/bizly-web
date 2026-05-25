package com.travel.api.dto.commission;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.travel.api.dto.common.TravelMutationResponse;
import com.travel.common.ApiDefaultValdiation;
import com.travel.api.dto.common.TravelMutationResponse;
import com.travel.common.PageRequest;

import lombok.Getter;
import lombok.Setter;

public final class TravelCommissionRuleDtos {

	private TravelCommissionRuleDtos() {}

	@Getter
	@Setter
	public static class AddTravelCommissionRuleRequest {
		@NotBlank(message = ApiDefaultValdiation.NAME)
		private String name;
		@NotBlank(message = ApiDefaultValdiation.NAME)
		private String ruleType;
		private Double ratePercent;
		private Double flatAmount;
		private Boolean active;
	}

	@Getter
	@Setter
	public static class AddTravelCommissionRuleResponse {
		private Long id;
	}

	@Getter
	@Setter
	public static class UpdateTravelCommissionRuleRequest {
		@NotNull(message = ApiDefaultValdiation.ID)
		private Long id;
		@NotBlank(message = ApiDefaultValdiation.NAME)
		private String name;
		@NotBlank(message = ApiDefaultValdiation.NAME)
		private String ruleType;
		private Double ratePercent;
		private Double flatAmount;
		private Boolean active;
	}

	@Getter
	@Setter
	public static class UpdateTravelCommissionRuleResponse extends TravelMutationResponse {}

	@Getter
	@Setter
	public static class DeleteTravelCommissionRuleRequest {
		@NotNull(message = ApiDefaultValdiation.ID)
		private Long id;
	}

	@Getter
	@Setter
	public static class DeleteTravelCommissionRuleResponse extends TravelMutationResponse {}

	@Getter
	@Setter
	public static class GetTravelCommissionRuleRequest {
		@NotNull(message = ApiDefaultValdiation.ID)
		private Long id;
	}

	@Getter
	@Setter
	public static class GetTravelCommissionRuleResponse {
		private Long id;
		private String name;
		private String ruleType;
		private Double ratePercent;
		private Double flatAmount;
		private Boolean active;
	}

	@Getter
	@Setter
	public static class GetsTravelCommissionRulesRequest extends PageRequest {}
}
