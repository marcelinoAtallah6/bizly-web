package com.um.api.repository.menu;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.um.api.model.menu.UmMenu;

@Repository
public interface UmMenuRepository extends JpaRepository<UmMenu, Long> {

	List<UmMenu> findByApplicationIdAndIsActiveOrderByIdAsc(Long applicationId, Integer isActive);

	List<UmMenu> findByApplicationIdOrderBySortOrderAscNameAsc(Long applicationId);

	Optional<UmMenu> findFirstByRouteIgnoreCaseOrderByIdAsc(String route);

	Optional<UmMenu> findFirstByRouteIgnoreCaseAndIdNot(String route, Long id);

	@Query("select coalesce(max(m.sortOrder), 0) from UmMenu m where m.application.id = :applicationId")
	Integer findMaxSortOrderByApplicationId(@Param("applicationId") Long applicationId);

	@Query("select m from UmMenu m where m.isActive = true and m.route is not null and trim(m.route) <> '' order by m.sortOrder asc, m.name asc")
	List<UmMenu> findCatalogMenusWithRoute();
}
