package com.auth.api.repository.workflow;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.auth.api.model.workflow.WorkflowDefinitionEntity;

public interface WorkflowDefinitionRepository extends JpaRepository<WorkflowDefinitionEntity, Long> {

	@Query("SELECT d FROM WorkflowDefinitionEntity d WHERE UPPER(d.actionCode) = UPPER(:actionCode) "
			+ "AND UPPER(d.status) = 'PUBLISHED' AND d.businessId IS NULL ORDER BY d.versionNo DESC")
	List<WorkflowDefinitionEntity> findPublishedGlobal(@Param("actionCode") String actionCode);

	Optional<WorkflowDefinitionEntity> findFirstByActionCodeIgnoreCaseAndVersionNoAndBusinessIdIsNull(String actionCode,
			Integer versionNo);

	List<WorkflowDefinitionEntity> findByActionCodeIgnoreCaseAndBusinessIdIsNull(String actionCode);
}
