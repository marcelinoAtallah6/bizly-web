package com.um.api.service.role;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

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
import com.um.api.repository.role.RoleRepository;
import com.um.common.ApiMessages;
import com.um.common.PageResponse;
import com.um.exception.ServiceException;

@Service
public class RoleServiceImpl implements IRoleService {

	@Autowired
	private RoleRepository repository;

	@Override
	public AddRoleResponse add(AddRoleRequest request) {
		Role role = new Role();
		role.setName(request.getName());
		role.setRoleType(request.getRoleType());
		role.setCreatedAt(LocalDateTime.now());

		repository.save(role);

		AddRoleResponse response = new AddRoleResponse();
		response.setId(role.getId());
		return response;
	}

	@Override
	public UpdateRoleResponse update(UpdateRoleRequest request) {
		Role role = repository.findById(request.getId())
				.orElseThrow(() -> new ServiceException(ApiMessages.ROLE_NOT_FOUND, HttpStatus.NOT_FOUND));

		role.setName(request.getName());
		role.setRoleType(request.getRoleType());

		repository.save(role);

		UpdateRoleResponse response = new UpdateRoleResponse();
		response.setId(role.getId());
		return response;
	}

	@Override
	public DeleteRoleResponse delete(DeleteRoleRequest request) {
		if (!repository.existsById(request.getId())) {
			throw new ServiceException(ApiMessages.ROLE_NOT_FOUND, HttpStatus.NOT_FOUND);
		}

		repository.deleteById(request.getId());

		DeleteRoleResponse response = new DeleteRoleResponse();
		response.setId(request.getId());
		return response;
	}

	@Override
	public GetRoleResponse get(GetRoleRequest request) {
		Role role = repository.findById(request.getId())
				.orElseThrow(() -> new ServiceException(ApiMessages.ROLE_NOT_FOUND, HttpStatus.NOT_FOUND));

		return buildResponse(role);
	}

	@Override
	public PageResponse<GetRoleResponse> gets(GetsRolesRequest request) {
		Pageable pageable = PageRequest.of(request.getPageNumber(), request.getPageSize());
		Page<Role> page = repository.findAll(pageable);

		List<GetRoleResponse> items = page.getContent().stream().map(this::buildResponse).collect(Collectors.toList());

		PageResponse<GetRoleResponse> response = new PageResponse<>();
		response.setItems(items);
		response.setTotalCount(page.getTotalElements());
		response.setPageNumber(page.getNumber());
		response.setPageSize(page.getSize());
		response.setTotalPages(page.getTotalPages());
		return response;
	}

	private GetRoleResponse buildResponse(Role role) {
		GetRoleResponse response = new GetRoleResponse();
		response.setId(role.getId());
		response.setName(role.getName());
		response.setRoleType(role.getRoleType());
		response.setCreatedAt(role.getCreatedAt());
		return response;
	}
}