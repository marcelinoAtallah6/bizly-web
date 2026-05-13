package com.auth.api.service.login;

import org.springframework.stereotype.Service;

import com.auth.api.controllers.dto.forgot.ForgotPasswordEmailRequest;
import com.auth.api.controllers.dto.forgot.ForgotPasswordRequest;
import com.auth.api.controllers.dto.forgot.ResetPasswordRequest;
import com.auth.api.controllers.dto.forgot.VerifyResetTokenRequest;
import com.auth.api.controllers.dto.refresh.RefreshRequest;
import com.auth.api.controllers.dto.session.ActiveRoleRequest;
import com.auth.api.model.login.LoginResponse;

@Service
public interface ILoginService {

	public LoginResponse login(String username, String password);

	public LoginResponse refreshToken(RefreshRequest req, String deviceId, String ip);

	public LoginResponse setActiveRole(ActiveRoleRequest req, String deviceId, String ip);

	public void logout(String sessionId, String deviceId);

	public void forgotPassword(ForgotPasswordRequest request);

	public void verifyResetToken(VerifyResetTokenRequest request);

	public void resetPassword(ResetPasswordRequest request);

	/**
	 * Issues a fresh access + refresh token pair for the user's current session on the given device.
	 * Used by the business-registration and welcome-complete flows so the new claims
	 * ({@code businessId}, {@code firstLogin}, {@code roleLevel}) take effect immediately without
	 * forcing the user to log out and back in.
	 */
	public LoginResponse reissueAccessTokenForUser(Long userId, String deviceId, String ip);

}
