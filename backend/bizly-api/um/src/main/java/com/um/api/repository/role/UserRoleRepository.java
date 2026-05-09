package com.um.api.repository.role;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.um.api.model.role.UserRole;
import com.um.api.model.user.UserRoleId;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

	List<UserRole> findById_UserId(Long userId);

	void deleteById_UserId(Long userId);
}