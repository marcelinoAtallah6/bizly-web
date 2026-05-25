package com.um.api.dto.menu;

import java.util.List;
import java.util.Map;

/**
 * All screens that participate in the matrix plus action codes aggregated from {@code UM_ROLE_MENU_PERM}
 * (only verbs that are granted on at least one role–menu row).
 */
public class PermissionMetadataResponse {

	private List<String> actions;
	private Map<Long, List<String>> actionsByMenuId;
	private List<MenuPermissionRouteRow> menus;

	public List<String> getActions() {
		return actions;
	}

	public void setActions(List<String> actions) {
		this.actions = actions;
	}

	/**
	 * For each {@code UM_MENUS} id, the permission verbs granted on at least one {@code UM_ROLE_MENU_PERM} row
	 * for that menu (union across roles).
	 */
	public Map<Long, List<String>> getActionsByMenuId() {
		return actionsByMenuId;
	}

	public void setActionsByMenuId(Map<Long, List<String>> actionsByMenuId) {
		this.actionsByMenuId = actionsByMenuId;
	}

	public List<MenuPermissionRouteRow> getMenus() {
		return menus;
	}

	public void setMenus(List<MenuPermissionRouteRow> menus) {
		this.menus = menus;
	}
}
