package com.auth.api.repository.workflow;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.auth.api.model.workflow.WorkflowConfigEntity;

public interface WorkflowConfigRepository extends JpaRepository<WorkflowConfigEntity, Long> {

	Optional<WorkflowConfigEntity> findFirstByBuiltInKeyAndBusinessIdIsNull(String builtInKey);

	List<WorkflowConfigEntity> findByBuiltInKeyStartingWith(String prefix);

	List<WorkflowConfigEntity> findByScreenNameIgnoreCaseAndActionNameIgnoreCase(String screenName, String actionName);
}
