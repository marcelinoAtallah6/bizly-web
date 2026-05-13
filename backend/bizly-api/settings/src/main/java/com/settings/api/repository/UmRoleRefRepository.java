package com.settings.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.settings.api.model.UmRoleRef;

public interface UmRoleRefRepository extends JpaRepository<UmRoleRef, Long> {

	@Query("SELECT r FROM UmRoleRef r WHERE UPPER(TRIM(r.name)) = UPPER(TRIM(:name)) AND r.roleType IS NOT NULL")
	Optional<UmRoleRef> findFirstByNameIgnoreCaseWithType(@Param("name") String name);

	@Query("SELECT r FROM UmRoleRef r WHERE r.roleType IN :roleTypes")
	List<UmRoleRef> findByRoleTypeIn(@Param("roleTypes") List<Integer> roleTypes);
}
