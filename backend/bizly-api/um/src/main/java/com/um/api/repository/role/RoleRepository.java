package com.um.api.repository.role;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.um.api.model.role.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

	Optional<Role> findFirstByNameIgnoreCaseOrderByIdAsc(String name);

	List<Role> findByRoleKindAndBusinessIdIsNull(String roleKind);

	List<Role> findByBusinessId(Long businessId);

	List<Role> findByBusinessIdAndRoleKind(Long businessId, String roleKind);

	List<Role> findByBusinessIdIsNullAndRoleKindIn(List<String> roleKinds);

	/**
	 * The portal root role (e.g. "Super Admin"): global, system-restricted, no parent.
	 * Identified by DB flags — never by a hardcoded role name.
	 */
	Optional<Role> findFirstByRoleKindAndBusinessIdIsNullAndParentRoleIdIsNullAndIsSystemRestricted(
			String roleKind, Integer isSystemRestricted);

	@Query("SELECT MAX(r.roleType) FROM Role r")
	Optional<Integer> findMaxRoleType();
}
