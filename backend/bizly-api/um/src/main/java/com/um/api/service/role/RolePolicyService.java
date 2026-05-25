package com.um.api.service.role;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.um.api.domain.RoleKind;
import com.um.api.service.security.MenuPermissionService;
import com.um.api.dto.role.permission.RoleMenuPermissionEntryDto;
import com.um.api.model.role.Role;
import com.um.api.model.role.RoleMenuPermission;
import com.um.api.model.user.User;
import com.um.api.repository.role.RoleMenuPermissionRepository;
import com.um.api.repository.role.RoleRepository;
import com.um.api.repository.role.UserRoleRepository;
import com.um.api.repository.user.UserRepository;
import com.um.common.ApiMessages;
import com.um.exception.ServiceException;
import com.um.security.BusinessContextHolder;

/**
 * Central validation for hierarchical tenant RBAC on {@code UM_ROLE} and menu matrices.
 */
@Service
public class RolePolicyService {

	@Autowired
	private RoleRepository roleRepository;
	@Autowired
	private UserRoleRepository userRoleRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private RoleMenuPermissionRepository roleMenuPermissionRepository;
	@Autowired
	private MenuPermissionService menuPermissionService;

	public boolean seesEntireBusinessTeamHierarchy(Long userId) {
		if (userId == null) {
			return false;
		}
		User user = userRepository.findById(userId).orElse(null);
		return user != null && user.isBusinessOwnerFlag();
	}

	/**
	 * Team role ids visible to the actor: entire tenant for business owners; otherwise the logged-in
	 * role(s) and every descendant team role in the same business (not sibling branches).
	 */
	public Set<Long> visibleTeamRoleIds(Long userId, Long businessId) {
		if (seesEntireBusinessTeamHierarchy(userId)) {
			return roleRepository.findByBusinessIdAndRoleKind(businessId, RoleKind.BUSINESS_TEAM.name()).stream()
					.map(Role::getId).collect(Collectors.toSet());
		}
		Set<Long> anchors = actorTeamRoleIdsInBusiness(businessId);
		if (anchors.isEmpty()) {
			return Collections.emptySet();
		}
		return collectTeamRoleSubtree(businessId, anchors);
	}

	public List<Role> filterTeamRolesVisibleToActor(Long userId, Long businessId, List<Role> teamRoles) {
		Set<Long> visible = visibleTeamRoleIds(userId, businessId);
		return teamRoles.stream().filter(r -> visible.contains(r.getId())).collect(Collectors.toList());
	}

	public void assertActorMayViewTeamRole(Long userId, Long businessId, Long roleId) {
		if (BusinessContextHolder.canBypassTenant()) {
			return;
		}
		Role role = roleRepository.findById(roleId)
				.orElseThrow(() -> new ServiceException(ApiMessages.ROLE_NOT_FOUND, HttpStatus.NOT_FOUND));
		if (!RoleKind.BUSINESS_TEAM.name().equals(role.getRoleKind())
				|| role.getBusinessId() == null || !role.getBusinessId().equals(businessId)) {
			throw new ServiceException(ApiMessages.ROLE_NOT_FOUND, HttpStatus.FORBIDDEN);
		}
		if (!visibleTeamRoleIds(userId, businessId).contains(roleId)) {
			throw new ServiceException(ApiMessages.ROLE_NOT_FOUND, HttpStatus.FORBIDDEN);
		}
	}

	public void assertActorMayViewUser(Long userId, Long businessId, Long targetUserId) {
		if (BusinessContextHolder.canBypassTenant()) {
			return;
		}
		if (seesEntireBusinessTeamHierarchy(userId)) {
			return;
		}
		Set<Long> visibleRoles = visibleTeamRoleIds(userId, businessId);
		if (visibleRoles.isEmpty()) {
			throw new ServiceException(ApiMessages.USER_NOT_FOUND, HttpStatus.FORBIDDEN);
		}
		List<Long> targetRoleIds = userRoleRepository.findById_UserId(targetUserId).stream()
				.map(ur -> ur.getId().getRoleId()).collect(Collectors.toList());
		for (Long rid : targetRoleIds) {
			if (visibleRoles.contains(rid)) {
				return;
			}
		}
		throw new ServiceException(ApiMessages.USER_NOT_FOUND, HttpStatus.FORBIDDEN);
	}

