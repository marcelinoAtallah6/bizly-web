package com.um.api.service.menu;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.um.api.dto.menu.MenuPermissionRouteRow;
import com.um.api.dto.menu.PermissionMetadataResponse;
import com.um.api.model.menu.UmApplication;
import com.um.api.model.menu.UmMenu;
import com.um.api.repository.menu.UmApplicationRepository;
import com.um.api.service.security.MenuPermissionAction;

@Service
public class MenuPermissionMetadataService {

	@Autowired
	private UmApplicationRepository applicationRepository;

	@Transactional(readOnly = true)
	public PermissionMetadataResponse buildMetadata() {
		PermissionMetadataResponse out = new PermissionMetadataResponse();
		List<String> actions = new ArrayList<>();
		for (MenuPermissionAction a : MenuPermissionAction.values()) {
			actions.add(a.name());
		}
		out.setActions(actions);

		List<MenuPermissionRouteRow> rows = new ArrayList<>();
		List<UmApplication> applications = applicationRepository.findByIsActiveOrderByNameAsc(true);
		for (UmApplication app : applications) {
			List<UmMenu> roots = app.getMenus();
			if (roots == null) {
				continue;
			}
			for (UmMenu root : roots) {
				if (!Boolean.TRUE.equals(root.getIsActive())) {
					continue;
				}
				walkMenus(root, app, "", rows);
			}
		}
		out.setMenus(rows);
		return out;
	}

	private void walkMenus(UmMenu menu, UmApplication app, String pathPrefix, List<MenuPermissionRouteRow> rows) {
		String path = pathPrefix.isEmpty() ? menu.getName() : pathPrefix + " / " + menu.getName();
		String route = menu.getRoute();
		if (route != null && !route.isBlank()) {
			MenuPermissionRouteRow row = new MenuPermissionRouteRow();
			row.setMenuId(menu.getId());
			row.setRoute(route.trim());
			row.setMenuPath(path);
			row.setApplicationId(app.getId());
			row.setApplicationName(app.getName());
			rows.add(row);
		}
		if (menu.getMenus() != null) {
			for (UmMenu child : menu.getMenus()) {
				if (!Boolean.TRUE.equals(child.getIsActive())) {
					continue;
				}
				walkMenus(child, app, path, rows);
			}
		}
	}
}
