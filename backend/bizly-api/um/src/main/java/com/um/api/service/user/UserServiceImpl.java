package com.um.api.service.user;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.um.api.dto.user.add.AddUserRequest;
import com.um.api.dto.user.add.AddUserResponse;
import com.um.api.dto.user.delete.DeleteUserRequest;
import com.um.api.dto.user.delete.DeleteUserResponse;
import com.um.api.dto.user.get.GetUserRequest;
import com.um.api.dto.user.get.GetUserResponse;
import com.um.api.dto.user.gets.GetsUsersRequest;
import com.um.api.dto.user.profile.UpdateProfileSelfRequest;
import com.um.api.dto.user.update.UpdateUserRequest;
import com.um.api.dto.user.update.UpdateUserResponse;
import com.um.api.domain.RegistrationSource;
import com.um.api.domain.RoleKind;
import com.um.api.domain.UserType;
import com.um.api.model.business.Business;
import com.um.api.model.role.Role;
import com.um.api.model.role.UserRole;
import com.um.api.model.user.User;
import com.um.api.model.user.UserRoleId;
import com.um.api.repository.business.BusinessRepository;
import com.um.api.repository.role.RoleRepository;
import com.um.api.repository.role.UserRoleRepository;
import com.um.api.repository.user.UserRepository;
import com.um.api.service.role.RolePolicyService;
import com.um.common.ApiMessages;
import com.um.common.PageResponse;
import com.um.common.PasswordUtil;
import com.um.common.ProfileImageUtil;
import com.um.exception.ServiceException;
import com.um.api.service.workflow.engine.WorkflowEngineOrchestratorService;
import com.um.security.BusinessContextHolder;

@Service
public class UserServiceImpl implements IUserService {

	@Autowired
	private UserRepository repository;

	@Autowired
	private BusinessRepository businessRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private UserRoleRepository userRoleRepository;

	@Autowired
	private RolePolicyService rolePolicyService;

	@Autowired
	private WorkflowEngineOrchestratorService workflowEngineOrchestrator;

	@Autowired
	private PasswordUtil passwordUtil;

	private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	private static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$";
	private static final String DEFAULT_USER_STATUS = "ACTIVE";
	private static final String DEFAULT_MOBILE_PLACEHOLDER = "-";
	private static final String DEFAULT_AUTH_PROVIDER = "LOCAL";

