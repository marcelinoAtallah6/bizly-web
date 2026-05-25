package com.travel.api.dto.document;

import java.time.LocalDateTime;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.travel.api.dto.common.TravelMutationResponse;
import com.travel.common.ApiDefaultValdiation;
import com.travel.common.PageRequest;

import lombok.Getter;
import lombok.Setter;

public final class TravelDocumentDtos {

	private TravelDocumentDtos() {}

	@Getter
	@Setter
	public static class AddTravelDocumentRequest {
		private Long clientId;
		private Long bookingId;
		@NotBlank(message = ApiDefaultValdiation.NAME)
		private String docType;
		private String fileName;
		private String storageRef;
		private String uploadedBy;
	}

	@Getter
	@Setter
	public static class AddTravelDocumentResponse {
		private Long id;
	}

	@Getter
	@Setter
	public static class UpdateTravelDocumentRequest {
		@NotNull(message = ApiDefaultValdiation.ID)
		private Long id;
		private Long clientId;
		private Long bookingId;
		@NotBlank(message = ApiDefaultValdiation.NAME)
		private String docType;
		private String fileName;
		private String storageRef;
		private String uploadedBy;
	}

	@Getter
	@Setter
	public static class UpdateTravelDocumentResponse extends TravelMutationResponse {}

	@Getter
	@Setter
	public static class DeleteTravelDocumentRequest {
		@NotNull(message = ApiDefaultValdiation.ID)
		private Long id;
	}

	@Getter
	@Setter
	public static class DeleteTravelDocumentResponse extends TravelMutationResponse {}

	@Getter
	@Setter
	public static class GetTravelDocumentRequest {
		@NotNull(message = ApiDefaultValdiation.ID)
		private Long id;
	}

	@Getter
	@Setter
	public static class GetTravelDocumentResponse {
		private Long id;
		private Long clientId;
		private Long bookingId;
		private String docType;
		private String fileName;
		private String storageRef;
		private LocalDateTime uploadedAt;
		private String uploadedBy;
	}

	@Getter
	@Setter
	public static class GetsTravelDocumentsRequest extends PageRequest {}

	@Getter
	@Setter
	public static class UploadTravelFileResponse {
		private String storageRef;
		private String fileName;
		private String contentType;
	}

	@Getter
	@Setter
	public static class StorageRefRequest {
		@NotBlank(message = ApiDefaultValdiation.NAME)
		private String storageRef;
	}
}
