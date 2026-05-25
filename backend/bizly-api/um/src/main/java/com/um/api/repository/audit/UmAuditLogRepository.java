package com.um.api.repository.audit;

import java.util.Collection;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.um.api.model.audit.UmAuditLog;

public interface UmAuditLogRepository extends JpaRepository<UmAuditLog, Long> {

	Page<UmAuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

	Page<UmAuditLog> findByUsernameContainingIgnoreCaseOrderByCreatedAtDesc(String username, Pageable pageable);

	Page<UmAuditLog> findByBusinessIdOrderByCreatedAtDesc(Long businessId, Pageable pageable);

	Page<UmAuditLog> findByBusinessIdAndUsernameContainingIgnoreCaseOrderByCreatedAtDesc(Long businessId,
			String username, Pageable pageable);

	Page<UmAuditLog> findByBusinessIdAndUsernameInOrderByCreatedAtDesc(Long businessId,
			Collection<String> usernames, Pageable pageable);

	Page<UmAuditLog> findByBusinessIdAndUsernameInAndUsernameContainingIgnoreCaseOrderByCreatedAtDesc(Long businessId,
			Collection<String> usernames, String username, Pageable pageable);
}
