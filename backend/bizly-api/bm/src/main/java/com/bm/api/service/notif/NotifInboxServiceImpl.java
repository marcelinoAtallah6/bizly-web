package com.bm.api.service.notif;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.exception.SQLGrammarException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.bm.api.dto.notif.NotifInboxItemResponse;
import com.bm.api.dto.notif.NotifInboxListResponse;
import com.bm.api.model.NotifInbox;
import com.bm.api.repository.NotifInboxRepository;
import com.bm.security.BusinessContextHolder;

@Service
public class NotifInboxServiceImpl implements INotifInboxService {

	private static final Logger log = LogManager.getLogger(NotifInboxServiceImpl.class);

	private static final int DEFAULT_PAGE_SIZE = 15;
	private static final int MAX_PAGE_SIZE = 100;

	@Autowired
	private NotifInboxRepository repository;

	/**
	 * REQUIRES_NEW guarantees the caller's transaction (e.g. appointment add)
	 * survives a DB error on the inbox insert. The fresh transaction here is
	 * the only one Spring is allowed to mark rollback-only.
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean publish(NotifPublishRequest request) {
		return writeRow(request, false);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean publishOnce(NotifPublishRequest request) {
		return writeRow(request, true);
	}

	private boolean writeRow(NotifPublishRequest request, boolean dedup) {
		if (request == null) {
			return false;
		}
		String username = trimToNull(request.getUsername());
		if (username == null) {
			// Producers may pass a null/blank username (e.g. system-created records); skip.
			return false;
		}
		if (trimToNull(request.getTitle()) == null || trimToNull(request.getCategory()) == null) {
			return false;
		}

		try {
			/*
			 * Tenant scope: when a request originates from a user request (BusinessContextHolder is set),
			 * stamp the current business id. Scheduler / system producers run without a context — we
			 * leave businessId NULL so the row reads as a system-wide notification visible to the user.
			 */
			Long businessId = BusinessContextHolder.currentBusinessId();
			if (dedup) {
				String rt = trimToNull(request.getResourceType());
				String rid = trimToNull(request.getResourceId());
				if (rt == null || rid == null) {
					log.warn("[NOTIF_INBOX][PUBLISH_ONCE][NO_KEY] user={} title={} (missing resourceType/Id)",
							username, request.getTitle());
				} else {
					boolean alreadyExists = businessId != null
							? repository.existsForUserAndBusiness(username, rt, rid, businessId)
							: repository.existsByUsernameAndResourceTypeAndResourceId(username, rt, rid);
					if (alreadyExists) {
						return false;
					}
				}
			}

