package com.um.api.service.role;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import com.um.api.domain.RoleKind;
import com.um.api.model.menu.UmApplication;
import com.um.api.model.menu.UmMenu;
import com.um.api.model.role.Role;
import com.um.api.model.role.RoleMenuPermission;
import com.um.api.model.role.RoleMenuPermissionId;
import com.um.api.model.user.User;
import com.um.api.repository.menu.UmApplicationRepository;
import com.um.api.repository.menu.UmMenuRepository;
import com.um.api.repository.role.RoleMenuPermissionRepository;
import com.um.api.service.menu.MenuTreeSupport;
import com.um.api.repository.role.RoleRepository;
import com.um.api.repository.user.UserRepository;
import com.um.api.service.security.MenuPermissionService;
import com.um.common.ApiMessages;
import com.um.exception.ServiceException;
import com.um.security.BusinessContextHolder;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
public class RoleMenuPermissionServiceImpl implements IRoleMenuPermissionService {

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private UmApplicationRepository applicationRepository;

	@Autowired
	private UmMenuRepository menuRepository;

	@Autowired
	private RoleMenuPermissionRepository permissionRepository;

	@Autowired
	private RolePolicyService rolePolicyService;
	@Autowired
	private MenuPermissionService menuPermissionService;
	@Autowired
	private UserRepository userRepository;

	@Override
	public List<RoleMenuPermissionRowResponse> getPermissions(GetRoleMenuPermissionsRequest request) {

		Role role = roleRepository.findById(request.getRoleId())
				.orElseThrow(() -> new ServiceException(ApiMessages.ROLE_NOT_FOUND, HttpStatus.NOT_FOUND));
		rolePolicyService.assertVisibleRole(role, currentActorUserId());

		Map<Long, RoleMenuPermission> byMenu = permissionRepository.findByIdRoleId(role.getId()).stream()
				.collect(Collectors.toMap(r -> r.getId().getMenuId(), Function.identity()));

		boolean fullCatalog = rolePolicyService.usesFullMenuCatalog(role);
		Set<Long> catalogMenuIds = resolveCatalogMenuIds(role, fullCatalog);

		List<RoleMenuPermissionRowResponse> rows = new ArrayList<>();

		List<UmApplication> apps = applicationRepository.findByIsActiveOrderBySortOrderAscNameAsc(true);
		for (UmApplication app : apps) {
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
				if (!fullCatalog && !catalogMenuIds.contains(menu.getId())) {
					continue;
				}
				RoleMenuPermissionRowResponse row = new RoleMenuPermissionRowResponse();
				row.setMenuId(menu.getId());
				row.setParentMenuId(MenuTreeSupport.parentIdOf(menu));
				row.setApplicationId(app.getId());
				row.setApplicationName(app.getName());
				row.setMenuPath(buildMenuPath(menu, byId));
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
			}
		}

		return rows;
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

	@Override
	@Transactional
	public void savePermissions(SaveRoleMenuPermissionsRequest request) {

		Role role = roleRepository.findById(request.getRoleId())
				.orElseThrow(() -> new ServiceException(ApiMessages.ROLE_NOT_FOUND, HttpStatus.NOT_FOUND));
		Long actorId = currentActorUserId();
		rolePolicyService.assertVisibleRole(role, actorId);
		rolePolicyService.assertMutableRole(role);
		if (RoleKind.BUSINESS_TEAM.name().equals(role.getRoleKind()) && role.getBusinessId() != null) {
			rolePolicyService.assertActorMayMutateTeamRole(actorId, role.getBusinessId(), role.getId());
		}
		if (request.getPermissions() != null) {
			rolePolicyService.assertMenuMatrixSubsetOfParent(role, request.getPermissions());
			rolePolicyService.assertMenusGrantedToActor(request.getPermissions());
			assertMenusInCatalog(role, request.getPermissions());
		}

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

	private Set<Long> resolveCatalogMenuIds(Role role, boolean fullCatalog) {
		if (fullCatalog) {
			return Set.of();
		}
		Set<Long> fromParent = rolePolicyService.catalogMenuIdsForPermissionMatrix(role);
		if (!fromParent.isEmpty()) {
			return fromParent;
		}
		if (BusinessContextHolder.isPortalAdminRoleLevel()) {
			return fromParent;
		}
		return menuPermissionService.menuIdsGrantedToCurrentPrincipal();
	}

	private void assertMenusInCatalog(Role role, List<RoleMenuPermissionEntryDto> requested) {
		if (rolePolicyService.usesFullMenuCatalog(role)) {
			return;
		}
		Set<Long> catalog = resolveCatalogMenuIds(role, false);
		for (RoleMenuPermissionEntryDto e : requested) {
			if (!e.isAllowView() && !e.isAllowAdd() && !e.isAllowEdit() && !e.isAllowDelete()) {
				continue;
			}
			if (!catalog.contains(e.getMenuId())) {
				throw new ServiceException(
						"Menu " + e.getMenuId() + " is outside the menus you may assign.",
						HttpStatus.BAD_REQUEST);
			}
		}
	}

	private Long currentActorUserId() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
			return null;
		}
		return userRepository.findFirstByUsernameOrderByIdAsc(auth.getName()).map(User::getId).orElse(null);
	}
}
