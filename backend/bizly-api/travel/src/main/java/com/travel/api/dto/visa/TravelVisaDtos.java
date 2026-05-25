package com.travel.api.dto.visa;

import java.time.LocalDateTime;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.travel.api.dto.common.TravelMutationResponse;
import com.travel.common.ApiDefaultValdiation;
import com.travel.api.dto.common.TravelMutationResponse;
import com.travel.common.PageRequest;

import lombok.Getter;
import lombok.Setter;

public final class TravelVisaDtos {

	private TravelVisaDtos() {}

	@Getter
	@Setter
	public static class AddTravelVisaRequest {
		@NotNull(message = ApiDefaultValdiation.CUSTOMER_ID)
		private Long clientId;
		private Long bookingId;
		@NotBlank(message = ApiDefaultValdiation.NAME)
		private String country;
		private String visaType;
		private String status;
		private LocalDateTime submittedAt;
		private LocalDateTime decisionAt;
		private String notes;
	}

	@Getter
	@Setter
	public static class AddTravelVisaResponse {
		private Long id;
	}

	@Getter
	@Setter
	public static class UpdateTravelVisaRequest {
		@NotNull(message = ApiDefaultValdiation.ID)
		private Long id;
		@NotNull(message = ApiDefaultValdiation.CUSTOMER_ID)
		private Long clientId;
		private Long bookingId;
		@NotBlank(message = ApiDefaultValdiation.NAME)
		private String country;
		private String visaType;
		private String status;
		private LocalDateTime submittedAt;
		private LocalDateTime decisionAt;
		private String notes;
	}

	@Getter
	@Setter
	public static class UpdateTravelVisaResponse extends TravelMutationResponse {}

	@Getter
	@Setter
	public static class DeleteTravelVisaRequest {
		@NotNull(message = ApiDefaultValdiation.ID)
		private Long id;
	}

	@Getter
	@Setter
	public static class DeleteTravelVisaResponse extends TravelMutationResponse {}

	@Getter
	@Setter
	public static class GetTravelVisaRequest {
		@NotNull(message = ApiDefaultValdiation.ID)
		private Long id;
	}

	@Getter
	@Setter
	public static class GetTravelVisaResponse {
		private Long id;
		private Long clientId;
		private Long bookingId;
		private String country;
		private String visaType;
		private String status;
		private LocalDateTime submittedAt;
		private LocalDateTime decisionAt;
		private String notes;
	}

	@Getter
	@Setter
	public static class GetsTravelVisasRequest extends PageRequest {}
}