	public List<Long> visibleUserIds(Long userId, Long businessId) {
		if (seesEntireBusinessTeamHierarchy(userId) || BusinessContextHolder.canBypassTenant()) {
			return null;
		}
		Set<Long> roleIds = visibleTeamRoleIds(userId, businessId);
		if (roleIds.isEmpty()) {
			return Collections.emptyList();
		}
		return userRoleRepository.findDistinctUserIdsByRoleIdIn(roleIds);
	}

	/**
	 * Usernames whose audit rows a business actor may see. {@code null} = entire tenant;
	 * empty = none (sub-role with no subtree).
	 */
	public List<String> visibleAuditableUsernames(Long userId, Long businessId) {
		List<Long> userIds = visibleUserIds(userId, businessId);
		if (userIds == null) {
			return null;
		}
		if (userIds.isEmpty()) {
			return Collections.emptyList();
		}
		return userRepository.findUsernamesByIdIn(userIds);
	}

	private Set<Long> actorTeamRoleIdsInBusiness(Long businessId) {
		Set<Long> out = new HashSet<>();
		for (Long roleId : menuPermissionService.resolveAllAssignedRoleIds()) {
			Role r = roleRepository.findById(roleId).orElse(null);
			if (r != null && RoleKind.BUSINESS_TEAM.name().equals(r.getRoleKind())
					&& businessId.equals(r.getBusinessId())) {
				out.add(roleId);
			}
		}
		return out;
	}

	private Set<Long> collectTeamRoleSubtree(Long businessId, Set<Long> anchorRoleIds) {
		List<Role> all = roleRepository.findByBusinessIdAndRoleKind(businessId, RoleKind.BUSINESS_TEAM.name());
		Map<Long, List<Long>> childrenByParent = new HashMap<>();
		for (Role r : all) {
			Long parentId = r.getParentRoleId();
			if (parentId != null) {
				childrenByParent.computeIfAbsent(parentId, k -> new ArrayList<>()).add(r.getId());
			}
		}
		Set<Long> subtree = new HashSet<>();
		Deque<Long> queue = new ArrayDeque<>(anchorRoleIds);
		while (!queue.isEmpty()) {
			Long id = queue.poll();
			if (!subtree.add(id)) {
				continue;
			}
			for (Long childId : childrenByParent.getOrDefault(id, Collections.emptyList())) {
				queue.add(childId);
			}
		}
		return subtree;
	}

