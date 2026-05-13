package com.broadcast.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.broadcast.api.model.MailTemplateEntity;

public interface MailTemplateRepository extends JpaRepository<MailTemplateEntity, Long> {

	/**
	 * Legacy un-scoped lookup. Use only when no business context is available
	 * (system jobs). Otherwise prefer {@link #findForBusinessOrGlobal}.
	 */
	Optional<MailTemplateEntity> findFirstByTemplateKeyIgnoreCaseAndActiveIndIgnoreCaseOrderByIdAsc(
			String templateKey, String activeInd);

	/**
	 * Tenant-aware lookup: prefer the tenant's own customised template, fall
	 * back to the global (NULL business id) template when none is present.
	 * Ordering: per-business templates first, then global, then id ASC.
	 */
	@Query("SELECT t FROM MailTemplateEntity t WHERE LOWER(t.templateKey) = LOWER(:key) "
			+ "AND LOWER(t.activeInd) = LOWER(:activeInd) "
			+ "AND (t.businessId = :businessId OR t.businessId IS NULL) "
			+ "ORDER BY CASE WHEN t.businessId IS NULL THEN 1 ELSE 0 END ASC, t.id ASC")
	java.util.List<MailTemplateEntity> findForBusinessOrGlobal(@Param("key") String templateKey,
			@Param("activeInd") String activeInd, @Param("businessId") Long businessId);
}
