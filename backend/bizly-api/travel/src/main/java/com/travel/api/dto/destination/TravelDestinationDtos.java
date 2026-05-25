package com.travel.api.dto.destination;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.travel.api.dto.common.TravelMutationResponse;
import com.travel.common.ApiDefaultValdiation;
import com.travel.common.PageRequest;

import lombok.Getter;
import lombok.Setter;

public final class TravelDestinationDtos {

	private TravelDestinationDtos() {}

	@Getter
	@Setter
	public static class AddTravelDestinationRequest {
		@NotBlank(message = ApiDefaultValdiation.NAME)
		private String name;
		private String country;
		private String region;
		private String visaRequirements;
		private String healthRequirements;
		private String highSeasonNotes;
		private String lowSeasonNotes;
		private String travelWarnings;
		private String riskLevel;
		private String travelAdvisory;
		private Boolean active;
	}

	@Getter
	@Setter
	public static class AddTravelDestinationResponse {
		private Long id;
	}

	@Getter
	@Setter
	public static class UpdateTravelDestinationRequest {
		@NotNull(message = ApiDefaultValdiation.ID)
		private Long id;
		@NotBlank(message = ApiDefaultValdiation.NAME)
		private String name;
		private String country;
		private String region;
		private String visaRequirements;
		private String healthRequirements;
		private String highSeasonNotes;
		private String lowSeasonNotes;
		private String travelWarnings;
		private String riskLevel;
		private String travelAdvisory;
		private Boolean active;
	}

	@Getter
	@Setter
	public static class UpdateTravelDestinationResponse extends TravelMutationResponse {}

	@Getter
	@Setter
	public static class DeleteTravelDestinationRequest {
		@NotNull(message = ApiDefaultValdiation.ID)
		private Long id;
	}

	public static class DeleteTravelDestinationResponse extends TravelMutationResponse {}

	@Getter
	@Setter
	public static class GetTravelDestinationRequest {
		@NotNull(message = ApiDefaultValdiation.ID)
		private Long id;
	}

	@Getter
	@Setter
	public static class GetTravelDestinationResponse {
		private Long id;
		private String name;
		private String country;
		private String region;
		private String visaRequirements;
		private String healthRequirements;
		private String highSeasonNotes;
		private String lowSeasonNotes;
		private String travelWarnings;
		private String riskLevel;
		private String travelAdvisory;
		private Boolean active;
	}

	@Getter
	@Setter
	public static class GetsTravelDestinationsRequest extends PageRequest {}
}