			NotifInbox row = new NotifInbox();
			row.setUsername(username);
			row.setBusinessId(businessId);
			row.setCategory(request.getCategory());
			row.setSeverity((request.getSeverity() != null ? request.getSeverity() : NotifSeverity.INFO).name());
			row.setTitle(request.getTitle());
			row.setBody(request.getBody());
			row.setLinkRoute(request.getLinkRoute());
			row.setResourceType(request.getResourceType());
			row.setResourceId(request.getResourceId());
			row.setCreatedAt(LocalDateTime.now());
			repository.save(row);
			// Force the INSERT now so any DB error becomes a catchable exception
			// here instead of bubbling out at commit time.
			repository.flush();
			return true;
		} catch (RuntimeException ex) {
			// Notifications must never break the producing transaction. The
			// REQUIRES_NEW boundary ensures the caller is unaffected.
			if (isMissingTable(ex)) {
				log.warn("[NOTIF_INBOX][PUBLISH][FAIL][TABLE_MISSING] user={} title={} — run "
						+ "backend/bizly-api/bm/src/main/resources/db/patch-notif-inbox-oracle.sql "
						+ "to create UM.NOTIF_INBOX.", username, request.getTitle());
			} else {
				log.warn("[NOTIF_INBOX][PUBLISH][FAIL] user={} category={} title={} err={}", username,
						request.getCategory(), request.getTitle(), ex.getMessage());
			}
			return false;
		}
	}

	/** Detect the common "table does not exist" case so the log is actionable. */
	private static boolean isMissingTable(Throwable ex) {
		Throwable cur = ex;
		while (cur != null) {
			if (cur instanceof SQLGrammarException) {
				return true;
			}
			String m = cur.getMessage();
			if (m != null) {
				String lc = m.toLowerCase();
				if (lc.contains("ora-00942") // Oracle: table or view does not exist
						|| lc.contains("does not exist")) {
					return true;
				}
			}
			cur = cur.getCause();
		}
		return false;
	}

	@Override
	@Transactional(readOnly = true)
	public NotifInboxListResponse recent(String username, Integer pageNumber, Integer pageSize) {
		NotifInboxListResponse res = new NotifInboxListResponse();

		int safePage = pageNumber == null || pageNumber < 0 ? 0 : pageNumber;
		int safeSize = pageSize == null || pageSize <= 0 ? DEFAULT_PAGE_SIZE
				: Math.min(pageSize, MAX_PAGE_SIZE);

		res.setPageNumber(safePage);
		res.setPageSize(safeSize);
		res.setItems(Collections.emptyList());

		String u = trimToNull(username);
		if (u == null) {
			return res;
		}

		/*
		 * Tenant scope: when a user request brings a business id we filter to the user's own-business
		 * AND globally-broadcast rows (business_id IS NULL). Admin callers without a business context
		 * fall back to the legacy un-scoped query so they still see the full inbox.
		 */
		Long businessId = BusinessContextHolder.currentBusinessId();
		long total = businessId != null
				? repository.countInboxForUserAndBusiness(u, businessId)
				: repository.countByUsername(u);
		res.setTotalCount(total);
		res.setUnreadCount(businessId != null
				? repository.countUnreadForUserAndBusiness(u, businessId)
				: repository.countByUsernameAndReadAtIsNull(u));

		long fromIndex = (long) safePage * safeSize;
		if (fromIndex >= total) {
			// Past the end — keep items empty, hasMore false.
			return res;
		}

		Pageable pageable = PageRequest.of(safePage, safeSize);
		List<NotifInbox> rows = businessId != null
				? repository.findInboxForUserAndBusiness(u, businessId, pageable)
				: repository.findByUsernameOrderByCreatedAtDesc(u, pageable);
		List<NotifInboxItemResponse> items = new ArrayList<>(rows.size());
		for (NotifInbox r : rows) {
			items.add(toResponse(r));
		}
		res.setItems(items);
		res.setHasMore((fromIndex + items.size()) < total);
		return res;
	}

	@Override
	@Transactional(readOnly = true)
	public long unreadCount(String username) {
		String u = trimToNull(username);
		if (u == null) {
			return 0L;
		}
		Long businessId = BusinessContextHolder.currentBusinessId();
		return businessId != null
				? repository.countUnreadForUserAndBusiness(u, businessId)
				: repository.countByUsernameAndReadAtIsNull(u);
	}

	@Override
	@Transactional
	public void markRead(String username, Long id) {
		String u = trimToNull(username);
		if (u == null || id == null) {
			return;
		}
		Long businessId = BusinessContextHolder.currentBusinessId();
		if (businessId != null) {
			repository.markReadForBusiness(id, u, businessId, LocalDateTime.now());
		} else {
			repository.markRead(id, u, LocalDateTime.now());
		}
	}

	@Override
	@Transactional
	public void markAllRead(String username) {
		String u = trimToNull(username);
		if (u == null) {
			return;
		}
		Long businessId = BusinessContextHolder.currentBusinessId();
		if (businessId != null) {
			repository.markAllReadForBusiness(u, businessId, LocalDateTime.now());
		} else {
			repository.markAllRead(u, LocalDateTime.now());
		}
	}

	private static NotifInboxItemResponse toResponse(NotifInbox r) {
		NotifInboxItemResponse dto = new NotifInboxItemResponse();
		dto.setId(r.getId());
		dto.setCategory(r.getCategory());
		dto.setSeverity(r.getSeverity());
		dto.setTitle(r.getTitle());
		dto.setBody(r.getBody());
		dto.setLinkRoute(r.getLinkRoute());
		dto.setResourceType(r.getResourceType());
		dto.setResourceId(r.getResourceId());
		dto.setCreatedAt(r.getCreatedAt());
		dto.setReadAt(r.getReadAt());
		dto.setUnread(r.getReadAt() == null);
		return dto;
	}

	private static String trimToNull(String s) {
		if (s == null) {
			return null;
		}
		String t = s.trim();
		return t.isEmpty() ? null : t;
	}
}