	/**
	 * Parent for a new {@link RoleKind#BUSINESS_TEAM} row must be either the sector template for this
	 * business, or another team role in the same tenant.
	 */
	public void validateTeamRoleParent(Role parent, Long businessId, Long sectorTemplateId) {
		if (parent.getBusinessId() == null) {
			if (!RoleKind.BUSINESS_TYPE_TEMPLATE.name().equals(parent.getRoleKind())) {
				throw new ServiceException("Parent must be a global business type template.", HttpStatus.BAD_REQUEST);
			}
			if (sectorTemplateId == null || !sectorTemplateId.equals(parent.getId())) {
				throw new ServiceException("Parent template does not match this business type.", HttpStatus.BAD_REQUEST);
			}
			return;
		}
		if (!businessId.equals(parent.getBusinessId())) {
			throw new ServiceException("Cross-tenant parent role is not allowed.", HttpStatus.FORBIDDEN);
		}
		if (!RoleKind.BUSINESS_TEAM.name().equals(parent.getRoleKind())) {
			throw new ServiceException("Invalid parent role kind for a team role.", HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * Business owners may assign any team role in the tenant. Other users may assign only roles in the
	 * subtree below one of their own roles (walk {@code parent_role_id} upward from the candidate).
	 */
	public void assertCallerMayAssignTeamRole(Role role, Long businessId, Long callerUserId) {
		if (!RoleKind.BUSINESS_TEAM.name().equals(role.getRoleKind())) {
			throw new ServiceException("Only business team roles can be assigned here.", HttpStatus.FORBIDDEN);
		}
		if (role.getBusinessId() == null || !role.getBusinessId().equals(businessId)) {
			throw new ServiceException(ApiMessages.ROLE_NOT_FOUND, HttpStatus.FORBIDDEN);
		}
		if (callerUserId == null) {
			throw new ServiceException("Unable to resolve the current user for role assignment.", HttpStatus.FORBIDDEN);
		}
		User actor = userRepository.findById(callerUserId)
				.orElseThrow(() -> new ServiceException(ApiMessages.USER_NOT_FOUND, HttpStatus.FORBIDDEN));
		if (actor.isBusinessOwnerFlag()) {
			return;
		}
		Set<Long> actorRoleIds = new HashSet<>();
		for (var ur : userRoleRepository.findById_UserId(callerUserId)) {
			actorRoleIds.add(ur.getId().getRoleId());
		}
		Long rid = role.getId();
		while (rid != null) {
			if (actorRoleIds.contains(rid)) {
				return;
			}
			Role walk = roleRepository.findById(rid).orElse(null);
			rid = walk != null ? walk.getParentRoleId() : null;
		}
		throw new ServiceException("You cannot assign a role outside your delegated hierarchy.", HttpStatus.FORBIDDEN);
	}

	public List<Role> filterTeamRolesAssignableBy(Long callerUserId, Long businessId, List<Role> teamRoles) {
		if (callerUserId == null) {
			return teamRoles;
		}
		User actor = userRepository.findById(callerUserId).orElse(null);
		if (actor != null && actor.isBusinessOwnerFlag()) {
			return teamRoles;
		}
		Set<Long> actorRoleIds = new HashSet<>();
		for (var ur : userRoleRepository.findById_UserId(callerUserId)) {
			actorRoleIds.add(ur.getId().getRoleId());
		}
		return teamRoles.stream().filter(r -> mayAssignTeamRole(actorRoleIds, r.getId())).collect(Collectors.toList());
	}

	private boolean mayAssignTeamRole(Set<Long> actorRoleIds, Long candidateRoleId) {
		Long rid = candidateRoleId;
		while (rid != null) {
			if (actorRoleIds.contains(rid)) {
				return true;
			}
			Role walk = roleRepository.findById(rid).orElse(null);
			rid = walk != null ? walk.getParentRoleId() : null;
		}
		return false;
	}

	/**
	 * Menus a child role may appear in the permission matrix. Empty = full catalog (root portal admin only).
	 * Non-empty = only menus the immediate parent role was granted.
	 */
	public Set<Long> catalogMenuIdsForPermissionMatrix(Role role) {
		Long parentId = role.getParentRoleId();
		if (parentId == null) {
			return Collections.emptySet();
		}
		if (!RoleKind.BUSINESS_TEAM.name().equals(role.getRoleKind())
				&& !RoleKind.ADMIN_INTERNAL.name().equals(role.getRoleKind())) {
			return Collections.emptySet();
		}
		return roleMenuPermissionRepository.findByIdRoleId(parentId).stream()
				.filter(RolePolicyService::parentGrantsMenuToChildren)
				.map(p -> p.getId().getMenuId())
				.collect(Collectors.toSet());
	}

	/** {@code true} when the role should list every active UM menu in the matrix editor. */
	public boolean usesFullMenuCatalog(Role role) {
		if (isRootPortalAdmin(role)) {
			return true;
		}
		if (BusinessContextHolder.canBypassTenant() && !RoleKind.BUSINESS_TEAM.name().equals(role.getRoleKind())
				&& !isPortalDelegateAdmin(role)) {
			return true;
		}
		return role.getParentRoleId() == null
				&& !RoleKind.BUSINESS_TEAM.name().equals(role.getRoleKind())
				&& !RoleKind.ADMIN_INTERNAL.name().equals(role.getRoleKind());
	}

	/** Portal root (e.g. "Super Admin"): global internal admin, system-restricted, no parent. */
	public boolean isRootPortalAdmin(Role role) {
		return role != null
				&& RoleKind.ADMIN_INTERNAL.name().equals(role.getRoleKind())
				&& role.getBusinessId() == null
				&& role.getParentRoleId() == null
				&& role.isSystemRestricted();
	}

	/** Delegated portal admin created under the root — inherits menus from {@link #resolveRootPortalAdminRole()}. */
	public boolean isPortalDelegateAdmin(Role role) {
		return role != null
				&& RoleKind.ADMIN_INTERNAL.name().equals(role.getRoleKind())
				&& role.getBusinessId() == null
				&& role.getParentRoleId() != null
				&& !role.isSystemRestricted();
	}

	public Role resolveRootPortalAdminRole() {
		return roleRepository
				.findFirstByRoleKindAndBusinessIdIsNullAndParentRoleIdIsNullAndIsSystemRestricted(
						RoleKind.ADMIN_INTERNAL.name(), 1)
				.orElseThrow(() -> new ServiceException(
						"No portal root admin role found. Seed one row in UM_ROLE with role_kind=ADMIN_INTERNAL, "
								+ "is_system_restricted=1, parent_role_id=NULL, business_id=NULL.",
						HttpStatus.INTERNAL_SERVER_ERROR));
	}

	public Long resolveRootPortalAdminRoleId() {
		return resolveRootPortalAdminRole().getId();
	}

	/** Links a new or legacy internal admin role under the portal root. */
	public void ensurePortalDelegateParent(Role role) {
		if (!RoleKind.ADMIN_INTERNAL.name().equals(role.getRoleKind()) || role.getBusinessId() != null) {
			return;
		}
		if (isRootPortalAdmin(role)) {
			return;
		}
		if (role.getParentRoleId() == null) {
			role.setParentRoleId(resolveRootPortalAdminRoleId());
		}
	}

	public void assertMenusGrantedToActor(List<RoleMenuPermissionEntryDto> requested) {
		if (BusinessContextHolder.canBypassTenant() || requested == null) {
			return;
		}
		Set<Long> actorMenus = menuPermissionService.menuIdsGrantedToCurrentPrincipal();
		for (RoleMenuPermissionEntryDto e : requested) {
			if (!e.isAllowView() && !e.isAllowAdd() && !e.isAllowEdit() && !e.isAllowDelete()) {
				continue;
			}
			if (!actorMenus.contains(e.getMenuId())) {
				throw new ServiceException(
						"Cannot assign menu access that your account does not have (menu " + e.getMenuId() + ").",
						HttpStatus.BAD_REQUEST);
			}
		}
	}

	public void assertMenuMatrixSubsetOfParent(Role child, List<RoleMenuPermissionEntryDto> requested) {
		/*
		 * Business portal: the acting user's own menu grants are the ceiling (see assertMenusGrantedToActor).
		 * Immediate parent's DB row may lag (e.g. template copy without add flags) even though the actor can add.
		 */
		Long parentId = child.getParentRoleId();
		if (parentId == null) {
			return;
		}
		if (!BusinessContextHolder.canBypassTenant()
				&& RoleKind.BUSINESS_TEAM.name().equals(child.getRoleKind())) {
			return;
		}
		if (!RoleKind.BUSINESS_TEAM.name().equals(child.getRoleKind())
				&& !RoleKind.ADMIN_INTERNAL.name().equals(child.getRoleKind())) {
			return;
		}
		Role parent = roleRepository.findById(parentId)
				.orElseThrow(() -> new ServiceException("Parent role not found for permission validation.",
						HttpStatus.BAD_REQUEST));
		Map<Long, RoleMenuPermission> parentByMenu = new HashMap<>();
		for (RoleMenuPermission p : roleMenuPermissionRepository.findByIdRoleId(parent.getId())) {
			parentByMenu.put(p.getId().getMenuId(), p);
		}
		for (RoleMenuPermissionEntryDto e : requested) {
			if (!e.isAllowView() && !e.isAllowAdd() && !e.isAllowEdit() && !e.isAllowDelete()) {
				continue;
			}
			RoleMenuPermission par = parentByMenu.get(e.getMenuId());
			if (par == null || !par.isAllowView()) {
				throw new ServiceException(
						"Cannot grant menu access that the parent role does not possess (menu " + e.getMenuId() + ").",
						HttpStatus.BAD_REQUEST);
			}
			if (e.isAllowView() && !par.isAllowView()) {
				throw new ServiceException("Cannot exceed parent view permission.", HttpStatus.BAD_REQUEST);
			}
			if (e.isAllowAdd() && !par.isAllowAdd()) {
				throw new ServiceException("Cannot exceed parent add permission.", HttpStatus.BAD_REQUEST);
			}
			if (e.isAllowEdit() && !par.isAllowEdit()) {
				throw new ServiceException("Cannot exceed parent edit permission.", HttpStatus.BAD_REQUEST);
			}
			if (e.isAllowDelete() && !par.isAllowDelete()) {
				throw new ServiceException("Cannot exceed parent delete permission.", HttpStatus.BAD_REQUEST);
			}
		}
	}

	private static boolean parentGrantsMenuToChildren(RoleMenuPermission p) {
		return p.isAllowView() || p.isAllowAdd() || p.isAllowEdit() || p.isAllowDelete();
	}

	public void assertVisibleRole(Role role, Long actorUserId) {
		if (BusinessContextHolder.isPortalAdminRoleLevel()) {
			if (role.getBusinessId() != null && RoleKind.BUSINESS_TEAM.name().equals(role.getRoleKind())) {
				Long ctx = BusinessContextHolder.currentBusinessId();
				if (ctx == null || !ctx.equals(role.getBusinessId())) {
					throw new ServiceException(ApiMessages.ROLE_NOT_FOUND, HttpStatus.NOT_FOUND);
				}
			}
			return;
		}
		Long biz = BusinessContextHolder.requireBusinessId();
		if (!RoleKind.BUSINESS_TEAM.name().equals(role.getRoleKind())
				|| role.getBusinessId() == null || !role.getBusinessId().equals(biz)) {
			throw new ServiceException(ApiMessages.ROLE_NOT_FOUND, HttpStatus.FORBIDDEN);
		}
		assertActorMayViewTeamRole(actorUserId, biz, role.getId());
	}

	/**
	 * Blocks changing a team role the actor is currently logged in as (active session role).
	 */
	public void assertActorMayMutateTeamRole(Long actorUserId, Long businessId, Long roleId) {
		if (BusinessContextHolder.canBypassTenant() || roleId == null) {
			return;
		}
		for (Long assignedId : menuPermissionService.resolveAllAssignedRoleIds()) {
			if (!assignedId.equals(roleId)) {
				continue;
			}
			Role r = roleRepository.findById(roleId).orElse(null);
			if (r != null && RoleKind.BUSINESS_TEAM.name().equals(r.getRoleKind())
					&& businessId != null && businessId.equals(r.getBusinessId())) {
				throw new ServiceException(
						"You cannot modify the team role you are currently logged in with.",
						HttpStatus.FORBIDDEN);
			}
		}
	}

	public void assertMutableRole(Role role) {
		if (RoleKind.BUSINESS_TYPE_TEMPLATE.name().equals(role.getRoleKind())) {
			if (role.isSystemRestricted()) {
				throw new ServiceException("System business type templates cannot be modified here.",
						HttpStatus.FORBIDDEN);
			}
		}
		if (RoleKind.BUSINESS_TEAM.name().equals(role.getRoleKind())) {
			if (!BusinessContextHolder.canBypassTenant()) {
				Long biz = BusinessContextHolder.requireBusinessId();
				if (role.getBusinessId() == null || !role.getBusinessId().equals(biz)) {
					throw new ServiceException(ApiMessages.ROLE_NOT_FOUND, HttpStatus.FORBIDDEN);
				}
			} else {
				Long ctx = BusinessContextHolder.currentBusinessId();
				if (ctx == null || !ctx.equals(role.getBusinessId())) {
					throw new ServiceException(
							"Select the tenant in the business context switcher before changing this team role.",
							HttpStatus.BAD_REQUEST);
				}
			}
		}
	}
}
