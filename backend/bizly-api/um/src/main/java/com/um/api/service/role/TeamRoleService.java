package com.um.api.service.role;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.um.api.domain.RoleKind;
import com.um.api.dto.role.team.CreateTeamRoleRequest;
import com.um.api.dto.role.team.ParentRoleOptionResponse;
import com.um.api.dto.role.team.TeamRoleResponse;
import com.um.api.model.business.Business;
import com.um.api.model.role.Role;
import com.um.api.model.user.User;
import com.um.api.repository.business.BusinessRepository;
import com.um.api.repository.role.RoleMenuPermissionRepository;
import com.um.api.repository.role.RoleRepository;
import com.um.api.repository.role.UserRoleRepository;
import com.um.api.repository.user.UserRepository;
import com.um.common.ApiMessages;
import com.um.exception.ServiceException;
import com.um.security.BusinessContextHolder;

@Service
public class TeamRoleService {

	@Autowired
	private RoleRepository roleRepository;
	@Autowired
	private BusinessRepository businessRepository;
	@Autowired
	private PermissionPropagationService permissionPropagationService;
	@Autowired
	private RolePolicyService rolePolicyService;
	@Autowired
	private UserRoleRepository userRoleRepository;
	@Autowired
	private RoleMenuPermissionRepository roleMenuPermissionRepository;
	@Autowired
	private UserRepository userRepository;

	public List<TeamRoleResponse> listForCurrentBusiness() {
		Long businessId = BusinessContextHolder.requireBusinessId();
		assertNotPortalAdmin();
		Long userId = currentActorUserId();
		List<Role> all = roleRepository.findByBusinessIdAndRoleKind(businessId, RoleKind.BUSINESS_TEAM.name());
		return rolePolicyService.filterTeamRolesVisibleToActor(userId, businessId, all)
				.stream().map(this::toResponse).collect(Collectors.toList());
	}

	/**
	 * Team roles the acting user may assign when editing users (hierarchy + business-owner bypass).
	 */
	public List<TeamRoleResponse> listAssignableForUserManagement() {
		Long businessId = BusinessContextHolder.requireBusinessId();
		assertNotPortalAdmin();
		Long userId = currentActorUserId();
		List<Role> all = roleRepository.findByBusinessIdAndRoleKind(businessId, RoleKind.BUSINESS_TEAM.name());
		List<Role> filtered = rolePolicyService.filterTeamRolesAssignableBy(userId, businessId, all);
		return filtered.stream().map(this::toResponse).collect(Collectors.toList());
	}

	public List<ParentRoleOptionResponse> listParentRoleOptions() {
		Long businessId = BusinessContextHolder.requireBusinessId();
		assertNotPortalAdmin();
		Business business = businessRepository.findById(businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.BUSINESS_NOT_FOUND, HttpStatus.NOT_FOUND));
		Long templateId = business.getBusinessTypeRoleId();
		if (templateId == null) {
			throw new ServiceException("Business has no assigned type template.", HttpStatus.BAD_REQUEST);
		}
		Role template = roleRepository.findById(templateId)
				.orElseThrow(() -> new ServiceException(ApiMessages.ROLE_NOT_FOUND, HttpStatus.BAD_REQUEST));
		List<ParentRoleOptionResponse> out = new ArrayList<>();
		ParentRoleOptionResponse t = new ParentRoleOptionResponse();
		t.setId(template.getId());
		t.setName(template.getName());
		t.setOptionKind("TEMPLATE");
		out.add(t);
		Long userId = currentActorUserId();
		List<Role> teamRoles = roleRepository.findByBusinessIdAndRoleKind(businessId, RoleKind.BUSINESS_TEAM.name());
		for (Role r : rolePolicyService.filterTeamRolesVisibleToActor(userId, businessId, teamRoles)) {
			ParentRoleOptionResponse row = new ParentRoleOptionResponse();
			row.setId(r.getId());
			row.setName(r.getName());
			row.setOptionKind("TEAM");
			out.add(row);
		}
		return out;
	}

	@Transactional
	public TeamRoleResponse create(CreateTeamRoleRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		assertNotPortalAdmin();

		Business business = businessRepository.findById(businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.BUSINESS_NOT_FOUND, HttpStatus.NOT_FOUND));
		Long templateId = business.getBusinessTypeRoleId();
		if (templateId == null) {
			throw new ServiceException("Business has no assigned type template.", HttpStatus.BAD_REQUEST);
		}
		Role sectorTemplate = roleRepository.findById(templateId)
				.orElseThrow(() -> new ServiceException(ApiMessages.ROLE_NOT_FOUND, HttpStatus.BAD_REQUEST));
		if (!RoleKind.BUSINESS_TYPE_TEMPLATE.name().equals(sectorTemplate.getRoleKind())) {
			throw new ServiceException("Invalid business type template.", HttpStatus.BAD_REQUEST);
		}

		Long parentId = request.getParentRoleId() != null ? request.getParentRoleId() : templateId;
		Role parent = roleRepository.findById(parentId)
				.orElseThrow(() -> new ServiceException(ApiMessages.ROLE_NOT_FOUND, HttpStatus.BAD_REQUEST));
		rolePolicyService.validateTeamRoleParent(parent, businessId, templateId);
		Long actorId = currentActorUserId();
		if (!parentId.equals(templateId)) {
			rolePolicyService.assertActorMayViewTeamRole(actorId, businessId, parentId);
		}

		Role team = new Role();
		team.setName(request.getName().trim());
		team.setRoleType(parent.getRoleType());
		team.setRoleLevelId(parent.getRoleLevelId());
		team.setRoleKind(RoleKind.BUSINESS_TEAM.name());
		team.setBusinessId(businessId);
		team.setParentRoleId(parent.getId());
		team.setIsSystemRestricted(0);
		team.setIsDefaultForRegistration(0);
		team.setIsBusinessType(0);
		team.setCreatedAt(LocalDateTime.now());
		roleRepository.save(team);

		permissionPropagationService.copyFromRole(parent.getId(), team.getId());
		return toResponse(team);
	}

	@Transactional
	public void delete(Long roleId) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		assertNotPortalAdmin();
		Role role = roleRepository.findById(roleId)
				.orElseThrow(() -> new ServiceException(ApiMessages.ROLE_NOT_FOUND, HttpStatus.NOT_FOUND));
		if (!RoleKind.BUSINESS_TEAM.name().equals(role.getRoleKind())
				|| role.getBusinessId() == null || !role.getBusinessId().equals(businessId)) {
			throw new ServiceException("Only team roles in your business can be deleted.", HttpStatus.FORBIDDEN);
		}
		rolePolicyService.assertActorMayViewTeamRole(currentActorUserId(), businessId, roleId);
		userRoleRepository.deleteByRoleId(roleId);
		roleMenuPermissionRepository.deleteByIdRoleId(roleId);
		roleRepository.delete(role);
	}

	private Long currentActorUserId() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
			return null;
		}
		return userRepository.findFirstByUsernameOrderByIdAsc(auth.getName()).map(User::getId).orElse(null);
	}

	private void assertNotPortalAdmin() {
		if (BusinessContextHolder.canBypassTenant()) {
			throw new ServiceException("Portal admins manage templates, not team roles here.", HttpStatus.FORBIDDEN);
		}
	}

	private TeamRoleResponse toResponse(Role r) {
		TeamRoleResponse out = new TeamRoleResponse();
		out.setId(r.getId());
		out.setName(r.getName());
		out.setParentRoleId(r.getParentRoleId());
		out.setBusinessId(r.getBusinessId());
		return out;
	}
}
