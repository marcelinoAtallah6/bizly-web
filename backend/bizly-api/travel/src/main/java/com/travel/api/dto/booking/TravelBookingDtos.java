package com.travel.api.dto.booking;

import java.time.LocalDate;

import javax.validation.constraints.NotNull;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.travel.api.dto.packagepkg.TravelPackageDetailJson.PaymentScheduleItemDto;
import com.travel.api.dto.common.TravelMutationResponse;
import com.travel.common.ApiDefaultValdiation;
import com.travel.api.dto.common.TravelMutationResponse;
import com.travel.common.PageRequest;

import lombok.Getter;
import lombok.Setter;

public final class TravelBookingDtos {

	private TravelBookingDtos() {
	}

	@Getter
	@Setter
	public static class AddTravelBookingRequest {
		@NotNull(message = ApiDefaultValdiation.CUSTOMER_ID)
		private Long clientId;
		private Long packageId;
		private String referenceNo;
		private String status;
		private LocalDate departureDate;
		private LocalDate returnDate;
		private Double totalAmount;
		private String currency;
		private String notes;
		private String timelineStage;
		private String approvalStatus;
		private List<PaymentScheduleItemDto> paymentSchedule;
		private Boolean requiresApproval;
	}

	@Getter
	@Setter
	public static class AddTravelBookingResponse {
		private Long id;
	}

	@Getter
	@Setter
	public static class UpdateTravelBookingRequest {
		@NotNull(message = ApiDefaultValdiation.ID)
		private Long id;
		@NotNull(message = ApiDefaultValdiation.CUSTOMER_ID)
		private Long clientId;
		private Long packageId;
		private String referenceNo;
		private String status;
		private LocalDate departureDate;
		private LocalDate returnDate;
		private Double totalAmount;
		private String currency;
		private String notes;
		private String timelineStage;
		private String approvalStatus;
		private List<PaymentScheduleItemDto> paymentSchedule;
		private Boolean requiresApproval;
	}

	@Getter
	@Setter
	public static class UpdateTravelBookingResponse extends TravelMutationResponse {}

	@Getter
	@Setter
	public static class DeleteTravelBookingRequest {
		@NotNull(message = ApiDefaultValdiation.ID)
		private Long id;
	}

	@Getter
	@Setter
	public static class DeleteTravelBookingResponse extends TravelMutationResponse {}

	@Getter
	@Setter
	public static class GetTravelBookingRequest {
		@NotNull(message = ApiDefaultValdiation.ID)
		private Long id;
	}

	@Getter
	@Setter
	public static class GetTravelBookingResponse {
		private Long id;
		private Long clientId;
		private Long packageId;
		private String referenceNo;
		private String status;
		private LocalDate departureDate;
		private LocalDate returnDate;
		private Double totalAmount;
		private String currency;
		private String notes;
		private String timelineStage;
		private String approvalStatus;
		private List<PaymentScheduleItemDto> paymentSchedule;
		private Boolean requiresApproval;
	}

	@Getter
	@Setter
	public static class GetsTravelBookingsRequest extends PageRequest {
	}

	/** Month grid for package departure calendar (per-day availability). */
	@Getter
	@Setter
	public static class BookingAvailabilityRequest {
		private Long packageId;
		@NotNull
		private Integer year;
		@NotNull
		private Integer month;
		/** When editing, exclude this booking from occupancy. */
		private Long excludeBookingId;
	}

	@Getter
	@Setter
	public static class BookingAvailabilityDayResponse {
		private LocalDate date;
		/** AVAILABLE, PENDING, BOOKED, UNAVAILABLE */
		private String status;
	}

	@Getter
	@Setter
	public static class BookingAvailabilityResponse {
		private java.util.List<BookingAvailabilityDayResponse> days = new java.util.ArrayList<>();
	}
}
