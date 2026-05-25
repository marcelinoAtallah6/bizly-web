package com.um.api.service.role;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.um.api.domain.RoleKind;
import com.um.api.dto.role.add.AddRoleRequest;
import com.um.api.dto.role.add.AddRoleResponse;
import com.um.api.dto.role.delete.DeleteRoleRequest;
import com.um.api.dto.role.delete.DeleteRoleResponse;
import com.um.api.dto.role.get.GetRoleRequest;
import com.um.api.dto.role.get.GetRoleResponse;
import com.um.api.dto.role.gets.GetsRolesRequest;
import com.um.api.dto.role.update.UpdateRoleRequest;
import com.um.api.dto.role.update.UpdateRoleResponse;
import com.um.api.model.role.Role;
import com.um.api.model.role.RoleLevel;
import com.um.api.repository.role.RoleLevelRepository;
import com.um.api.repository.role.RoleMenuPermissionRepository;
import com.um.api.model.user.User;
import com.um.api.repository.role.RoleRepository;
import com.um.api.repository.role.UserRoleRepository;
import com.um.api.repository.user.UserRepository;
import com.um.common.ApiMessages;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.um.common.PageResponse;
import com.um.exception.ServiceException;
import com.um.security.BusinessContextHolder;

@Service
public class RoleServiceImpl implements IRoleService {

	@Autowired
	private RoleRepository repository;

	@Autowired
	private RoleLevelRepository roleLevelRepository;

	@Autowired
	private RoleMenuPermissionRepository roleMenuPermissionRepository;

	@Autowired
	private UserRoleRepository userRoleRepository;

	@Autowired
	private RolePolicyService rolePolicyService;
	@Autowired
	private UserRepository userRepository;

	@Override
	public AddRoleResponse add(AddRoleRequest request) {
		if (!BusinessContextHolder.isPortalAdminRoleLevel()) {
			throw new ServiceException("Business owners create team roles under /um/business/team-role/add",
					HttpStatus.FORBIDDEN);
		}
		RoleLevel level = roleLevelRepository.findById(request.getRoleLevelId())
				.orElseThrow(() -> new ServiceException("Invalid role level", HttpStatus.BAD_REQUEST));

		Role role = new Role();
		role.setName(request.getName().trim());
		role.setRoleLevelId(level.getId());
		role.setCreatedAt(LocalDateTime.now());
		role.setIsDefaultForRegistration(0);
		role.setIsSystemRestricted(0);

		int nextType = nextRoleTypeCode();
		if (RoleLevel.CODE_ADMIN.equalsIgnoreCase(level.getCode())) {
			role.setRoleKind(RoleKind.ADMIN_INTERNAL.name());
			role.setIsBusinessType(0);
			role.setRoleType(nextType);
		} else if (RoleLevel.CODE_BUSINESS.equalsIgnoreCase(level.getCode())) {
			role.setRoleKind(RoleKind.BUSINESS_TYPE_TEMPLATE.name());
			role.setIsBusinessType(1);
			role.setRoleType(nextType);
		} else {
			throw new ServiceException("Unsupported role level: " + level.getCode(), HttpStatus.BAD_REQUEST);
		}

		if (RoleKind.ADMIN_INTERNAL.name().equals(role.getRoleKind())) {
			rolePolicyService.ensurePortalDelegateParent(role);
		}

		repository.save(role);

		AddRoleResponse response = new AddRoleResponse();
		response.setId(role.getId());
		return response;
	}

	@Override
	public UpdateRoleResponse update(UpdateRoleRequest request) {
		Role role = repository.findById(request.getId())
				.orElseThrow(() -> new ServiceException(ApiMessages.ROLE_NOT_FOUND, HttpStatus.NOT_FOUND));
		Long actorId = currentActorUserId();
		rolePolicyService.assertVisibleRole(role, actorId);
		rolePolicyService.assertMutableRole(role);
		if (RoleKind.BUSINESS_TEAM.name().equals(role.getRoleKind()) && role.getBusinessId() != null) {
			rolePolicyService.assertActorMayMutateTeamRole(actorId, role.getBusinessId(), role.getId());
		}

		role.setName(request.getName().trim());

		if (RoleKind.ADMIN_INTERNAL.name().equals(role.getRoleKind())) {
			rolePolicyService.ensurePortalDelegateParent(role);
		}

		repository.save(role);

		UpdateRoleResponse response = new UpdateRoleResponse();
		response.setId(role.getId());
		return response;
	}

