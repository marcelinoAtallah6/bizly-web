package com.um.api.service.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.um.api.model.menu.UmMenu;
import com.um.api.model.role.Role;
import com.um.api.model.role.RoleMenuPermission;
import com.um.api.model.role.RoleMenuPermissionId;
import com.um.api.repository.menu.UmMenuRepository;
import com.um.api.repository.role.RoleMenuPermissionRepository;
import com.um.api.repository.role.RoleRepository;
import com.um.common.ApiMessages;
import com.um.exception.ServiceException;
import com.um.security.BusinessContextHolder;

@Service
public class MenuPermissionService {

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private RoleMenuPermissionRepository permissionRepository;

	@Autowired
	private UmMenuRepository umMenuRepository;

	public Set<String> currentNormalizedRoles() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		Set<String> granted = new HashSet<>();
		if (auth != null) {
			for (GrantedAuthority ga : auth.getAuthorities()) {
				granted.add(normalizeRoleName(ga.getAuthority()));
			}
		}
		return granted;
	}

	/**
	 * Union of menu ids the current principal may delegate (any assigned role grants view/add/edit/delete).
	 */
	public Set<Long> menuIdsGrantedToCurrentPrincipal() {
		Set<Long> menuIds = new LinkedHashSet<>();
		for (Long roleId : resolveAllAssignedRoleIds()) {
			for (RoleMenuPermission p : permissionRepository.findByIdRoleId(roleId)) {
				if (p.isAllowView() || p.isAllowAdd() || p.isAllowEdit() || p.isAllowDelete()) {
					menuIds.add(p.getId().getMenuId());
				}
			}
		}
		return menuIds;
	}

	public List<Long> resolveAllAssignedRoleIds() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null) {
			return Collections.emptyList();
		}
		LinkedHashSet<Long> ids = new LinkedHashSet<>();
		for (GrantedAuthority ga : auth.getAuthorities()) {
			String name = stripRolePrefix(ga.getAuthority());
			if (name.isEmpty()) {
				continue;
			}
			roleRepository.findFirstByNameIgnoreCaseOrderByIdAsc(name).map(Role::getId).ifPresent(ids::add);
		}
		return new ArrayList<>(ids);
	}

	public boolean isMatrixEnforcedForRole(long roleId) {
		return permissionRepository.countByIdRoleId(roleId) > 0;
	}

	public Optional<Long> resolveMenuIdByRoute(String route) {
		if (route == null || route.isBlank()) {
			return Optional.empty();
		}
		String r1 = route.trim();
		String r2 = r1.startsWith("/") ? r1.substring(1) : "/" + r1;
		Optional<UmMenu> m = umMenuRepository.findFirstByRouteIgnoreCaseOrderByIdAsc(r1);
		if (m.isEmpty()) {
			m = umMenuRepository.findFirstByRouteIgnoreCaseOrderByIdAsc(r2);
		}
		return m.map(UmMenu::getId);
	}

	/**
	 * Enforces DB-backed menu permissions. ADMIN-level roles (e.g. SUPER_ADMIN) bypass the check —
	 * they are trusted across the board. Every other role MUST have an explicit row in
	 * {@code UM_ROLE_MENU_PERM} granting the requested action; an empty matrix means deny, so
	 * stripping a permission from UM also blocks the API immediately.
	 */
	public void assertAllowed(Optional<Long> menuId, Optional<String> menuRoute, MenuPermissionAction action) {
		if (BusinessContextHolder.canBypassTenant()) {
			return;
		}
		List<Long> assigned = resolveAllAssignedRoleIds();
		if (assigned.isEmpty()) {
			throw new ServiceException(ApiMessages.MENU_PERMISSION_DENIED, HttpStatus.FORBIDDEN);
		}
		Long mid = menuId.orElse(null);
		if (mid == null && menuRoute.isPresent()) {
			mid = resolveMenuIdByRoute(menuRoute.get()).orElse(null);
		}
		if (mid == null) {
			throw new ServiceException(ApiMessages.MENU_PERMISSION_DENIED, HttpStatus.FORBIDDEN);
		}
		for (Long rid : assigned) {
			RoleMenuPermissionId compositeId = new RoleMenuPermissionId();
			compositeId.setRoleId(rid);
			compositeId.setMenuId(mid);
			RoleMenuPermission p = permissionRepository.findById(compositeId).orElse(null);
			if (p != null && allows(p, action)) {
				return;
			}
		}
		throw new ServiceException(ApiMessages.MENU_PERMISSION_DENIED, HttpStatus.FORBIDDEN);
	}

	private static boolean allows(RoleMenuPermission p, MenuPermissionAction action) {
		switch (action) {
		case VIEW:
			return p.isAllowView();
		case ADD:
			return p.isAllowAdd();
		case EDIT:
			return p.isAllowEdit();
		case DELETE:
			return p.isAllowDelete();
		default:
			return false;
		}
	}

	private static String stripRolePrefix(String authority) {
		if (authority == null) {
			return "";
		}
		String t = authority.trim();
		if (t.length() > 5 && t.regionMatches(true, 0, "ROLE_", 0, 5)) {
			return t.substring(5);
		}
		return t;
	}

	private static String normalizeRoleName(String raw) {
		String trimmed = raw == null ? "" : raw.trim();
		if (trimmed.isEmpty()) {
			return "";
		}
		String upper = trimmed.toUpperCase(Locale.ROOT);
		if (upper.startsWith("ROLE_")) {
			return upper;
		}
		return "ROLE_" + upper;
	}
}
