package com.travel.api.dto.followup;

import java.time.LocalDateTime;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.travel.api.dto.common.TravelMutationResponse;
import com.travel.common.ApiDefaultValdiation;
import com.travel.api.dto.common.TravelMutationResponse;
import com.travel.common.PageRequest;

import lombok.Getter;
import lombok.Setter;

public final class TravelFollowUpDtos {

	private TravelFollowUpDtos() {}

	@Getter
	@Setter
	public static class AddTravelFollowUpRequest {
		private Long clientId;
		private Long bookingId;
		@NotBlank(message = ApiDefaultValdiation.TITLE)
		private String subject;
		private LocalDateTime dueAt;
		private String status;
		private String notes;
		private String assignedTo;
	}

	@Getter
	@Setter
	public static class AddTravelFollowUpResponse {
		private Long id;
	}

	@Getter
	@Setter
	public static class UpdateTravelFollowUpRequest {
		@NotNull(message = ApiDefaultValdiation.ID)
		private Long id;
		private Long clientId;
		private Long bookingId;
		@NotBlank(message = ApiDefaultValdiation.TITLE)
		private String subject;
		private LocalDateTime dueAt;
		private String status;
		private String notes;
		private String assignedTo;
	}

	@Getter
	@Setter
	public static class UpdateTravelFollowUpResponse extends TravelMutationResponse {}

	@Getter
	@Setter
	public static class DeleteTravelFollowUpRequest {
		@NotNull(message = ApiDefaultValdiation.ID)
		private Long id;
	}

	@Getter
	@Setter
	public static class DeleteTravelFollowUpResponse extends TravelMutationResponse {}

	@Getter
	@Setter
	public static class GetTravelFollowUpRequest {
		@NotNull(message = ApiDefaultValdiation.ID)
		private Long id;
	}

	@Getter
	@Setter
	public static class GetTravelFollowUpResponse {
		private Long id;
		private Long clientId;
		private Long bookingId;
		private String subject;
		private LocalDateTime dueAt;
		private String status;
		private String notes;
		private String assignedTo;
	}

	@Getter
	@Setter
	public static class GetsTravelFollowUpsRequest extends PageRequest {}
}
