package com.travel.api.service.document;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.travel.exception.ServiceException;

@Service
public class TravelDocumentStorageService {

	@Value("${travel.upload.base-dir:uploads}")
	private String uploadBaseDir;

	public StoredFile store(Long businessId, MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new ServiceException("File is required.", HttpStatus.BAD_REQUEST);
		}
		String original = file.getOriginalFilename();
		String safeName = sanitizeFileName(original != null ? original : "document.bin");
		String relative = "travel/" + businessId + "/" + UUID.randomUUID() + "_" + safeName;
		Path target = resolvePath(relative);
		try {
			Files.createDirectories(target.getParent());
			try (InputStream in = file.getInputStream()) {
				Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException ex) {
			throw new ServiceException("Unable to save uploaded file.", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new StoredFile(relative, safeName, file.getContentType());
	}

	public Resource loadAsResource(String storageRef) {
		if (storageRef == null || storageRef.isBlank()) {
			throw new ServiceException("Document file reference is missing.", HttpStatus.NOT_FOUND);
		}
		String ref = storageRef.trim().replace('\\', '/');
		if (ref.startsWith("http://") || ref.startsWith("https://")) {
			throw new ServiceException("Remote URLs must be opened directly.", HttpStatus.BAD_REQUEST);
		}
		Path file = resolvePath(ref);
		if (!Files.isRegularFile(file)) {
			throw new ServiceException("File not found on server: " + ref, HttpStatus.NOT_FOUND);
		}
		try {
			byte[] bytes = Files.readAllBytes(file);
			return new ByteArrayResource(bytes) {
				@Override
				public String getFilename() {
					return file.getFileName().toString();
				}
			};
		} catch (IOException ex) {
			throw new ServiceException("Unable to read file.", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private Path resolvePath(String relative) {
		Path base = Paths.get(uploadBaseDir).toAbsolutePath().normalize();
		Path file = base.resolve(relative.startsWith("/") ? relative.substring(1) : relative).normalize();
		if (!file.startsWith(base)) {
			throw new ServiceException("Invalid file path.", HttpStatus.BAD_REQUEST);
		}
		return file;
	}

	private static String sanitizeFileName(String name) {
		String cleaned = name.replaceAll("[^a-zA-Z0-9._-]", "_");
		if (cleaned.isBlank()) {
			return "file.bin";
		}
		return cleaned.length() > 180 ? cleaned.substring(cleaned.length() - 180) : cleaned;
	}

	public static final class StoredFile {
		private final String storageRef;
		private final String fileName;
		private final String contentType;

		public StoredFile(String storageRef, String fileName, String contentType) {
			this.storageRef = storageRef;
			this.fileName = fileName;
			this.contentType = contentType;
		}

		public String getStorageRef() {
			return storageRef;
		}

		public String getFileName() {
			return fileName;
		}

		public String getContentType() {
			return contentType;
		}
	}
}
