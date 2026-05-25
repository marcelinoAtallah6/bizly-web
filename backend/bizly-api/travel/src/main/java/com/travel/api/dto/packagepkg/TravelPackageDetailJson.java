package com.travel.api.dto.packagepkg;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/** Serialized to {@code travel_package.itinerary_json} / {@code media_json}. */
public final class TravelPackageDetailJson {

	private TravelPackageDetailJson() {}

	@Getter
	@Setter
	public static class ItineraryDayDto {
		private Integer dayNumber;
		private String title;
		private String details;
	}

	@Getter
	@Setter
	public static class MediaItemDto {
		private String fileName;
		private String storageRef;
	}

	@Getter
	@Setter
	public static class PackageDestinationDto {
		private Long destinationId;
		private String destinationName;
		private Integer nights;
	}

	@Getter
	@Setter
	public static class PackageAddonDto {
		private String code;
		private String name;
		private String description;
		private Double price;
	}

	@Getter
	@Setter
	public static class PaymentScheduleItemDto {
		private java.time.LocalDate dueDate;
		private Double amount;
		private String label;
		private Boolean paid;
	}

	public static List<ItineraryDayDto> emptyItinerary() {
		return new ArrayList<>();
	}

	public static List<MediaItemDto> emptyMedia() {
		return new ArrayList<>();
	}
}
