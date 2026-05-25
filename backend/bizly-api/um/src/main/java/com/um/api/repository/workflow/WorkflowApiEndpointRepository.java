package com.um.api.repository.workflow;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.um.api.model.workflow.WorkflowApiEndpoint;

public interface WorkflowApiEndpointRepository extends JpaRepository<WorkflowApiEndpoint, Long> {

	List<WorkflowApiEndpoint> findByActiveTrueAndPathAntPatternIsNotNullOrderByPriorityDesc();

	Optional<WorkflowApiEndpoint> findByMenuIdAndActionCodeIgnoreCase(Long menuId, String actionCode);

	@Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM WorkflowApiEndpoint e WHERE e.active = true "
			+ "AND e.pathAntPattern IS NOT NULL AND UPPER(e.screenRoute) = UPPER(:screen) AND UPPER(e.actionCode) = UPPER(:action)")
	boolean existsActiveMutatingForScreenAndAction(@Param("screen") String screen, @Param("action") String action);

	@Query("SELECT e FROM WorkflowApiEndpoint e WHERE e.active = true AND e.pathAntPattern IS NOT NULL "
			+ "AND UPPER(e.actionCode) IN ('ADD','EDIT','DELETE') ORDER BY e.priority DESC, e.screenRoute ASC, e.actionCode ASC")
	List<WorkflowApiEndpoint> findMutatingCatalogEndpoints();

	/** Gateway deferral / replay: HTTP triggers only (excludes DOMAIN event rows). */
	@Query("SELECT e FROM WorkflowApiEndpoint e WHERE e.active = true AND e.pathAntPattern IS NOT NULL "
			+ "AND (e.triggerSource IS NULL OR UPPER(e.triggerSource) = 'HTTP') "
			+ "ORDER BY e.priority DESC, e.screenRoute ASC, e.actionCode ASC")
	List<WorkflowApiEndpoint> findActiveHttpEndpointsForGateway();

	List<WorkflowApiEndpoint> findByActiveTrueOrderByDisplayNameAscIdAsc();

	Optional<WorkflowApiEndpoint> findFirstByEngineActionCodeIgnoreCase(String engineActionCode);
}
