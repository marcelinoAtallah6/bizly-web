package com.travel.api.dto.payment;

import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;

import com.travel.api.dto.common.TravelMutationResponse;
import com.travel.common.ApiDefaultValdiation;
import com.travel.api.dto.common.TravelMutationResponse;
import com.travel.common.PageRequest;

import lombok.Getter;
import lombok.Setter;

public final class TravelPaymentDtos {

	private TravelPaymentDtos() {}

	@Getter
	@Setter
	public static class AddTravelPaymentRequest {
		@NotNull(message = ApiDefaultValdiation.ID)
		private Long invoiceId;
		@NotNull(message = ApiDefaultValdiation.PRICE)
		private Double amount;
		private String paymentMethod;
		private String referenceNo;
	}

	@Getter
	@Setter
	public static class AddTravelPaymentResponse {
		private Long id;
	}

	@Getter
	@Setter
	public static class UpdateTravelPaymentRequest {
		@NotNull(message = ApiDefaultValdiation.ID)
		private Long id;
		@NotNull(message = ApiDefaultValdiation.ID)
		private Long invoiceId;
		@NotNull(message = ApiDefaultValdiation.PRICE)
		private Double amount;
		private String paymentMethod;
		private String referenceNo;
	}

	@Getter
	@Setter
	public static class UpdateTravelPaymentResponse extends TravelMutationResponse {}

	@Getter
	@Setter
	public static class DeleteTravelPaymentRequest {
		@NotNull(message = ApiDefaultValdiation.ID)
		private Long id;
	}

	@Getter
	@Setter
	public static class DeleteTravelPaymentResponse extends TravelMutationResponse {}

	@Getter
	@Setter
	public static class GetTravelPaymentRequest {
		@NotNull(message = ApiDefaultValdiation.ID)
		private Long id;
	}

	@Getter
	@Setter
	public static class GetTravelPaymentResponse {
		private Long id;
		private Long invoiceId;
		private Double amount;
		private String paymentMethod;
		private LocalDateTime paidAt;
		private String referenceNo;
	}

	@Getter
	@Setter
	public static class GetsTravelPaymentsRequest extends PageRequest {}
}