	@Override
	public AddUserResponse add(AddUserRequest request) {
		User user = new User();
		user.setUsername(request.getUsername());
		user.setFirstName(request.getFirstName());
		user.setLastName(request.getLastName());
		user.setEmail(request.getEmail());
		user.setMobileNumber(request.getMobileNumber());
		user.setStatus(request.getStatus());
		user.setCreatedAt(LocalDateTime.now());
		user.setDateOfBirth(request.getDateOfBirth());
		user.setFirstLogin(1);
		user.setAuthProvider(DEFAULT_AUTH_PROVIDER);

		if (BusinessContextHolder.isPortalAdminRoleLevel()) {
			if (BusinessContextHolder.currentBusinessId() != null) {
				throw new ServiceException(
						"Create business users from the business portal. Portal admins may only update the existing business owner here.",
						HttpStatus.FORBIDDEN);
			}
			user.setUserType(UserType.PORTAL_ADMIN.name());
			user.setRegistrationSource(RegistrationSource.ADMIN_PORTAL.name());
			user.setIsBusinessOwner(0);
		} else {
			user.setUserType(UserType.BUSINESS_USER.name());
			user.setRegistrationSource(RegistrationSource.BUSINESS_PORTAL.name());
			user.setIsBusinessOwner(0);
		}

		/*
		 * Tenant scope: business admins can only create users inside their own tenant.
		 * SUPER_ADMIN (role-level=ADMIN) provisions users across tenants — they may
		 * pre-bind a business id via the X-Business-Override header (already
		 * substituted by BusinessContextHolder) OR leave it NULL for a system-wide
		 * account. Defence-in-depth: we never trust a business id sent in the request
		 * body.
		 */
		user.setBusinessId(BusinessContextHolder.currentBusinessId());

		String decryptedPassword;
		try {
			decryptedPassword = passwordUtil.decryptPassword(request.getPassword());
		} catch (Exception e) {
			throw new ServiceException(ApiMessages.PASSWORD_PROCESSING_FAILED, HttpStatus.BAD_REQUEST);
		}
		if (!decryptedPassword.matches(PASSWORD_REGEX)) {
			throw new ServiceException(ApiMessages.PASSWORD_TOO_WEAK, HttpStatus.BAD_REQUEST);
		}
		user.setPassword(passwordEncoder.encode(decryptedPassword));

		applyOptionalProfileOnCreate(user, request.getProfileImageMimeType(), request.getProfileImageBase64());

		repository.save(user);

		/*
		 * Save user roles. Even though this endpoint is admin-gated
		 * (RequireMenuPermission on the controller), we still refuse to grant {@code
		 * is_system_restricted = 1} roles from here as a defence-in-depth measure. If a
		 * malicious or compromised admin client tries to seed a SUPER_ADMIN role via
		 * /user/add, the assignment is rejected with 403 and logged.
		 */
		for (Long roleId : request.getRoleIds()) {
			Role role = roleRepository.findById(roleId)
					.orElseThrow(() -> new ServiceException(ApiMessages.ROLE_NOT_FOUND, HttpStatus.BAD_REQUEST));
			if (role.isSystemRestricted()) {
				throw new ServiceException("This role cannot be granted via the user admin screen.",
						HttpStatus.FORBIDDEN);
			}
			assertRoleAssignableForCaller(role);
			if (!BusinessContextHolder.canBypassTenant()
					&& RoleKind.BUSINESS_TEAM.name().equals(role.getRoleKind())) {
				Long actorId = currentActorUserId();
				rolePolicyService.assertCallerMayAssignTeamRole(role, BusinessContextHolder.requireBusinessId(),
						actorId);
			}
			UserRole userRole = new UserRole();
			UserRoleId userRoleId = new UserRoleId();
			userRoleId.setUserId(user.getId());
			userRoleId.setRoleId(roleId);
			userRole.setId(userRoleId);
			userRole.setRole(role);
			userRoleRepository.save(userRole);
		}

		triggerUserCreatedWorkflow(user);

		AddUserResponse response = new AddUserResponse();
		response.setId(user.getId());
		return response;
	}

	private void triggerUserCreatedWorkflow(User user) {
		Map<String, Object> ctx = new HashMap<>();
		ctx.put("userId", user.getId());
		ctx.put("username", user.getUsername());
		ctx.put("email", user.getEmail());
		ctx.put("firstName", user.getFirstName());
		ctx.put("lastName", user.getLastName());
		workflowEngineOrchestrator.onActionCompleted(WorkflowEngineOrchestratorService.ACTION_USER_CREATED,
				user.getBusinessId(), ctx);
	}

	@Override
	@Transactional
	public UpdateUserResponse update(UpdateUserRequest request) {
		User user = loadUserForCaller(request.getId());
		assertPortalAdminMayManageBusinessUser(user);

		user.setUsername(request.getUsername());
		user.setFirstName(request.getFirstName());
		user.setLastName(request.getLastName());
		user.setEmail(request.getEmail());
		user.setMobileNumber(request.getMobileNumber());
		if (request.getStatus() != null && !request.getStatus().isBlank()) {
			user.setStatus(request.getStatus().trim());
		} else {
			ensureRequiredUserColumns(user);
		}

		applyProfileOnUpdate(user, request);

		repository.save(user);

		assignRolesForUpdate(user, request.getRoleIds());

		UpdateUserResponse response = new UpdateUserResponse();
		response.setId(user.getId());
		return response;
	}

	@Override
	public DeleteUserResponse delete(DeleteUserRequest request) {
		User user = loadUserForCaller(request.getId());
		assertPortalAdminMayManageBusinessUser(user);
		assertDeleteAllowed(user);
		repository.delete(user);

		DeleteUserResponse response = new DeleteUserResponse();
		response.setId(request.getId());
		return response;
	}

