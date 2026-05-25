package com.um.api.repository.workflow.engine;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.um.api.model.workflow.engine.WorkflowDefinition;

public interface WorkflowDefinitionRepository extends JpaRepository<WorkflowDefinition, Long> {

	/**
	 * Published definition for action: tenant-specific wins over global (null business_id).
	 */
	@Query("SELECT d FROM WorkflowDefinition d WHERE UPPER(d.actionCode) = UPPER(:actionCode) AND d.status = 'PUBLISHED' "
			+ "AND ((:businessId IS NOT NULL AND d.businessId = :businessId) OR d.businessId IS NULL) "
			+ "ORDER BY CASE WHEN :businessId IS NOT NULL AND d.businessId = :businessId THEN 0 ELSE 1 END, "
			+ "d.versionNo DESC")
	List<WorkflowDefinition> findPublishedForAction(@Param("actionCode") String actionCode,
			@Param("businessId") Long businessId);

	default Optional<WorkflowDefinition> findBestPublished(String actionCode, Long businessId) {
		List<WorkflowDefinition> rows = findPublishedForAction(actionCode, businessId);
		return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
	}

	@Query("SELECT d FROM WorkflowDefinition d WHERE d.status = 'PUBLISHED' "
			+ "AND ((:businessId IS NOT NULL AND d.businessId = :businessId) OR d.businessId IS NULL) "
			+ "ORDER BY CASE WHEN :businessId IS NOT NULL AND d.businessId = :businessId THEN 0 ELSE 1 END, "
			+ "d.versionNo DESC")
	List<WorkflowDefinition> findAllPublishedForBusinessScope(@Param("businessId") Long businessId);

	Optional<WorkflowDefinition> findFirstByActionCodeIgnoreCaseAndVersionNoAndBusinessIdIsNull(String actionCode,
			Integer versionNo);

	List<WorkflowDefinition> findByActionCodeIgnoreCaseAndBusinessIdIsNull(String actionCode);
}
