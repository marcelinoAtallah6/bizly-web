package com.um.api.service.user;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
import com.um.api.dto.user.update.UpdateUserRequest;
import com.um.api.dto.user.update.UpdateUserResponse;
import com.um.api.model.role.Role;
import com.um.api.model.role.UserRole;
import com.um.api.model.user.User;
import com.um.api.model.user.UserRoleId;
import com.um.api.repository.role.RoleRepository;
import com.um.api.repository.role.UserRoleRepository;
import com.um.api.repository.user.UserRepository;
import com.um.common.ApiMessages;
import com.um.common.PageResponse;
import com.um.common.PasswordUtil;
import com.um.common.ProfileImageUtil;
import com.um.exception.ServiceException;
import com.um.security.BusinessContextHolder;

@Service
public class UserServiceImpl implements IUserService {

	@Autowired
	private UserRepository repository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private UserRoleRepository userRoleRepository;

	@Autowired
	private PasswordUtil passwordUtil;

	private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	private static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$";

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
		user.setNotifWelcomeFlag(0);
		user.setNotifWelcomeStatus(0);
		/*
		 * Tenant scope: business admins can only create users inside their own tenant. SUPER_ADMIN
		 * (role-level=ADMIN) provisions users across tenants — they may pre-bind a business id via
		 * the X-Business-Override header (already substituted by BusinessContextHolder) OR leave it
		 * NULL for a system-wide account. Defence-in-depth: we never trust a business id sent in
		 * the request body.
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
		 * Save user roles. Even though this endpoint is admin-gated (RequireMenuPermission on the
		 * controller), we still refuse to grant {@code is_system_restricted = 1} roles from here as
		 * a defence-in-depth measure. If a malicious or compromised admin client tries to seed a
		 * SUPER_ADMIN role via /user/add, the assignment is rejected with 403 and logged.
		 */
		for (Long roleId : request.getRoleIds()) {
			Role role = roleRepository.findById(roleId)
					.orElseThrow(() -> new ServiceException(ApiMessages.ROLE_NOT_FOUND, HttpStatus.BAD_REQUEST));
			if (role.isSystemRestricted()) {
				throw new ServiceException("This role cannot be granted via the user admin screen.",
						HttpStatus.FORBIDDEN);
			}
			UserRole userRole = new UserRole();
			UserRoleId userRoleId = new UserRoleId();
			userRoleId.setUserId(user.getId());
			userRoleId.setRoleId(roleId);
			userRole.setId(userRoleId);
			userRole.setRole(role);
			userRoleRepository.save(userRole);
		}

		AddUserResponse response = new AddUserResponse();
		response.setId(user.getId());
		return response;
	}

	@Override
	@Transactional
	public UpdateUserResponse update(UpdateUserRequest request) {
		User user = loadUserForCaller(request.getId());

		user.setUsername(request.getUsername());
		user.setFirstName(request.getFirstName());
		user.setLastName(request.getLastName());
		user.setEmail(request.getEmail());
		user.setMobileNumber(request.getMobileNumber());
		user.setStatus(request.getStatus());

		applyProfileOnUpdate(user, request);

		repository.save(user);

		userRoleRepository.deleteById_UserId(request.getId());
		for (Long roleId : request.getRoleIds()) {
			Role role = roleRepository.findById(roleId)
					.orElseThrow(() -> new ServiceException(ApiMessages.ROLE_NOT_FOUND, HttpStatus.BAD_REQUEST));
			UserRole userRole = new UserRole();
			UserRoleId userRoleId = new UserRoleId();
			userRoleId.setUserId(user.getId());
			userRoleId.setRoleId(roleId);
			userRole.setId(userRoleId);
			userRole.setRole(role);
			userRoleRepository.save(userRole);
		}

		UpdateUserResponse response = new UpdateUserResponse();
		response.setId(user.getId());
		return response;
	}

	@Override
	public DeleteUserResponse delete(DeleteUserRequest request) {
		User user = loadUserForCaller(request.getId());
		repository.delete(user);

		DeleteUserResponse response = new DeleteUserResponse();
		response.setId(request.getId());
		return response;
	}

	@Override
	public GetUserResponse get(GetUserRequest request) {
		User user = loadUserForCaller(request.getId());
		return mapToResponse(user, true);
	}

	/**
	 * Tenant-scoped lookup. Business admins can only see users in their own tenant; SUPER_ADMIN
	 * (role-level=ADMIN) can address any user — including SUPER_ADMIN accounts that have a NULL
	 * business id — across tenants.
	 */
	private User loadUserForCaller(Long id) {
		if (BusinessContextHolder.canBypassTenant()) {
			return repository.findById(id)
					.orElseThrow(() -> new ServiceException(ApiMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND));
		}
		Long businessId = BusinessContextHolder.currentBusinessId();
		if (businessId == null) {
			throw new ServiceException(ApiMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
		}
		return repository.findByIdAndBusinessId(id, businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND));
	}

	@Override
	public PageResponse<GetUserResponse> gets(GetsUsersRequest request) {
		Pageable pageable = PageRequest.of(request.getPageNumber(), request.getPageSize());
		// Tenant scope: business admins only see their tenant's users; SUPER_ADMIN sees everyone.
		Page<User> page;
		if (BusinessContextHolder.canBypassTenant()) {
			page = repository.findAll(pageable);
		} else {
			Long businessId = BusinessContextHolder.currentBusinessId();
			page = businessId == null ? Page.empty(pageable) : repository.findAllByBusinessId(businessId, pageable);
		}

		List<GetUserResponse> items = page.getContent().stream().map(u -> mapToResponse(u, false))
				.collect(Collectors.toList());

		PageResponse<GetUserResponse> response = new PageResponse<>();
		response.setItems(items);
		response.setTotalCount(page.getTotalElements());
		response.setPageNumber(page.getNumber());
		response.setPageSize(page.getSize());
		response.setTotalPages(page.getTotalPages());
		return response;
	}

	private GetUserResponse mapToResponse(User user, boolean includeProfileImage) {
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
		response.setRoleIds(userRoleRepository.findById_UserId(user.getId()).stream()
				.map(ur -> ur.getId().getRoleId()).collect(Collectors.toList()));
		if (includeProfileImage && user.getProfileImageData() != null && user.getProfileImageData().length > 0) {
			response.setProfileImageMimeType(user.getProfileImageMime());
			response.setProfileImageBase64(Base64.getEncoder().encodeToString(user.getProfileImageData()));
		}
		return response;
	}

	private static void applyOptionalProfileOnCreate(User user, String mimeType, String base64) {
		if (base64 == null || base64.isBlank()) {
			return;
		}
		ProfileImageUtil.validateMime(mimeType);
		user.setProfileImageMime(mimeType.trim());
		user.setProfileImageData(ProfileImageUtil.decodeBase64Image(base64));
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
