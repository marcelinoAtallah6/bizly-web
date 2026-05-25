package com.travel.api.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.travel.security.BusinessContextHolder;

import com.travel.api.dto.document.TravelDocumentDtos.*;
import com.travel.api.service.document.ITravelDocumentService;
import com.travel.api.service.document.TravelDocumentStorageService;
import com.travel.common.ApiMessages;
import com.travel.common.ApiResponse;
import com.travel.common.PageResponse;
import com.travel.security.MenuPermissionAction;
import com.travel.security.RequireMenuPermission;

@RestController
@RequestMapping("/document")
public class TravelDocumentController {

	private static final Logger log = LogManager.getLogger(TravelDocumentController.class);

	@Autowired
	private ITravelDocumentService service;

	@Autowired
	private TravelDocumentStorageService storageService;

	@PostMapping("/add")
	@RequireMenuPermission(menuRoute = "/travel/documents", action = MenuPermissionAction.ADD)
	public @ResponseBody ResponseEntity<ApiResponse<AddTravelDocumentResponse>> add(HttpServletRequest httpRequest,
			@RequestBody @Valid AddTravelDocumentRequest request) {
		log.info("[TRAVEL_DOCUMENT][ADD] docType={}", request.getDocType());
		return ResponseEntity.ok(ApiResponse.success(service.add(request, httpRequest.getHeader("X-User")),
				ApiMessages.DOCUMENT_ADDED));
	}

	@PostMapping("/update")
	@RequireMenuPermission(menuRoute = "/travel/documents", action = MenuPermissionAction.EDIT)
	public @ResponseBody ResponseEntity<ApiResponse<UpdateTravelDocumentResponse>> update(HttpServletRequest httpRequest,
			@RequestBody @Valid UpdateTravelDocumentRequest request) {
		log.info("[TRAVEL_DOCUMENT][UPDATE] id={}", request.getId());
		return ResponseEntity.ok(ApiResponse.success(service.update(request, httpRequest.getHeader("X-User")),
				ApiMessages.DOCUMENT_UPDATED));
	}

	@PostMapping("/delete")
	@RequireMenuPermission(menuRoute = "/travel/documents", action = MenuPermissionAction.DELETE)
	public @ResponseBody ResponseEntity<ApiResponse<DeleteTravelDocumentResponse>> delete(
			@RequestBody @Valid DeleteTravelDocumentRequest request) {
		log.info("[TRAVEL_DOCUMENT][DELETE] id={}", request.getId());
		return ResponseEntity.ok(ApiResponse.success(service.delete(request), ApiMessages.DOCUMENT_DELETED));
	}

	@PostMapping("/get")
	@RequireMenuPermission(menuRoute = "/travel/documents", action = MenuPermissionAction.VIEW)
	public @ResponseBody ResponseEntity<ApiResponse<GetTravelDocumentResponse>> get(
			@RequestBody @Valid GetTravelDocumentRequest request) {
		return ResponseEntity.ok(ApiResponse.success(service.get(request), ApiMessages.SUCCESS));
	}

	@PostMapping("/gets")
	@RequireMenuPermission(menuRoute = "/travel/documents", action = MenuPermissionAction.VIEW)
	public @ResponseBody ResponseEntity<ApiResponse<PageResponse<GetTravelDocumentResponse>>> gets(
			@RequestBody @Valid GetsTravelDocumentsRequest request) {
		return ResponseEntity.ok(ApiResponse.success(service.gets(request), ApiMessages.SUCCESS));
	}

	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@RequireMenuPermission(menuRoute = "/travel/documents", action = MenuPermissionAction.ADD)
	public @ResponseBody ResponseEntity<ApiResponse<UploadTravelFileResponse>> upload(
			@RequestPart("file") MultipartFile file) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		var stored = storageService.store(businessId, file);
		UploadTravelFileResponse res = new UploadTravelFileResponse();
		res.setStorageRef(stored.getStorageRef());
		res.setFileName(stored.getFileName());
		res.setContentType(stored.getContentType());
		log.info("[TRAVEL_DOCUMENT][UPLOAD] ref={}", stored.getStorageRef());
		return ResponseEntity.ok(ApiResponse.success(res, ApiMessages.SUCCESS));
	}

	@PostMapping("/download")
	@RequireMenuPermission(menuRoute = "/travel/documents", action = MenuPermissionAction.VIEW)
	public ResponseEntity<Resource> download(@RequestBody @Valid GetTravelDocumentRequest request) {
		GetTravelDocumentResponse doc = service.get(request);
		Resource resource = storageService.loadAsResource(doc.getStorageRef());
		String fileName = doc.getFileName() != null ? doc.getFileName() : "document";
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(resource);
	}

	@PostMapping("/view")
	@RequireMenuPermission(menuRoute = "/travel/documents", action = MenuPermissionAction.VIEW)
	public ResponseEntity<Resource> view(@RequestBody @Valid GetTravelDocumentRequest request) {
		GetTravelDocumentResponse doc = service.get(request);
		Resource resource = storageService.loadAsResource(doc.getStorageRef());
		String fileName = doc.getFileName() != null ? doc.getFileName() : "document";
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(resource);
	}

	@PostMapping("/download-ref")
	@RequireMenuPermission(menuRoute = "/travel/clients", action = MenuPermissionAction.VIEW)
	public ResponseEntity<Resource> downloadByRef(@RequestBody @Valid StorageRefRequest request) {
		Resource resource = storageService.loadAsResource(request.getStorageRef());
		String fileName = fileNameFromRef(request.getStorageRef());
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(resource);
	}

	@PostMapping("/view-ref")
	@RequireMenuPermission(menuRoute = "/travel/clients", action = MenuPermissionAction.VIEW)
	public ResponseEntity<Resource> viewByRef(@RequestBody @Valid StorageRefRequest request) {
		Resource resource = storageService.loadAsResource(request.getStorageRef());
		String fileName = fileNameFromRef(request.getStorageRef());
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(resource);
	}

	private static String fileNameFromRef(String storageRef) {
		if (storageRef == null) {
			return "document";
		}
		int i = storageRef.lastIndexOf('/');
		return i >= 0 ? storageRef.substring(i + 1) : storageRef;
	}
}
