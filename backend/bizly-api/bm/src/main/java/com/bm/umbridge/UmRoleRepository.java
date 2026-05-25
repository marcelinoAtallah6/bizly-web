package com.bm.umbridge;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UmRoleRepository extends JpaRepository<UmRoleEntity, Long> {

	Optional<UmRoleEntity> findFirstByNameIgnoreCaseOrderByIdAsc(String name);
}
