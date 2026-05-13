package com.auth.api.controllers.dto.registration;

/**
 * Response of {@code POST /auth/me/avatar}. Lets the SPA pull the navbar avatar on demand when the
 * JWT couldn't embed it ({@code profileImageInJwt = false}). Keeps the access token small while
 * still letting the header show a real picture for every user, regardless of upload size.
 */
public class MeAvatarResponse {

	private Long userId;
	/** MIME type (e.g. {@code image/jpeg}); empty / {@code null} when the user has no avatar. */
	private String mime;
	/** Raw Base64 of the stored bytes (no {@code data:} prefix); empty / {@code null} when none. */
	private String base64;

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getMime() {
		return mime;
	}

	public void setMime(String mime) {
		this.mime = mime;
	}

	public String getBase64() {
		return base64;
	}

	public void setBase64(String base64) {
		this.base64 = base64;
	}
}
