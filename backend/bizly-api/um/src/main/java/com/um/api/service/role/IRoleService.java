package com.um.api.service.role;

import com.um.api.dto.role.add.AddRoleRequest;
import com.um.api.dto.role.add.AddRoleResponse;
import com.um.api.dto.role.delete.DeleteRoleRequest;
import com.um.api.dto.role.delete.DeleteRoleResponse;
import com.um.api.dto.role.get.GetRoleRequest;
import com.um.api.dto.role.get.GetRoleResponse;
import com.um.api.dto.role.gets.GetsRolesRequest;
import com.um.api.dto.role.update.UpdateRoleRequest;
import com.um.api.dto.role.update.UpdateRoleResponse;
import com.um.common.PageResponse;

public interface IRoleService {

	AddRoleResponse add(AddRoleRequest request);

	UpdateRoleResponse update(UpdateRoleRequest request);

	DeleteRoleResponse delete(DeleteRoleRequest request);

	GetRoleResponse get(GetRoleRequest request);

	PageResponse<GetRoleResponse> gets(GetsRolesRequest request);
}