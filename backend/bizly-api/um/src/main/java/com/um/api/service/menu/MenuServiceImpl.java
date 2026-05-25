package com.um.api.service.menu;

import java.util.ArrayList;
import java.util.Comparator;
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
import com.um.api.repository.menu.UmMenuRepository;
import com.um.api.repository.role.RoleMenuPermissionRepository;
import com.um.api.service.security.MenuPermissionService;
import com.um.security.BusinessContextHolder;

@Service
public class MenuServiceImpl implements IMenuService {

	@Autowired
	private UmApplicationRepository applicationRepository;

	@Autowired
	private UmMenuRepository menuRepository;

	@Autowired
	private RoleMenuPermissionRepository permissionRepository;

	@Autowired
	private MenuPermissionService menuPermissionService;

	@Override
	public List<NavGroupItemResponse> getMenus() {

		Set<String> grantedRoles = menuPermissionService.currentNormalizedRoles();
		List<Long> assignedRoleIds = menuPermissionService.resolveAllAssignedRoleIds();
		/* Strict-mode is decided by role LEVEL, not by whether the active role happens to have rows.
		 * Without this rule a BUSINESS role with zero entries in {@code UM_ROLE_MENU_PERM} would fall
		 * back to "show every menu", which is exactly the bug where switching to an empty role still
		 * surfaces the full sidebar. ADMIN-level roles (e.g. SUPER_ADMIN) are trusted across the
		 * board and continue to receive the unfiltered tree — the server still enforces every API. */
		boolean adminBypass = BusinessContextHolder.canBypassTenant();
		boolean strict = !adminBypass;

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

		List<UmApplication> applications = applicationRepository.findByIsActiveOrderBySortOrderAscNameAsc(true);

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

			List<UmMenu> flatMenus = menuRepository.findByApplicationIdOrderBySortOrderAscNameAsc(app.getId());
			group.setMenus(buildNavMenuTree(flatMenus, grantedRoles, strict, viewGrantedIds, mergedByMenu, flatMenus));

			/* Drop empty groups. In strict (BUSINESS) mode we ALWAYS drop them, even when the
			 * application itself has a {@code route} like {@code /dashboard} — application rows
			 * have no entry in {@code UM_ROLE_MENU_PERM}, so a standalone app-route can never be
			 * permission-checked and would otherwise leak through as a "free" sidebar entry. In
			 * admin-bypass mode we keep the legacy fallback so standalone apps still render. */
			if (group.getMenus().isEmpty()) {
				if (strict || app.getRoute() == null) {
					continue;
				}
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

	private boolean menuPasses(UmMenu menu, Set<String> grantedRoles, boolean strict, Set<Long> viewGrantedIds,
			List<UmMenu> flatMenus) {
		if (!visibleForRoles(menu.getAllowedRoles(), grantedRoles)) {
			return false;
		}
		if (!strict) {
			return true;
		}
		return subtreeContainsAllowedView(menu, viewGrantedIds, flatMenus);
	}

	private boolean subtreeContainsAllowedView(UmMenu menu, Set<Long> viewGrantedIds, List<UmMenu> flatMenus) {
		if (viewGrantedIds.contains(menu.getId())) {
			return true;
		}
		Long id = menu.getId();
		for (UmMenu candidate : flatMenus) {
			if (!Boolean.TRUE.equals(candidate.getIsActive())) {
				continue;
			}
			Long parentId = MenuTreeSupport.parentIdOf(candidate);
			if (id.equals(parentId) && subtreeContainsAllowedView(candidate, viewGrantedIds, flatMenus)) {
				return true;
			}
		}
		return false;
	}

	private List<NavMenuItemResponse> buildNavMenuTree(List<UmMenu> flatMenus, Set<String> grantedRoles, boolean strict,
			Set<Long> viewGrantedIds, Map<Long, MenuPermBits> mergedByMenu, List<UmMenu> flatForPermCheck) {
		if (flatMenus == null || flatMenus.isEmpty()) {
			return new ArrayList<>();
		}
		List<NavMenuItemResponse> roots = MenuTreeSupport.buildTree(flatMenus,
				m -> Boolean.TRUE.equals(m.getIsActive())
						&& menuPasses(m, grantedRoles, strict, viewGrantedIds, flatForPermCheck),
				m -> toMenuItem(m, grantedRoles, strict, viewGrantedIds, mergedByMenu),
				NavMenuItemResponse::getMenus);
		MenuTreeSupport.sortTree(roots,
				Comparator.comparing(NavMenuItemResponse::getSortOrder, Comparator.nullsLast(Integer::compareTo))
						.thenComparing(NavMenuItemResponse::getName, Comparator.nullsLast(String::compareToIgnoreCase)),
				NavMenuItemResponse::getMenus);
		return roots;
	}

	private NavMenuItemResponse toMenuItem(UmMenu entity, Set<String> grantedRoles, boolean strict,
			Set<Long> viewGrantedIds, Map<Long, MenuPermBits> mergedByMenu) {

		NavMenuItemResponse dto = new NavMenuItemResponse();

		dto.setId(entity.getId());
		dto.setName(entity.getName());
		dto.setRoute(entity.getRoute());
		dto.setSortOrder(entity.getSortOrder());
		dto.setIcon(entity.getIcon());
		dto.setDescription(entity.getApplication().getDescription());
		dto.setIsActive(Boolean.TRUE.equals(entity.getIsActive()));
		dto.setMenus(new ArrayList<>());

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
