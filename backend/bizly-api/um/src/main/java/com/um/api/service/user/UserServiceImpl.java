package com.um.api.service.user;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

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
import com.um.exception.ServiceException;

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

		// Decrypt and encode password
		try {
			String decryptedPassword = passwordUtil.decryptPassword(request.getPassword());
			String encodedPassword = passwordEncoder.encode(decryptedPassword);
			user.setPassword(encodedPassword);
		} catch (Exception e) {
			throw new ServiceException(ApiMessages.PASSWORD_PROCESSING_FAILED, HttpStatus.BAD_REQUEST);
		}

		repository.save(user);

		// Save user roles
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

		AddUserResponse response = new AddUserResponse();
		response.setId(user.getId());
		return response;
	}

	@Override
	public UpdateUserResponse update(UpdateUserRequest request) {
		User user = repository.findById(request.getId())
				.orElseThrow(() -> new ServiceException(ApiMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND));

		user.setUsername(request.getUsername());
		user.setFirstName(request.getFirstName());
		user.setLastName(request.getLastName());
		user.setEmail(request.getEmail());
		user.setMobileNumber(request.getMobileNumber());
		user.setStatus(request.getStatus());

		repository.save(user);

		UpdateUserResponse response = new UpdateUserResponse();
		response.setId(user.getId());
		return response;
	}

	@Override
	public DeleteUserResponse delete(DeleteUserRequest request) {
		if (!repository.existsById(request.getId())) {
			throw new ServiceException(ApiMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
		}

		repository.deleteById(request.getId());

		DeleteUserResponse response = new DeleteUserResponse();
		response.setId(request.getId());
		return response;
	}

	@Override
	public GetUserResponse get(GetUserRequest request) {
		User user = repository.findById(request.getId())
				.orElseThrow(() -> new ServiceException(ApiMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND));

		return mapToResponse(user);
	}

	@Override
	public PageResponse<GetUserResponse> gets(GetsUsersRequest request) {
		Pageable pageable = PageRequest.of(request.getPageNumber(), request.getPageSize());
		Page<User> page = repository.findAll(pageable);

		List<GetUserResponse> items = page.getContent().stream().map(this::mapToResponse).collect(Collectors.toList());

		PageResponse<GetUserResponse> response = new PageResponse<>();
		response.setItems(items);
		response.setTotalCount(page.getTotalElements());
		response.setPageNumber(page.getNumber());
		response.setPageSize(page.getSize());
		response.setTotalPages(page.getTotalPages());
		return response;
	}

	private GetUserResponse mapToResponse(User user) {
		GetUserResponse response = new GetUserResponse();
		response.setId(user.getId());
		response.setUsername(user.getUsername());
		response.setFirstName(user.getFirstName());
		response.setLastName(user.getLastName());
		response.setEmail(user.getEmail());
		response.setMobileNumber(user.getMobileNumber());
		response.setStatus(user.getStatus());
		response.setCreatedAt(user.getCreatedAt());
		return response;
	}
}
