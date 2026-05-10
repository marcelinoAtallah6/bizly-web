package com.um.api.service.role;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.um.api.dto.role.permission.GetRoleMenuPermissionsRequest;
import com.um.api.dto.role.permission.RoleMenuPermissionEntryDto;
import com.um.api.dto.role.permission.RoleMenuPermissionRowResponse;
import com.um.api.dto.role.permission.SaveRoleMenuPermissionsRequest;
import com.um.api.model.menu.UmApplication;
import com.um.api.model.menu.UmMenu;
import com.um.api.model.role.Role;
import com.um.api.model.role.RoleMenuPermission;
import com.um.api.model.role.RoleMenuPermissionId;
import com.um.api.repository.menu.UmApplicationRepository;
import com.um.api.repository.role.RoleMenuPermissionRepository;
import com.um.api.repository.role.RoleRepository;
import com.um.common.ApiMessages;
import com.um.exception.ServiceException;

@Service
public class RoleMenuPermissionServiceImpl implements IRoleMenuPermissionService {

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private UmApplicationRepository applicationRepository;

	@Autowired
	private RoleMenuPermissionRepository permissionRepository;

	@Override
	public List<RoleMenuPermissionRowResponse> getPermissions(GetRoleMenuPermissionsRequest request) {

		Role role = roleRepository.findById(request.getRoleId())
				.orElseThrow(() -> new ServiceException(ApiMessages.ROLE_NOT_FOUND, HttpStatus.NOT_FOUND));

		Map<Long, RoleMenuPermission> byMenu = permissionRepository.findByIdRoleId(role.getId()).stream()
				.collect(Collectors.toMap(r -> r.getId().getMenuId(), Function.identity()));

		List<RoleMenuPermissionRowResponse> rows = new ArrayList<>();

		List<UmApplication> apps = applicationRepository.findByIsActiveOrderByNameAsc(true);
		for (UmApplication app : apps) {
			List<UmMenu> roots = app.getMenus();
			if (roots == null) {
				continue;
			}
			for (UmMenu root : roots) {
				if (!Boolean.TRUE.equals(root.getIsActive())) {
					continue;
				}
				walkMenus(root, app, "", null, rows, byMenu);
			}
		}

		return rows;
	}

	private void walkMenus(UmMenu menu, UmApplication app, String pathPrefix, Long parentMenuId,
			List<RoleMenuPermissionRowResponse> rows, Map<Long, RoleMenuPermission> byMenu) {

		String path = pathPrefix.isEmpty() ? menu.getName() : pathPrefix + " / " + menu.getName();

		RoleMenuPermissionRowResponse row = new RoleMenuPermissionRowResponse();
		row.setMenuId(menu.getId());
		row.setParentMenuId(parentMenuId);
		row.setApplicationId(app.getId());
		row.setApplicationName(app.getName());
		row.setMenuPath(path);
		row.setRoute(menu.getRoute());

		RoleMenuPermission p = byMenu.get(menu.getId());
		if (p != null) {
			row.setAllowView(p.isAllowView());
			row.setAllowAdd(p.isAllowAdd());
			row.setAllowEdit(p.isAllowEdit());
			row.setAllowDelete(p.isAllowDelete());
		} else {
			row.setAllowView(false);
			row.setAllowAdd(false);
			row.setAllowEdit(false);
			row.setAllowDelete(false);
		}
		rows.add(row);

		if (menu.getMenus() != null) {
			for (UmMenu child : menu.getMenus()) {
				if (!Boolean.TRUE.equals(child.getIsActive())) {
					continue;
				}
				walkMenus(child, app, path, menu.getId(), rows, byMenu);
			}
		}
	}

	@Override
	@Transactional
	public void savePermissions(SaveRoleMenuPermissionsRequest request) {

		Role role = roleRepository.findById(request.getRoleId())
				.orElseThrow(() -> new ServiceException(ApiMessages.ROLE_NOT_FOUND, HttpStatus.NOT_FOUND));

		permissionRepository.deleteByIdRoleId(role.getId());

		List<RoleMenuPermission> toSave = new ArrayList<>();
		if (request.getPermissions() == null) {
			return;
		}
		for (RoleMenuPermissionEntryDto e : request.getPermissions()) {
			if (!e.isAllowView() && !e.isAllowAdd() && !e.isAllowEdit() && !e.isAllowDelete()) {
				continue;
			}
			RoleMenuPermissionId id = new RoleMenuPermissionId();
			id.setRoleId(role.getId());
			id.setMenuId(e.getMenuId());

			RoleMenuPermission row = new RoleMenuPermission();
			row.setId(id);
			row.setAllowView(e.isAllowView());
			row.setAllowAdd(e.isAllowAdd());
			row.setAllowEdit(e.isAllowEdit());
			row.setAllowDelete(e.isAllowDelete());
			toSave.add(row);
		}

		if (!toSave.isEmpty()) {
			permissionRepository.saveAll(toSave);
		}
	}
}
