package com.um.api.controller.admin;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.um.api.domain.RoleKind;
import com.um.api.dto.role.get.GetRoleResponse;
import com.um.api.model.role.Role;
import com.um.api.repository.role.RoleRepository;
import com.um.common.ApiResponse;
import com.um.exception.ServiceException;
import com.um.security.BusinessContextHolder;

@RestController
@RequestMapping("/admin/business-type-template")
public class BusinessTypeTemplateController {

	@Autowired private RoleRepository roleRepository;

	@PostMapping("/list")
	public ResponseEntity<ApiResponse<List<GetRoleResponse>>> list() {
		if (!BusinessContextHolder.canBypassTenant()) {
			throw new ServiceException("Portal admin only", HttpStatus.FORBIDDEN);
		}
		List<GetRoleResponse> items = roleRepository
				.findByRoleKindAndBusinessIdIsNull(RoleKind.BUSINESS_TYPE_TEMPLATE.name())
				.stream()
				.map(this::map)
				.collect(Collectors.toList());
		return ResponseEntity.ok(ApiResponse.success(items, "OK"));
	}

	private GetRoleResponse map(Role r) {
		GetRoleResponse out = new GetRoleResponse();
		out.setId(r.getId());
		out.setName(r.getName());
		out.setRoleType(r.getRoleType());
		return out;
	}
}
