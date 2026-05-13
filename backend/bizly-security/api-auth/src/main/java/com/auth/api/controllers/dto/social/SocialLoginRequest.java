package com.auth.api.controllers.dto.social;

/**
 * Payload for {@code POST /auth/social/{provider}}. The {@code idToken} is
 * the token returned by the provider SDK on the client (Google ID token,
 * Facebook access token, Apple identity token).
 */
public class SocialLoginRequest {

	private String idToken;

	/** Optional access token (Facebook returns this; Google ID-token alone is enough). */
	private String accessToken;

	public String getIdToken() { return idToken; }
	public void setIdToken(String idToken) { this.idToken = idToken; }

	public String getAccessToken() { return accessToken; }
	public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
}
