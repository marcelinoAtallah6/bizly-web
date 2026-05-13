package com.auth.api.service.social;

import com.auth.api.controllers.dto.social.SocialLoginRequest;
import com.auth.api.model.login.LoginResponse;

public interface ISocialLoginService {

	/**
	 * Verifies the provider-issued token, resolves (or creates) the corresponding
	 * {@code um_user} row, then issues a Bizly JWT for that user via the existing
	 * login pipeline so all downstream session / JWT logic is unchanged.
	 *
	 * @param provider one of {@code google}, {@code facebook}, {@code apple}
	 */
	LoginResponse login(String provider, SocialLoginRequest req, String deviceId, String ip);
}
