package com.auth.api.repository.workflow;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.auth.api.model.workflow.WorkflowInstanceEntity;

public interface WorkflowInstanceRepository extends JpaRepository<WorkflowInstanceEntity, Long> {

	boolean existsByBusinessIdAndWorkflowConfigIdAndStatusIgnoreCase(Long businessId, Long workflowConfigId,
			String status);

	List<WorkflowInstanceEntity> findByBusinessIdAndWorkflowConfigIdAndStatusIgnoreCase(Long businessId,
			Long workflowConfigId, String status);
}
