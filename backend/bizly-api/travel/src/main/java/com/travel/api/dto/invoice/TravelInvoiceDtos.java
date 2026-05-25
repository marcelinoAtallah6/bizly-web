package com.travel.api.dto.invoice;

import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;

import com.travel.api.dto.common.TravelMutationResponse;
import com.travel.common.ApiDefaultValdiation;
import com.travel.api.dto.common.TravelMutationResponse;
import com.travel.common.PageRequest;

import lombok.Getter;
import lombok.Setter;

public final class TravelInvoiceDtos {

	private TravelInvoiceDtos() {}

	@Getter
	@Setter
	public static class AddTravelInvoiceRequest {
		@NotNull(message = ApiDefaultValdiation.ID)
		private Long bookingId;
		private String invoiceNo;
		@NotNull(message = ApiDefaultValdiation.PRICE)
		private Double amount;
		private String currency;
		private String status;
		private LocalDateTime issuedAt;
		private LocalDateTime dueAt;
	}

	@Getter
	@Setter
	public static class AddTravelInvoiceResponse {
		private Long id;
	}

	@Getter
	@Setter
	public static class UpdateTravelInvoiceRequest {
		@NotNull(message = ApiDefaultValdiation.ID)
		private Long id;
		@NotNull(message = ApiDefaultValdiation.ID)
		private Long bookingId;
		private String invoiceNo;
		@NotNull(message = ApiDefaultValdiation.PRICE)
		private Double amount;
		private String currency;
		private String status;
		private LocalDateTime issuedAt;
		private LocalDateTime dueAt;
	}

	@Getter
	@Setter
	public static class UpdateTravelInvoiceResponse extends TravelMutationResponse {}

	@Getter
	@Setter
	public static class DeleteTravelInvoiceRequest {
		@NotNull(message = ApiDefaultValdiation.ID)
		private Long id;
	}

	@Getter
	@Setter
	public static class DeleteTravelInvoiceResponse extends TravelMutationResponse {}

	@Getter
	@Setter
	public static class GetTravelInvoiceRequest {
		@NotNull(message = ApiDefaultValdiation.ID)
		private Long id;
	}

	@Getter
	@Setter
	public static class GetTravelInvoiceResponse {
		private Long id;
		private Long bookingId;
		private String invoiceNo;
		private Double amount;
		private String currency;
		private String status;
		private LocalDateTime issuedAt;
		private LocalDateTime dueAt;
	}

	@Getter
	@Setter
	public static class GetsTravelInvoicesRequest extends PageRequest {}
}
