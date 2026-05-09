package com.auth.api.controllers.dto.forgot;

public class VerifyResetTokenRequest {

	private String token;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}
