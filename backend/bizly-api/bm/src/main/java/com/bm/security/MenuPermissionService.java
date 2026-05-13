package com.bm.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.bm.common.ApiMessages;
import com.bm.exception.ServiceException;
import com.bm.umbridge.UmMenuEntity;
import com.bm.umbridge.UmMenuRepository;
import com.bm.umbridge.UmRoleEntity;
import com.bm.umbridge.UmRoleMenuPermissionEntity;
import com.bm.umbridge.UmRoleMenuPermissionId;
import com.bm.umbridge.UmRoleMenuPermissionRepository;
import com.bm.umbridge.UmRoleRepository;

@Service
public class MenuPermissionService {

	@Autowired
	private UmRoleRepository roleRepository;

	@Autowired
	private UmRoleMenuPermissionRepository permissionRepository;

	@Autowired
	private UmMenuRepository umMenuRepository;

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
			roleRepository.findByNameIgnoreCase(name).map(UmRoleEntity::getId).ifPresent(ids::add);
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
		Optional<UmMenuEntity> m = umMenuRepository.findFirstByRouteIgnoreCaseOrderByIdAsc(r1);
		if (m.isEmpty()) {
			m = umMenuRepository.findFirstByRouteIgnoreCaseOrderByIdAsc(r2);
		}
		return m.map(UmMenuEntity::getId);
	}

	public void assertAllowed(Optional<Long> menuId, Optional<String> menuRoute, MenuPermissionAction action) {
		List<Long> assigned = resolveAllAssignedRoleIds();
		if (assigned.isEmpty()) {
			throw new ServiceException(ApiMessages.MENU_PERMISSION_DENIED, HttpStatus.FORBIDDEN);
		}
		List<Long> matrixRoles = assigned.stream().filter(this::isMatrixEnforcedForRole).collect(Collectors.toList());
		if (matrixRoles.isEmpty()) {
			return;
		}
		Long mid = menuId.orElse(null);
		if (mid == null && menuRoute.isPresent()) {
			mid = resolveMenuIdByRoute(menuRoute.get()).orElse(null);
		}
		if (mid == null) {
			throw new ServiceException(ApiMessages.MENU_PERMISSION_DENIED, HttpStatus.FORBIDDEN);
		}
		for (Long rid : matrixRoles) {
			UmRoleMenuPermissionId compositeId = new UmRoleMenuPermissionId();
			compositeId.setRoleId(rid);
			compositeId.setMenuId(mid);
			Optional<UmRoleMenuPermissionEntity> opt = permissionRepository.findById(compositeId);
			if (opt.isPresent() && allows(opt.get(), action)) {
				return;
			}
		}
		throw new ServiceException(ApiMessages.MENU_PERMISSION_DENIED, HttpStatus.FORBIDDEN);
	}

	private static boolean allows(UmRoleMenuPermissionEntity p, MenuPermissionAction action) {
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
}
