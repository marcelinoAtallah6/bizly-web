package com.auth.api.repository.user;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.auth.api.model.user.UserRoleEntity;
import com.auth.api.model.user.UserRoleId;

public interface UserRoleRepository extends JpaRepository<UserRoleEntity, UserRoleId> {

	List<UserRoleEntity> findByIdUserId(Long userId);

}