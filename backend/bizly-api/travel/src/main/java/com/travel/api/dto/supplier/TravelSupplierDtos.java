package com.travel.api.dto.supplier;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.travel.api.dto.common.TravelMutationResponse;
import com.travel.common.ApiDefaultValdiation;
import com.travel.api.dto.common.TravelMutationResponse;
import com.travel.common.PageRequest;

import lombok.Getter;
import lombok.Setter;

public final class TravelSupplierDtos {

	private TravelSupplierDtos() {}

	@Getter
	@Setter
	public static class AddTravelSupplierRequest {
		@NotBlank(message = ApiDefaultValdiation.NAME)
		private String name;
		private String supplierType;
		private String contactEmail;
		private String contactPhone;
		private String contractsJson;
		private String commissionNotes;
		private String rateTableNotes;
		private Boolean active;
	}

	@Getter
	@Setter
	public static class AddTravelSupplierResponse {
		private Long id;
	}

	@Getter
	@Setter
	public static class UpdateTravelSupplierRequest {
		@NotNull(message = ApiDefaultValdiation.ID)
		private Long id;
		@NotBlank(message = ApiDefaultValdiation.NAME)
		private String name;
		private String supplierType;
		private String contactEmail;
		private String contactPhone;
		private String contractsJson;
		private String commissionNotes;
		private String rateTableNotes;
		private Boolean active;
	}

	@Getter
	@Setter
	public static class UpdateTravelSupplierResponse extends TravelMutationResponse {}

	@Getter
	@Setter
	public static class DeleteTravelSupplierRequest {
		@NotNull(message = ApiDefaultValdiation.ID)
		private Long id;
	}

	@Getter
	@Setter
	public static class DeleteTravelSupplierResponse extends TravelMutationResponse {}

	@Getter
	@Setter
	public static class GetTravelSupplierRequest {
		@NotNull(message = ApiDefaultValdiation.ID)
		private Long id;
	}

	@Getter
	@Setter
	public static class GetTravelSupplierResponse {
		private Long id;
		private String name;
		private String supplierType;
		private String contactEmail;
		private String contactPhone;
		private String contractsJson;
		private String commissionNotes;
		private String rateTableNotes;
		private Boolean active;
	}

	@Getter
	@Setter
	public static class GetsTravelSuppliersRequest extends PageRequest {}
}
