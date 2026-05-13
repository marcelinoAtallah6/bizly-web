package com.broadcast.api.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.broadcast.api.model.BroadcastMessage;

/**
 * Tenant-scoped finders. The un-suffixed {@link #findAllByOrderByCreatedAtDesc(Pageable)}
 * remains only for ADMIN callers that explicitly opt out of tenant scoping via
 * {@code BusinessContextHolder.canBypassTenant()}.
 */
public interface BroadcastMessageRepository extends JpaRepository<BroadcastMessage, Long> {

	Page<BroadcastMessage> findAllByOrderByCreatedAtDesc(Pageable pageable);

	Page<BroadcastMessage> findAllByBusinessIdOrderByCreatedAtDesc(Long businessId, Pageable pageable);

	Optional<BroadcastMessage> findByIdAndBusinessId(Long id, Long businessId);
}