	@Override
	public GetUserResponse get(GetUserRequest request) {
		User user = loadUserForCaller(request.getId());
		assertPortalAdminMayManageBusinessUser(user);
		return mapToResponse(user, true, resolveBusinessNames(List.of(user)));
	}

	/**
	 * Tenant-scoped lookup. Business admins can only see users in their own tenant;
	 * SUPER_ADMIN (role-level=ADMIN) can address any user — including SUPER_ADMIN
	 * accounts that have a NULL business id — across tenants.
	 */
	private User loadUserForCaller(Long id) {
		if (BusinessContextHolder.isPortalAdminRoleLevel()) {
			return repository.findById(id)
					.orElseThrow(() -> new ServiceException(ApiMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND));
		}
		Long businessId = BusinessContextHolder.currentBusinessId();
		if (businessId == null) {
			throw new ServiceException(ApiMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
		}
		User user = repository.findByIdAndBusinessId(id, businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND));
		rolePolicyService.assertActorMayViewUser(currentActorUserId(), businessId, user.getId());
		return user;
	}

	@Override
	@Transactional
	public UpdateUserResponse updateSelfProfile(String username, UpdateProfileSelfRequest req) {
		User user = repository.findFirstByUsernameOrderByIdAsc(username)
				.orElseThrow(() -> new ServiceException(ApiMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND));
		assertSelfTenantAccess(user);
		user.setFirstName(req.getFirstName().trim());
		user.setLastName(req.getLastName().trim());
		if (req.getPhoneNumber() != null) {
			user.setMobileNumber(req.getPhoneNumber().trim());
		}
		if (Boolean.TRUE.equals(req.getClearProfileImage())) {
			user.setProfileImageMime(null);
			user.setProfileImageData(null);
		} else if (req.getProfileImageBase64() != null && !req.getProfileImageBase64().isBlank()) {
			ProfileImageUtil.validateMime(req.getProfileImageMimeType());
			user.setProfileImageMime(req.getProfileImageMimeType().trim());
			user.setProfileImageData(ProfileImageUtil.decodeBase64Image(req.getProfileImageBase64()));
		}
		ensureRequiredUserColumns(user);
		repository.save(user);
		UpdateUserResponse response = new UpdateUserResponse();
		response.setId(user.getId());
		return response;
	}

