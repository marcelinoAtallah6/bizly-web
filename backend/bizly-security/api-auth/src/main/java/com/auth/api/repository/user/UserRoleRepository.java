package com.auth.api.repository.user;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.auth.api.model.user.UserRoleEntity;
import com.auth.api.model.user.UserRoleId;

public interface UserRoleRepository extends JpaRepository<UserRoleEntity, UserRoleId> {

	List<UserRoleEntity> findByIdUserId(Long userId);

	/**
	 * Role names for assignments that still resolve to a row in {@code um_role}. Uses an inner join so
	 * orphaned {@code um_user_role.role_id} values (deleted role) do not trigger lazy-load errors.
	 */
	@Query("SELECT r.name FROM UserRoleEntity ur INNER JOIN ur.role r WHERE ur.id.userId = :userId ORDER BY r.name ASC")
	List<String> findRoleNamesByUserId(@Param("userId") Long userId);

}