package com.um.api.repository.workflow;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.um.api.model.workflow.WorkflowInstance;

public interface WorkflowInstanceRepository extends JpaRepository<WorkflowInstance, Long> {

	void deleteByWorkflowConfigId(Long workflowConfigId);

	List<WorkflowInstance> findByBusinessIdAndStatusOrderByCreatedAtDesc(Long businessId, String status);

	List<WorkflowInstance> findByBusinessIdOrderByCreatedAtDesc(Long businessId);

	List<WorkflowInstance> findByWorkflowConfigIdInOrderByCreatedAtDesc(Collection<Long> workflowConfigIds);

	List<WorkflowInstance> findByWorkflowConfigIdInAndStatusOrderByCreatedAtDesc(Collection<Long> workflowConfigIds,
			String status);

	boolean existsByBusinessIdAndWorkflowConfigIdAndStatusIgnoreCase(Long businessId, Long workflowConfigId,
			String status);
}
