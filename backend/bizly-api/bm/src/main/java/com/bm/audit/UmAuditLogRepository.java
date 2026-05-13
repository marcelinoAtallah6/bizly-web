package com.bm.audit;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UmAuditLogRepository extends JpaRepository<UmAuditLog, Long> {
}
