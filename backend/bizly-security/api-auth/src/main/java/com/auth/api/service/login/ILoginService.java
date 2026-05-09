package com.auth.api.service.login;

import org.springframework.stereotype.Service;

import com.auth.api.controllers.dto.refresh.RefreshRequest;
import com.auth.api.model.login.LoginResponse;

@Service
public interface ILoginService {

	public LoginResponse login(String username, String password);

	public LoginResponse refreshToken(RefreshRequest req, String deviceId, String ip);

	public void logout(String sessionId, String deviceId);

}
