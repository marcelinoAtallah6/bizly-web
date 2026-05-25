package com.um.api.repository.workflow.engine;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.um.api.model.workflow.engine.WorkflowStep;

public interface WorkflowStepRepository extends JpaRepository<WorkflowStep, Long> {

	List<WorkflowStep> findByDefinitionIdAndIsActiveOrderByStepOrderAsc(Long definitionId, Integer isActive);

	List<WorkflowStep> findByDefinitionIdOrderByStepOrderAsc(Long definitionId);

	void deleteByDefinitionId(Long definitionId);
}