	private void assertDeleteAllowed(User target) {
		if (BusinessContextHolder.canBypassTenant()) {
			return;
		}
		if (target.isBusinessOwnerFlag()) {
			throw new ServiceException(ApiMessages.CANNOT_DELETE_BUSINESS_OWNER, HttpStatus.FORBIDDEN);
		}
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null && target.getUsername().equalsIgnoreCase(auth.getName())) {
			throw new ServiceException(ApiMessages.CANNOT_DELETE_SELF, HttpStatus.FORBIDDEN);
		}
	}

	private Long currentActorUserId() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
			return null;
		}
		return repository.findFirstByUsernameOrderByIdAsc(auth.getName()).map(User::getId).orElse(null);
	}

	/**
	 * When a portal admin acts inside a tenant (business context switcher), they may
	 * only view/edit the business owner — not team members (those stay in the business portal).
	 */
	private void assertPortalAdminMayManageBusinessUser(User target) {
		if (!BusinessContextHolder.isPortalAdminRoleLevel()) {
			return;
		}
		Long scopedBiz = BusinessContextHolder.currentBusinessId();
		if (scopedBiz == null) {
			return;
		}
		if (target.getBusinessId() == null || !scopedBiz.equals(target.getBusinessId())) {
			throw new ServiceException(ApiMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
		}
		if (!isBusinessOwnerAccount(target)) {
			throw new ServiceException(
					"Portal admins may only manage the business owner account. Team members are managed in the business portal.",
					HttpStatus.FORBIDDEN);
		}
	}

	private boolean isBusinessOwnerAccount(User user) {
		if (user.isBusinessOwnerFlag()) {
			return true;
		}
		String ut = user.getUserType();
		return UserType.BUSINESS_OWNER.name().equals(ut);
	}

	private void assignRolesForUpdate(User user, List<Long> roleIds) {
		userRoleRepository.deleteById_UserId(user.getId());
		boolean portalOwnerScope = BusinessContextHolder.isPortalAdminRoleLevel()
				&& BusinessContextHolder.currentBusinessId() != null
				&& isBusinessOwnerAccount(user);
		for (Long roleId : roleIds) {
			Role role = roleRepository.findById(roleId)
					.orElseThrow(() -> new ServiceException(ApiMessages.ROLE_NOT_FOUND, HttpStatus.BAD_REQUEST));
			if (role.isSystemRestricted()) {
				throw new ServiceException("This role cannot be granted via the user admin screen.",
						HttpStatus.FORBIDDEN);
			}
			assertRoleAssignableForCaller(role);
			if (portalOwnerScope && RoleKind.BUSINESS_TEAM.name().equals(role.getRoleKind())) {
				throw new ServiceException(
						"Team roles cannot be assigned to the business owner from here. Use a business-type template role (e.g. Travel).",
						HttpStatus.FORBIDDEN);
			}
			if (!BusinessContextHolder.canBypassTenant()
					&& RoleKind.BUSINESS_TEAM.name().equals(role.getRoleKind())) {
				Long actorId = currentActorUserId();
				rolePolicyService.assertCallerMayAssignTeamRole(role, BusinessContextHolder.requireBusinessId(),
						actorId);
			}
			UserRole userRole = new UserRole();
			UserRoleId userRoleId = new UserRoleId();
			userRoleId.setUserId(user.getId());
			userRoleId.setRoleId(roleId);
			userRole.setId(userRoleId);
			userRole.setRole(role);
			userRoleRepository.save(userRole);
		}
	}

	private void assertRoleAssignableForCaller(Role role) {
		String kind = role.getRoleKind();
		if (BusinessContextHolder.isPortalAdminRoleLevel()) {
			if (RoleKind.BUSINESS_TEAM.name().equals(kind)) {
				throw new ServiceException("Assign team roles from the business portal.", HttpStatus.FORBIDDEN);
			}
			return;
		}
		if (RoleKind.ADMIN_INTERNAL.name().equals(kind) || RoleKind.BUSINESS_TYPE_TEMPLATE.name().equals(kind)) {
			throw new ServiceException("Cannot assign internal or template roles to business users.",
					HttpStatus.FORBIDDEN);
		}
		if (role.getBusinessId() != null && !role.getBusinessId().equals(BusinessContextHolder.requireBusinessId())) {
			throw new ServiceException(ApiMessages.ROLE_NOT_FOUND, HttpStatus.FORBIDDEN);
		}
	}

	private void assertSelfTenantAccess(User user) {
		if (BusinessContextHolder.isPortalAdminRoleLevel()) {
			return;
		}
		Long ctx = BusinessContextHolder.currentBusinessId();
		Long ub = user.getBusinessId();
		if (ctx == null && ub == null) {
			return;
		}
		if (ctx != null && ctx.equals(ub)) {
			return;
		}
		throw new ServiceException(ApiMessages.MENU_PERMISSION_DENIED, HttpStatus.FORBIDDEN);
	}

	@Override
	public PageResponse<GetUserResponse> gets(GetsUsersRequest request) {
		Pageable pageable = PageRequest.of(request.getPageNumber(), request.getPageSize());
		Page<User> page;
		if (BusinessContextHolder.isPortalAdminRoleLevel()) {
			Long scopedBusinessId = BusinessContextHolder.currentBusinessId();
			if (scopedBusinessId != null) {
				page = repository.findBusinessOwnersByBusinessId(scopedBusinessId,
						UserType.BUSINESS_OWNER.name(), pageable);
			} else {
				page = repository.findAllByBusinessIdIsNullAndUserType(UserType.PORTAL_ADMIN.name(), pageable);
			}
		} else {
			Long businessId = BusinessContextHolder.currentBusinessId();
			if (businessId == null) {
				page = Page.empty(pageable);
			} else {
				Long actorId = currentActorUserId();
				List<Long> visibleUserIds = rolePolicyService.visibleUserIds(actorId, businessId);
				if (visibleUserIds == null) {
					page = repository.findAllByBusinessId(businessId, pageable);
				} else if (visibleUserIds.isEmpty()) {
					page = Page.empty(pageable);
				} else {
					page = repository.findAllByBusinessIdAndIdIn(businessId, visibleUserIds, pageable);
				}
			}
		}

		List<User> users = page.getContent();
		Map<Long, String> businessNames = resolveBusinessNames(users);
		List<GetUserResponse> items = users.stream()
				.map(u -> mapToResponse(u, false, businessNames))
				.collect(Collectors.toList());

		PageResponse<GetUserResponse> response = new PageResponse<>();
		response.setItems(items);
		response.setTotalCount(page.getTotalElements());
		response.setPageNumber(page.getNumber());
		response.setPageSize(page.getSize());
		response.setTotalPages(page.getTotalPages());
		return response;
	}

	private GetUserResponse mapToResponse(User user, boolean includeProfileImage, Map<Long, String> businessNames) {
		GetUserResponse response = new GetUserResponse();
		response.setId(user.getId());
		response.setUsername(user.getUsername());
		response.setFirstName(user.getFirstName());
		response.setLastName(user.getLastName());
		response.setEmail(user.getEmail());
		response.setMobileNumber(user.getMobileNumber());
		response.setStatus(user.getStatus());
		response.setCreatedAt(user.getCreatedAt());
		response.setDateOfBirth(user.getDateOfBirth());
		response.setBusinessId(user.getBusinessId());
		if (user.getBusinessId() != null && businessNames != null) {
			response.setBusinessName(businessNames.get(user.getBusinessId()));
		}
		response.setRoleIds(userRoleRepository.findById_UserId(user.getId()).stream().map(ur -> ur.getId().getRoleId())
				.collect(Collectors.toList()));
		response.setBusinessOwner(isBusinessOwnerAccount(user));
		response.setUserType(user.getUserType());
		if (includeProfileImage && user.getProfileImageData() != null && user.getProfileImageData().length > 0) {
			response.setProfileImageMimeType(user.getProfileImageMime());
			response.setProfileImageBase64(Base64.getEncoder().encodeToString(user.getProfileImageData()));
		}
		return response;
	}

	private Map<Long, String> resolveBusinessNames(List<User> users) {
		Set<Long> ids = users.stream()
				.map(User::getBusinessId)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
		if (ids.isEmpty()) {
			return Collections.emptyMap();
		}
		return businessRepository.findAllById(ids).stream()
				.collect(Collectors.toMap(Business::getId, Business::getBusinessName, (a, b) -> a));
	}

	private static void applyOptionalProfileOnCreate(User user, String mimeType, String base64) {
		if (base64 == null || base64.isBlank()) {
			return;
		}
		ProfileImageUtil.validateMime(mimeType);
		user.setProfileImageMime(mimeType.trim());
		user.setProfileImageData(ProfileImageUtil.decodeBase64Image(base64));
	}

	/**
	 * Auth {@code /auth/register} historically omitted {@code status} (and
	 * sometimes mobile). UM maps the same {@code um_user} row; Hibernate rejects
	 * saves when {@code status} is null.
	 */
	private static void ensureRequiredUserColumns(User user) {
		if (user.getStatus() == null || user.getStatus().isBlank()) {
			user.setStatus(DEFAULT_USER_STATUS);
		}
		if (user.getMobileNumber() == null || user.getMobileNumber().isBlank()) {
			user.setMobileNumber(DEFAULT_MOBILE_PLACEHOLDER);
		}
	}

	private static void applyProfileOnUpdate(User user, UpdateUserRequest request) {
		if (Boolean.TRUE.equals(request.getClearProfileImage())) {
			user.setProfileImageMime(null);
			user.setProfileImageData(null);
			return;
		}
		if (request.getProfileImageBase64() != null && !request.getProfileImageBase64().isBlank()) {
			ProfileImageUtil.validateMime(request.getProfileImageMimeType());
			user.setProfileImageMime(request.getProfileImageMimeType().trim());
			user.setProfileImageData(ProfileImageUtil.decodeBase64Image(request.getProfileImageBase64()));
		}
	}
}
