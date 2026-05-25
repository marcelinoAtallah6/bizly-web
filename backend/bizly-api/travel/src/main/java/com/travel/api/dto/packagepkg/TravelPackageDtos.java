package com.travel.api.dto.packagepkg;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import java.util.List;

import com.travel.api.dto.packagepkg.TravelPackageDetailJson.ItineraryDayDto;
import com.travel.api.dto.packagepkg.TravelPackageDetailJson.MediaItemDto;
import com.travel.api.dto.packagepkg.TravelPackageDetailJson.PackageAddonDto;
import com.travel.api.dto.packagepkg.TravelPackageDetailJson.PackageDestinationDto;
import com.travel.api.dto.common.TravelMutationResponse;
import com.travel.common.ApiDefaultValdiation;
import com.travel.api.dto.common.TravelMutationResponse;
import com.travel.common.PageRequest;

import lombok.Getter;
import lombok.Setter;

public final class TravelPackageDtos {

	private TravelPackageDtos() {}

	@Getter
	@Setter
	public static class AddTravelPackageRequest {
		private String code;
		@NotBlank(message = ApiDefaultValdiation.NAME)
		private String name;
		private String description;
		private String inclusions;
		private String exclusions;
		private List<ItineraryDayDto> itinerary;
		private List<MediaItemDto> media;
		private List<PackageDestinationDto> destinations;
		private List<PackageAddonDto> addons;
		private Integer maxCapacityPerDay;
		private String destination;
		private Integer durationDays;
		@NotNull(message = ApiDefaultValdiation.PRICE)
		private Double basePrice;
		private String currency;
		private Boolean active;
	}

	@Getter
	@Setter
	public static class AddTravelPackageResponse {
		private Long id;
	}

	@Getter
	@Setter
	public static class UpdateTravelPackageRequest {
		@NotNull(message = ApiDefaultValdiation.ID)
		private Long id;
		private String code;
		@NotBlank(message = ApiDefaultValdiation.NAME)
		private String name;
		private String description;
		private String inclusions;
		private String exclusions;
		private List<ItineraryDayDto> itinerary;
		private List<MediaItemDto> media;
		private List<PackageDestinationDto> destinations;
		private List<PackageAddonDto> addons;
		private Integer maxCapacityPerDay;
		private String destination;
		private Integer durationDays;
		@NotNull(message = ApiDefaultValdiation.PRICE)
		private Double basePrice;
		private String currency;
		private Boolean active;
	}

	@Getter
	@Setter
	public static class UpdateTravelPackageResponse extends TravelMutationResponse {}

	@Getter
	@Setter
	public static class DeleteTravelPackageRequest {
		@NotNull(message = ApiDefaultValdiation.ID)
		private Long id;
	}

	@Getter
	@Setter
	public static class DeleteTravelPackageResponse extends TravelMutationResponse {}

	@Getter
	@Setter
	public static class GetTravelPackageRequest {
		@NotNull(message = ApiDefaultValdiation.ID)
		private Long id;
	}

	@Getter
	@Setter
	public static class GetTravelPackageResponse {
		private Long id;
		private String code;
		private String name;
		private String description;
		private String inclusions;
		private String exclusions;
		private List<ItineraryDayDto> itinerary;
		private List<MediaItemDto> media;
		private List<PackageDestinationDto> destinations;
		private List<PackageAddonDto> addons;
		private Integer maxCapacityPerDay;
		private String destination;
		private Integer durationDays;
		private Double basePrice;
		private String currency;
		private Boolean active;
	}

	@Getter
	@Setter
	public static class GetsTravelPackagesRequest extends PageRequest {}
}
