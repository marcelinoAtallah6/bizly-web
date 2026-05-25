package com.travel.api.dto.triprequest;

import java.time.LocalDate;

import javax.validation.constraints.NotNull;

import com.travel.api.dto.common.TravelMutationResponse;
import com.travel.common.ApiDefaultValdiation;
import com.travel.api.dto.common.TravelMutationResponse;
import com.travel.common.PageRequest;

import lombok.Getter;
import lombok.Setter;

public final class TravelTripRequestDtos {

	private TravelTripRequestDtos() {}

	@Getter
	@Setter
	public static class AddTravelTripRequestRequest {
		@NotNull(message = ApiDefaultValdiation.CUSTOMER_ID)
		private Long clientId;
		private Long destinationId;
		private String destinationName;
		private LocalDate departureDate;
		private LocalDate returnDate;
		private Double budgetAmount;
		private String currency;
		private Integer travelerCount;
		private String travelerDetails;
		private String notes;
		private String assignedTo;
	}

	@Getter
	@Setter
	public static class AddTravelTripRequestResponse {
		private Long id;
	}

	@Getter
	@Setter
	public static class UpdateTravelTripRequestRequest {
		@NotNull(message = ApiDefaultValdiation.ID)
		private Long id;
		@NotNull(message = ApiDefaultValdiation.CUSTOMER_ID)
		private Long clientId;
		private Long destinationId;
		private String destinationName;
		private LocalDate departureDate;
		private LocalDate returnDate;
		private Double budgetAmount;
		private String currency;
		private Integer travelerCount;
		private String travelerDetails;
		private String notes;
		private String assignedTo;
	}

	@Getter
	@Setter
	public static class UpdateTravelTripRequestResponse extends TravelMutationResponse {}

	@Getter
	@Setter
	public static class DeleteTravelTripRequestRequest {
		@NotNull(message = ApiDefaultValdiation.ID)
		private Long id;
	}

	@Getter
	@Setter
	public static class DeleteTravelTripRequestResponse extends TravelMutationResponse {}

	@Getter
	@Setter
	public static class GetTravelTripRequestRequest {
		@NotNull(message = ApiDefaultValdiation.ID)
		private Long id;
	}

	@Getter
	@Setter
	public static class GetTravelTripRequestResponse {
		private Long id;
		private Long clientId;
		private Long destinationId;
		private String destinationName;
		private LocalDate departureDate;
		private LocalDate returnDate;
		private Double budgetAmount;
		private String currency;
		private Integer travelerCount;
		private String travelerDetails;
		private String status;
		private Double quotedAmount;
		private String notes;
		private String assignedTo;
		private Long bookingId;
	}

	@Getter
	@Setter
	public static class GetsTravelTripRequestsRequest extends PageRequest {}

	@Getter
	@Setter
	public static class QuoteTravelTripRequestRequest {
		@NotNull(message = ApiDefaultValdiation.ID)
		private Long id;
		@NotNull(message = ApiDefaultValdiation.PRICE)
		private Double quotedAmount;
	}

	@Getter
	@Setter
	public static class QuoteTravelTripRequestResponse {}

	@Getter
	@Setter
	public static class AcceptTravelTripRequestRequest {
		@NotNull(message = ApiDefaultValdiation.ID)
		private Long id;
	}

	@Getter
	@Setter
	public static class AcceptTravelTripRequestResponse {}

	@Getter
	@Setter
	public static class RejectTravelTripRequestRequest {
		@NotNull(message = ApiDefaultValdiation.ID)
		private Long id;
	}

	@Getter
	@Setter
	public static class RejectTravelTripRequestResponse {}

	@Getter
	@Setter
	public static class ConvertTravelTripRequestRequest {
		@NotNull(message = ApiDefaultValdiation.ID)
		private Long id;
	}

	@Getter
	@Setter
	public static class ConvertTravelTripRequestResponse {
		private Long bookingId;
	}
}
