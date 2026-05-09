package com.um.api.service.menu;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.um.api.dto.menu.NavGroupItemResponse;
import com.um.api.dto.menu.NavMenuItemResponse;
import com.um.api.model.menu.UmApplication;
import com.um.api.model.menu.UmMenu;
import com.um.api.repository.menu.UmApplicationRepository;

@Service
public class MenuServiceImpl implements IMenuService {

	@Autowired
	private UmApplicationRepository applicationRepository;

	@Override
	public List<NavGroupItemResponse> getMenus() {

		Set<String> grantedRoles = currentNormalizedRoles();

		List<UmApplication> applications = applicationRepository.findByIsActiveOrderByNameAsc(true);

		List<NavGroupItemResponse> groups = new ArrayList<>();

		for (UmApplication app : applications) {

			if (!visibleForRoles(app.getAllowedRoles(), grantedRoles)) {
				continue;
			}

			NavGroupItemResponse group = new NavGroupItemResponse();
			group.setId(app.getId());
			group.setName(app.getName());
			group.setIcon(app.getIcon());
			group.setRoute(app.getRoute());
			group.setDescription(app.getDescription());

			group.setMenus(buildMenuTree(app.getMenus(), grantedRoles));

			if (group.getMenus().isEmpty() && app.getRoute() == null) {
				continue;
			}

			groups.add(group);
		}

		return groups;
	}

	private static Set<String> currentNormalizedRoles() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		Set<String> granted = new HashSet<>();
		if (auth != null) {
			for (GrantedAuthority ga : auth.getAuthorities()) {
				granted.add(normalizeRoleName(ga.getAuthority()));
			}
		}
		return granted;
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

	/**
	 * {@code allowedCsv} null/blank → visible. Otherwise user must have at least one matching role.
	 */
	private static boolean visibleForRoles(String allowedCsv, Set<String> grantedNormalized) {
		if (allowedCsv == null || allowedCsv.isBlank()) {
			return true;
		}
		for (String token : allowedCsv.split(",")) {
			String n = normalizeRoleName(token);
			if (!n.isEmpty() && grantedNormalized.contains(n)) {
				return true;
			}
		}
		return false;
	}

	private List<NavMenuItemResponse> buildMenuTree(List<UmMenu> menus, Set<String> grantedRoles) {

		if (menus == null || menus.isEmpty()) {
			return new ArrayList<>();
		}

		List<NavMenuItemResponse> result = new ArrayList<>();

		for (UmMenu menu : menus) {
			if (!Boolean.TRUE.equals(menu.getIsActive())) {
				continue;
			}
			if (!visibleForRoles(menu.getAllowedRoles(), grantedRoles)) {
				continue;
			}
			NavMenuItemResponse item = toMenuItem(menu, grantedRoles);
			if (item != null) {
				result.add(item);
			}
		}

		return result;
	}

	private NavMenuItemResponse toMenuItem(UmMenu entity, Set<String> grantedRoles) {

		NavMenuItemResponse dto = new NavMenuItemResponse();

		dto.setId(entity.getId());
		dto.setName(entity.getName());
		dto.setRoute(entity.getRoute());
		dto.setIcon(entity.getIcon());
		dto.setDescription(entity.getApplication().getDescription());
		dto.setIsActive(Boolean.TRUE.equals(entity.getIsActive()));

		if (entity.getMenus() != null && !entity.getMenus().isEmpty()) {

			List<NavMenuItemResponse> childDtos = new ArrayList<>();

			for (UmMenu child : entity.getMenus()) {
				if (!Boolean.TRUE.equals(child.getIsActive())) {
					continue;
				}
				if (!visibleForRoles(child.getAllowedRoles(), grantedRoles)) {
					continue;
				}
				NavMenuItemResponse childDto = toMenuItem(child, grantedRoles);
				if (childDto != null) {
					childDtos.add(childDto);
				}
			}

			dto.setMenus(childDtos);
		}

		return dto;
	}
}
