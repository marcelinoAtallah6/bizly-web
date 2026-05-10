package com.um.api.repository.menu;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.um.api.model.menu.UmMenu;

@Repository
public interface UmMenuRepository extends JpaRepository<UmMenu, Long> {

	List<UmMenu> findByApplicationIdAndIsActiveOrderByIdAsc(Long applicationId, Integer isActive);

	Optional<UmMenu> findFirstByRouteIgnoreCaseOrderByIdAsc(String route);
}
