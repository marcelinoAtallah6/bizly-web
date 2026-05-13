package com.auth.api.service.admin;

import java.util.List;

import com.auth.api.controllers.dto.admin.AdminBusinessDto;
import com.auth.api.controllers.dto.admin.AdminUserDto;

/**
 * Endpoints powering the SUPER_ADMIN "global context switcher" in the top bar.
 *
 * <p>System admins (roleLevel = ADMIN) can search every business and user across
 * the tenant and pick one as the "act as" target. The chosen id is sent on every
 * downstream API call as the {@code X-Business-Override} header, which the
 * {@code InternalAuthFilter} in each microservice honours only when the caller's
 * roleLevel is ADMIN. The override never leaves the admin's session.</p>
 *
 * <p>Authorisation is enforced at the controller layer by checking the
 * {@code X-Role-Level} header forwarded from the gateway.</p>
 */
public interface IAdminContextService {

	List<AdminBusinessDto> searchBusinesses(String q, Integer limit);

	List<AdminUserDto> searchUsers(String q, Integer limit);
}
