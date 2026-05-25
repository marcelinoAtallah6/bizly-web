package com.um.api.repository.role;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.um.api.model.role.UserRole;
import com.um.api.model.user.UserRoleId;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

	List<UserRole> findById_UserId(Long userId);

	@Query("SELECT DISTINCT ur.id.userId FROM UserRole ur WHERE ur.id.roleId IN :roleIds")
	List<Long> findDistinctUserIdsByRoleIdIn(@Param("roleIds") Collection<Long> roleIds);

	void deleteById_UserId(Long userId);

	@Modifying(clearAutomatically = true)
	@Query("DELETE FROM UserRole ur WHERE ur.id.roleId = :roleId")
	void deleteByRoleId(@Param("roleId") Long roleId);
}