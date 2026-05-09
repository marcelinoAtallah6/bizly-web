package com.pm.common;

import java.util.Base64;
import java.util.Locale;
import java.util.Set;

import org.springframework.http.HttpStatus;

import com.pm.exception.ServiceException;

/** Validates MIME and decodes Base64 image payloads for products (same rules as profile photos). */
public final class ProductImageUtil {

	private static final int MAX_BYTES = 2 * 1024 * 1024;

	private static final Set<String> ALLOWED_MIME = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");

	private ProductImageUtil() {
	}

	public static void validateMime(String mimeType) {
		if (mimeType == null || mimeType.isBlank()) {
			throw new ServiceException(ApiMessages.INVALID_PRODUCT_IMAGE, HttpStatus.BAD_REQUEST);
		}
		String m = mimeType.trim().toLowerCase(Locale.ROOT);
		if (!ALLOWED_MIME.contains(m)) {
			throw new ServiceException(ApiMessages.INVALID_PRODUCT_IMAGE, HttpStatus.BAD_REQUEST);
		}
	}

	public static byte[] decodeBase64Image(String base64Payload) {
		if (base64Payload == null || base64Payload.isBlank()) {
			throw new ServiceException(ApiMessages.INVALID_PRODUCT_IMAGE, HttpStatus.BAD_REQUEST);
		}
		String s = base64Payload.trim();
		if (s.startsWith("data:")) {
			int comma = s.indexOf(',');
			if (comma > 0) {
				s = s.substring(comma + 1);
			}
		}
		try {
			byte[] raw = Base64.getDecoder().decode(s);
			if (raw.length > MAX_BYTES) {
				throw new ServiceException(ApiMessages.PRODUCT_IMAGE_TOO_LARGE, HttpStatus.BAD_REQUEST);
			}
			return raw;
		} catch (IllegalArgumentException e) {
			throw new ServiceException(ApiMessages.INVALID_PRODUCT_IMAGE, HttpStatus.BAD_REQUEST);
		}
	}
}
