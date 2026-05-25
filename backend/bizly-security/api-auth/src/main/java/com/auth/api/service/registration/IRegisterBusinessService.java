package com.auth.api.service.registration;

import java.util.List;

import com.auth.api.controllers.dto.registration.AssignableRoleDto;
import com.auth.api.controllers.dto.registration.BusinessTypeOption;
import com.auth.api.controllers.dto.registration.MeAvatarResponse;
import com.auth.api.controllers.dto.registration.MeResponse;
import com.auth.api.controllers.dto.registration.RegisterBusinessRequest;
import com.auth.api.controllers.dto.registration.RegisterBusinessResponse;
import com.auth.api.controllers.dto.registration.RegisterRequest;
import com.auth.api.controllers.dto.registration.RegisterResponse;

public interface IRegisterBusinessService {

	/**
	 * Public self-service sign-up. Creates the {@code um_user} row, the
	 * {@code um_business} row, the {@code um_user_role} mapping and auto-issues
	 * a JWT session in one atomic transaction. This is the ONLY supported entry
	 * point for a brand-new account on the front-end "Create Account" wizard;
	 * the legacy {@link #registerBusiness} method exists only for upgrading
	 * pre-existing accounts that have no business yet (e.g. created by an
	 * admin or by social login before the business step finished).
	 */
	RegisterResponse register(RegisterRequest req, String deviceId, String ip);


	/**
	 * Server-decided default role applied to a brand new business owner. The
	 * UI is <em>not</em> trusted to choose this; the controller never reads a
	 * roleId from the request. The role row must be {@code BUSINESS}-level
	 * <em>and</em> have {@code is_default_for_registration = 1}. If multiple
	 * candidates exist, the one with the lowest id wins.
	 */
	RegisterBusinessResponse registerBusiness(String username, String deviceId, String ip, RegisterBusinessRequest req);

	/** Returns ONLY roles whose level allows assignment on registration and which are not system-restricted. */
	List<AssignableRoleDto> listAssignableRoles();

	/**
	 * Returns the catalog of business-type roles offered to the public sign-up
	 * and post-login business-creation flows. Filters at the SQL boundary so
	 * SUPER_ADMIN / system-restricted / non-BUSINESS-level rows can never leak
	 * to the client — adding a row to {@code um_role} is the only way to
	 * expose a new business type.
	 */
	List<BusinessTypeOption> listBusinessTypes();

	/** Marks the welcome wizard as finished and reissues the JWT so the {@code firstLogin} claim updates. */
	RegisterBusinessResponse completeWelcome(String username, String deviceId, String ip);

	/** Snapshot of the authenticated user's state, used by the SPA to drive the post-login redirect. */
	MeResponse me(String username);

	/**
	 * Returns the navbar avatar (mime + base64) for the authenticated user. The JWT only carries
	 * tiny images to keep the token light; anything larger lives in the DB and is fetched through
	 * this endpoint on demand.
	 */
	MeAvatarResponse meAvatar(String username);
}
