package com.um.api.controller.role;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.um.api.dto.role.delete.DeleteRoleRequest;
import com.um.api.dto.role.team.CreateTeamRoleRequest;
import com.um.api.dto.role.team.ParentRoleOptionResponse;
import com.um.api.dto.role.team.TeamRoleResponse;
import com.um.api.service.role.TeamRoleService;
import com.um.common.ApiResponse;

@RestController
@RequestMapping("/business/team-role")
public class TeamRoleController {

	@Autowired
	private TeamRoleService teamRoleService;

	@PostMapping("/list")
	public ResponseEntity<ApiResponse<List<TeamRoleResponse>>> list() {
		return ResponseEntity.ok(ApiResponse.success(teamRoleService.listForCurrentBusiness(), "OK"));
	}

	@PostMapping("/list-assignable")
	public ResponseEntity<ApiResponse<List<TeamRoleResponse>>> listAssignable() {
		return ResponseEntity.ok(ApiResponse.success(teamRoleService.listAssignableForUserManagement(), "OK"));
	}

	@PostMapping("/parent-options")
	public ResponseEntity<ApiResponse<List<ParentRoleOptionResponse>>> parentOptions() {
		return ResponseEntity.ok(ApiResponse.success(teamRoleService.listParentRoleOptions(), "OK"));
	}

	@PostMapping("/add")
	public ResponseEntity<ApiResponse<TeamRoleResponse>> add(@Valid @RequestBody CreateTeamRoleRequest request) {
		return ResponseEntity.ok(ApiResponse.success(teamRoleService.create(request), "Team role created"));
	}

	@PostMapping("/delete")
	public ResponseEntity<ApiResponse<String>> delete(@Valid @RequestBody DeleteRoleRequest request) {
		teamRoleService.delete(request.getId());
		return ResponseEntity.ok(ApiResponse.success("OK", "Team role deleted"));
	}
}
