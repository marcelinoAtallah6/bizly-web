package com.um.api.controller.role;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.um.api.dto.role.level.RoleLevelResponse;
import com.um.api.model.role.RoleLevel;
import com.um.api.repository.role.RoleLevelRepository;
import com.um.common.ApiResponse;
import com.um.exception.ServiceException;
import com.um.security.BusinessContextHolder;

@RestController
@RequestMapping("/role-level")
public class RoleLevelController {

	@Autowired
	private RoleLevelRepository roleLevelRepository;

	@PostMapping("/list")
	public ResponseEntity<ApiResponse<List<RoleLevelResponse>>> list() {
		if (!BusinessContextHolder.isPortalAdminRoleLevel()) {
			throw new ServiceException("Portal admin only", HttpStatus.FORBIDDEN);
		}
		List<RoleLevelResponse> items = roleLevelRepository.findAllByOrderBySortOrderAsc().stream()
				.map(this::map)
				.collect(Collectors.toList());
		return ResponseEntity.ok(ApiResponse.success(items, "OK"));
	}

	private RoleLevelResponse map(RoleLevel level) {
		RoleLevelResponse out = new RoleLevelResponse();
		out.setId(level.getId());
		out.setCode(level.getCode());
		out.setName(level.getName());
		out.setDescription(level.getDescription());
		return out;
	}
}
