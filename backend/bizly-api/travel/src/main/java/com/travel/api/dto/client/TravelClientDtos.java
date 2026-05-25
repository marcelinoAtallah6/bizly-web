package com.travel.api.dto.client;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.travel.api.dto.client.TravelClientProfileJson.TravelClientProfileDto;
import com.travel.api.dto.common.TravelMutationResponse;
import com.travel.common.ApiDefaultValdiation;
import com.travel.api.dto.common.TravelMutationResponse;
import com.travel.common.PageRequest;

import lombok.Getter;
import lombok.Setter;

public final class TravelClientDtos {

	private TravelClientDtos() {}

	@Getter
	@Setter
	public static class AddTravelClientRequest {
		@NotBlank(message = ApiDefaultValdiation.NAME)
		private String fullName;
		private String email;
		private String phone;
		private String passportNo;
		private String notes;
		private TravelClientProfileDto travelProfile;
		private Boolean active;
	}

	@Getter
	@Setter
	public static class AddTravelClientResponse {
		private Long id;
	}

	@Getter
	@Setter
	public static class UpdateTravelClientRequest {
		@NotNull(message = ApiDefaultValdiation.ID)
		private Long id;
		@NotBlank(message = ApiDefaultValdiation.NAME)
		private String fullName;
		private String email;
		private String phone;
		private String passportNo;
		private String notes;
		private TravelClientProfileDto travelProfile;
		private Boolean active;
	}

	@Getter
	@Setter
	public static class UpdateTravelClientResponse extends TravelMutationResponse {}

	@Getter
	@Setter
	public static class DeleteTravelClientRequest {
		@NotNull(message = ApiDefaultValdiation.ID)
		private Long id;
	}

	@Getter
	@Setter
	public static class DeleteTravelClientResponse extends TravelMutationResponse {}

	@Getter
	@Setter
	public static class GetTravelClientRequest {
		@NotNull(message = ApiDefaultValdiation.ID)
		private Long id;
	}

	@Getter
	@Setter
	public static class GetTravelClientResponse {
		private Long id;
		private String fullName;
		private String email;
		private String phone;
		private String passportNo;
		private String notes;
		private TravelClientProfileDto travelProfile;
		private Boolean active;
	}

	@Getter
	@Setter
	public static class GetsTravelClientsRequest extends PageRequest {}
}
