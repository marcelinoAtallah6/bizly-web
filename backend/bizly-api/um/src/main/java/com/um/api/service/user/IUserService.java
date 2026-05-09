package com.um.api.service.user;

import com.um.api.dto.user.add.AddUserRequest;
import com.um.api.dto.user.add.AddUserResponse;
import com.um.api.dto.user.delete.DeleteUserRequest;
import com.um.api.dto.user.delete.DeleteUserResponse;
import com.um.api.dto.user.get.GetUserRequest;
import com.um.api.dto.user.get.GetUserResponse;
import com.um.api.dto.user.gets.GetsUsersRequest;
import com.um.api.dto.user.update.UpdateUserRequest;
import com.um.api.dto.user.update.UpdateUserResponse;
import com.um.common.PageResponse;

public interface IUserService {

	AddUserResponse add(AddUserRequest request);

	UpdateUserResponse update(UpdateUserRequest request);

	DeleteUserResponse delete(DeleteUserRequest request);

	GetUserResponse get(GetUserRequest request);

	PageResponse<GetUserResponse> gets(GetsUsersRequest request);
}
