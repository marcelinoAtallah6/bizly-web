package com.bm.umbridge;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UmMenuRepository extends JpaRepository<UmMenuEntity, Long> {

	Optional<UmMenuEntity> findFirstByRouteIgnoreCaseOrderByIdAsc(String route);
}