	@Override
	@Transactional
	public DeleteRoleResponse delete(DeleteRoleRequest request) {
		Role role = repository.findById(request.getId())
				.orElseThrow(() -> new ServiceException(ApiMessages.ROLE_NOT_FOUND, HttpStatus.NOT_FOUND));
		rolePolicyService.assertMutableRole(role);
		userRoleRepository.deleteByRoleId(request.getId());
		roleMenuPermissionRepository.deleteByIdRoleId(request.getId());
		repository.deleteById(request.getId());

		DeleteRoleResponse response = new DeleteRoleResponse();
		response.setId(request.getId());
		return response;
	}

	@Override
	public GetRoleResponse get(GetRoleRequest request) {
		Role role = repository.findById(request.getId())
				.orElseThrow(() -> new ServiceException(ApiMessages.ROLE_NOT_FOUND, HttpStatus.NOT_FOUND));
		rolePolicyService.assertVisibleRole(role, currentActorUserId());
		return buildResponse(role);
	}

	@Override
	public PageResponse<GetRoleResponse> gets(GetsRolesRequest request) {
		if (!BusinessContextHolder.isPortalAdminRoleLevel()) {
			throw new ServiceException("Business users list team roles under /um/business/team-role/list",
					HttpStatus.FORBIDDEN);
		}
		List<Role> roles;
		if (request.getForBusinessId() != null) {
			roles = repository.findByBusinessIdAndRoleKind(request.getForBusinessId(), RoleKind.BUSINESS_TEAM.name());
		} else if (Boolean.TRUE.equals(request.getGlobalTemplatesOnly())) {
			roles = repository.findByBusinessIdIsNullAndRoleKindIn(Arrays.asList(
					RoleKind.ADMIN_INTERNAL.name(),
					RoleKind.BUSINESS_TYPE_TEMPLATE.name()));
		} else {
			roles = repository.findByBusinessIdIsNullAndRoleKindIn(Arrays.asList(
					RoleKind.ADMIN_INTERNAL.name(),
					RoleKind.BUSINESS_TYPE_TEMPLATE.name()));
		}

		List<GetRoleResponse> items = roles.stream().map(this::buildResponse).collect(Collectors.toList());

		PageResponse<GetRoleResponse> response = new PageResponse<>();
		response.setItems(items);
		response.setTotalCount(items.size());
		response.setPageNumber(0);
		response.setPageSize(items.size());
		response.setTotalPages(1);
		return response;
	}

	@Override
	public int nextRoleTypeCode() {
		return repository.findMaxRoleType().map(v -> v + 1).orElse(1);
	}

	private GetRoleResponse buildResponse(Role role) {
		GetRoleResponse response = new GetRoleResponse();
		response.setId(role.getId());
		response.setName(role.getName());
		response.setRoleType(role.getRoleType());
		response.setRoleLevelId(role.getRoleLevelId());
		response.setRoleKind(role.getRoleKind());
		response.setCreatedAt(role.getCreatedAt());
		response.setParentRoleId(role.getParentRoleId());
		response.setBusinessId(role.getBusinessId());
		if (role.getRoleLevelId() != null) {
			roleLevelRepository.findById(role.getRoleLevelId())
					.ifPresent(l -> response.setRoleLevelCode(l.getCode()));
		}
		return response;
	}

	private Long currentActorUserId() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
			return null;
		}
		return userRepository.findFirstByUsernameOrderByIdAsc(auth.getName()).map(User::getId).orElse(null);
	}
}
