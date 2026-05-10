package com.um.api.service.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.um.api.dto.menu.NavGroupItemResponse;
import com.um.api.dto.menu.NavMenuItemResponse;
import com.um.api.model.menu.UmApplication;
import com.um.api.model.menu.UmMenu;
import com.um.api.model.role.RoleMenuPermission;
import com.um.api.repository.menu.UmApplicationRepository;
import com.um.api.repository.role.RoleMenuPermissionRepository;
import com.um.api.service.security.MenuPermissionService;

@Service
public class MenuServiceImpl implements IMenuService {

	@Autowired
	private UmApplicationRepository applicationRepository;

	@Autowired
	private RoleMenuPermissionRepository permissionRepository;

	@Autowired
	private MenuPermissionService menuPermissionService;

	@Override
	public List<NavGroupItemResponse> getMenus() {

		Set<String> grantedRoles = menuPermissionService.currentNormalizedRoles();
		List<Long> assignedRoleIds = menuPermissionService.resolveAllAssignedRoleIds();
		boolean strict = assignedRoleIds.stream().anyMatch(id -> permissionRepository.countByIdRoleId(id) > 0);

		Map<Long, MenuPermBits> mergedByMenu = new HashMap<>();
		Set<Long> viewGrantedIds = new HashSet<>();
		if (strict) {
			for (Long rid : assignedRoleIds) {
				if (permissionRepository.countByIdRoleId(rid) == 0) {
					continue;
				}
				for (RoleMenuPermission p : permissionRepository.findByIdRoleId(rid)) {
					long menuId = p.getId().getMenuId();
					mergedByMenu.merge(menuId, MenuPermBits.from(p), MenuPermBits::union);
				}
			}
			for (Map.Entry<Long, MenuPermBits> e : mergedByMenu.entrySet()) {
				if (e.getValue().allowView) {
					viewGrantedIds.add(e.getKey());
				}
			}
		}

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

			group.setMenus(buildMenuTree(app.getMenus(), grantedRoles, strict, viewGrantedIds, mergedByMenu));

			if (group.getMenus().isEmpty() && app.getRoute() == null) {
				continue;
			}

			groups.add(group);
		}

		return groups;
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

	private boolean menuPasses(UmMenu menu, Set<String> grantedRoles, boolean strict, Set<Long> viewGrantedIds) {
		if (!visibleForRoles(menu.getAllowedRoles(), grantedRoles)) {
			return false;
		}
		if (!strict) {
			return true;
		}
		return subtreeContainsAllowedView(menu, viewGrantedIds);
	}

	private boolean subtreeContainsAllowedView(UmMenu menu, Set<Long> viewGrantedIds) {
		if (viewGrantedIds.contains(menu.getId())) {
			return true;
		}
		if (menu.getMenus() == null) {
			return false;
		}
		for (UmMenu child : menu.getMenus()) {
			if (!Boolean.TRUE.equals(child.getIsActive())) {
				continue;
			}
			if (subtreeContainsAllowedView(child, viewGrantedIds)) {
				return true;
			}
		}
		return false;
	}

	private List<NavMenuItemResponse> buildMenuTree(List<UmMenu> menus, Set<String> grantedRoles, boolean strict,
			Set<Long> viewGrantedIds, Map<Long, MenuPermBits> mergedByMenu) {

		if (menus == null || menus.isEmpty()) {
			return new ArrayList<>();
		}

		List<NavMenuItemResponse> result = new ArrayList<>();

		for (UmMenu menu : menus) {
			if (!Boolean.TRUE.equals(menu.getIsActive())) {
				continue;
			}
			if (!menuPasses(menu, grantedRoles, strict, viewGrantedIds)) {
				continue;
			}
			result.add(toMenuItem(menu, grantedRoles, strict, viewGrantedIds, mergedByMenu));
		}

		return result;
	}

	private NavMenuItemResponse toMenuItem(UmMenu entity, Set<String> grantedRoles, boolean strict,
			Set<Long> viewGrantedIds, Map<Long, MenuPermBits> mergedByMenu) {

		NavMenuItemResponse dto = new NavMenuItemResponse();

		dto.setId(entity.getId());
		dto.setName(entity.getName());
		dto.setRoute(entity.getRoute());
		dto.setIcon(entity.getIcon());
		dto.setDescription(entity.getApplication().getDescription());
		dto.setIsActive(Boolean.TRUE.equals(entity.getIsActive()));

		if (strict) {
			MenuPermBits bits = mergedByMenu.get(entity.getId());
			if (bits != null) {
				dto.setAllowView(bits.allowView);
				dto.setAllowAdd(bits.allowAdd);
				dto.setAllowEdit(bits.allowEdit);
				dto.setAllowDelete(bits.allowDelete);
			} else {
				dto.setAllowView(false);
				dto.setAllowAdd(false);
				dto.setAllowEdit(false);
				dto.setAllowDelete(false);
			}
		}

		if (entity.getMenus() != null && !entity.getMenus().isEmpty()) {

			List<NavMenuItemResponse> childDtos = new ArrayList<>();

			for (UmMenu child : entity.getMenus()) {
				if (!Boolean.TRUE.equals(child.getIsActive())) {
					continue;
				}
				if (!menuPasses(child, grantedRoles, strict, viewGrantedIds)) {
					continue;
				}
				childDtos.add(toMenuItem(child, grantedRoles, strict, viewGrantedIds, mergedByMenu));
			}

			dto.setMenus(childDtos);
		}

		return dto;
	}

	/** Union of flags across all assigned roles that use the permission matrix. */
	private static final class MenuPermBits {
		private boolean allowView;
		private boolean allowAdd;
		private boolean allowEdit;
		private boolean allowDelete;

		static MenuPermBits from(RoleMenuPermission p) {
			MenuPermBits b = new MenuPermBits();
			b.allowView = p.isAllowView();
			b.allowAdd = p.isAllowAdd();
			b.allowEdit = p.isAllowEdit();
			b.allowDelete = p.isAllowDelete();
			return b;
		}

		static MenuPermBits union(MenuPermBits a, MenuPermBits b) {
			MenuPermBits o = new MenuPermBits();
			o.allowView = a.allowView || b.allowView;
			o.allowAdd = a.allowAdd || b.allowAdd;
			o.allowEdit = a.allowEdit || b.allowEdit;
			o.allowDelete = a.allowDelete || b.allowDelete;
			return o;
		}
	}
}
