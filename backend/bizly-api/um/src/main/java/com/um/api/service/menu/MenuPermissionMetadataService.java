package com.um.api.service.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.um.api.dto.menu.MenuPermissionRouteRow;
import com.um.api.dto.menu.PermissionMetadataResponse;
import com.um.api.model.menu.UmApplication;
import com.um.api.model.menu.UmMenu;
import com.um.api.model.role.RoleMenuPermission;
import com.um.api.repository.menu.UmApplicationRepository;
import com.um.api.repository.menu.UmMenuRepository;
import com.um.api.repository.role.RoleMenuPermissionRepository;

@Service
public class MenuPermissionMetadataService {

	@Autowired
	private UmApplicationRepository applicationRepository;
	@Autowired
	private UmMenuRepository menuRepository;
	@Autowired
	private RoleMenuPermissionRepository roleMenuPermissionRepository;

	@Transactional(readOnly = true)
	public PermissionMetadataResponse buildMetadata() {
		PermissionMetadataResponse out = new PermissionMetadataResponse();
		fillPermissionActions(out);
		return attachMenuRoutes(out);
	}

	/**
	 * Permission union for the given role ids only (current principal's assigned roles).
	 */
	@Transactional(readOnly = true)
	public PermissionMetadataResponse buildMetadataForRoleIds(List<Long> roleIds) {
		PermissionMetadataResponse out = new PermissionMetadataResponse();
		if (roleIds == null || roleIds.isEmpty()) {
			out.setActions(List.of());
			out.setActionsByMenuId(Map.of());
			return attachMenuRoutes(out);
		}
		fillPermissionActionsForRoles(out, new LinkedHashSet<>(roleIds));
		return attachMenuRoutes(out);
	}

	private PermissionMetadataResponse attachMenuRoutes(PermissionMetadataResponse out) {
		List<MenuPermissionRouteRow> rows = new ArrayList<>();
		List<UmApplication> applications = applicationRepository.findByIsActiveOrderBySortOrderAscNameAsc(true);
		for (UmApplication app : applications) {
			List<UmMenu> flat = menuRepository.findByApplicationIdOrderBySortOrderAscNameAsc(app.getId());
			if (flat.isEmpty()) {
				continue;
			}
			Map<Long, UmMenu> byId = new HashMap<>();
			for (UmMenu m : flat) {
				byId.put(m.getId(), m);
			}
			for (UmMenu menu : flat) {
				if (!Boolean.TRUE.equals(menu.getIsActive())) {
					continue;
				}
				String route = menu.getRoute();
				if (route == null || route.isBlank()) {
					continue;
				}
				MenuPermissionRouteRow row = new MenuPermissionRouteRow();
				row.setMenuId(menu.getId());
				row.setRoute(route.trim());
				row.setMenuPath(buildMenuPath(menu, byId));
				row.setApplicationId(app.getId());
				row.setApplicationName(app.getName());
				rows.add(row);
			}
		}
		out.setMenus(rows);
		return out;
	}

	private static String buildMenuPath(UmMenu menu, Map<Long, UmMenu> byId) {
		List<String> parts = new ArrayList<>();
		UmMenu cur = menu;
		while (cur != null) {
			parts.add(0, cur.getName());
			Long parentId = MenuTreeSupport.parentIdOf(cur);
			cur = parentId != null ? byId.get(parentId) : null;
		}
		return String.join(" / ", parts);
	}

	/**
	 * One read of {@code UM_ROLE_MENU_PERM}: union of all enabled verbs plus per-menu unions.
	 */
	private void fillPermissionActions(PermissionMetadataResponse out) {
		fillPermissionActionsForRoles(out, null);
	}

	private void fillPermissionActionsForRoles(PermissionMetadataResponse out, Set<Long> roleIdFilter) {
		List<RoleMenuPermission> rows = roleMenuPermissionRepository.findAll();
		Set<String> unionSet = new LinkedHashSet<>();
		Map<Long, LinkedHashSet<String>> byMenuAcc = new HashMap<>();
		for (RoleMenuPermission p : rows) {
			if (roleIdFilter != null) {
				Long rid = p.getId() == null ? null : p.getId().getRoleId();
				if (rid == null || !roleIdFilter.contains(rid)) {
					continue;
				}
			}
			Long mid = p.getId() == null ? null : p.getId().getMenuId();
			LinkedHashSet<String> menuSet = mid == null ? null : byMenuAcc.computeIfAbsent(mid, k -> new LinkedHashSet<>());
			addVerbFlags(p, unionSet, menuSet);
		}
		out.setActions(sortCopy(new ArrayList<>(unionSet)));
		Map<Long, List<String>> byMenu = new HashMap<>();
		for (Map.Entry<Long, LinkedHashSet<String>> e : byMenuAcc.entrySet()) {
			byMenu.put(e.getKey(), sortCopy(new ArrayList<>(e.getValue())));
		}
		out.setActionsByMenuId(byMenu);
	}

	private static void addVerbFlags(RoleMenuPermission p, Set<String> union, Set<String> menuSet) {
		if (p.isAllowView()) {
			union.add("VIEW");
			if (menuSet != null) {
				menuSet.add("VIEW");
			}
		}
		if (p.isAllowAdd()) {
			union.add("ADD");
			if (menuSet != null) {
				menuSet.add("ADD");
			}
		}
		if (p.isAllowEdit()) {
			union.add("EDIT");
			if (menuSet != null) {
				menuSet.add("EDIT");
			}
		}
		if (p.isAllowDelete()) {
			union.add("DELETE");
			if (menuSet != null) {
				menuSet.add("DELETE");
			}
		}
	}

	private static List<String> sortCopy(List<String> list) {
		list.sort(String.CASE_INSENSITIVE_ORDER);
		return list;
	}

}
