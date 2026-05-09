package com.um.api.repository.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.um.api.model.audit.UmAuditLog;

public interface UmAuditLogRepository extends JpaRepository<UmAuditLog, Long> {

	Page<UmAuditLog> findByUsernameContainingIgnoreCaseOrderByCreatedAtDesc(String username, Pageable pageable);

	Page<UmAuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
