package com.um.api.repository.workflow;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.um.api.model.workflow.WorkflowConfig;

public interface WorkflowConfigRepository extends JpaRepository<WorkflowConfig, Long> {

	Optional<WorkflowConfig> findFirstByBuiltInKeyAndBusinessIdIsNull(String builtInKey);

	List<WorkflowConfig> findByScreenNameIgnoreCaseAndActionNameIgnoreCase(String screenName, String actionName);

	Optional<WorkflowConfig> findFirstByBuiltInKey(String builtInKey);

	List<WorkflowConfig> findByBuiltInKeyStartingWith(String prefix);
}
