package com.travel.api.service.notif;

import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.exception.SQLGrammarException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.travel.api.model.NotifInbox;
import com.travel.api.repository.NotifInboxRepository;
import com.travel.security.BusinessContextHolder;

@Service
public class NotifInboxServiceImpl implements INotifInboxService {

	private static final Logger log = LogManager.getLogger(NotifInboxServiceImpl.class);

	@Autowired
	private NotifInboxRepository repository;

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
			return false;
		}
		if (trimToNull(request.getTitle()) == null || trimToNull(request.getCategory()) == null) {
			return false;
		}

		try {
			Long businessId = request.getBusinessId();
			if (businessId == null) {
				businessId = BusinessContextHolder.currentBusinessId();
			}
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
			repository.flush();
			return true;
		} catch (RuntimeException ex) {
			if (isMissingTable(ex)) {
				log.warn("[NOTIF_INBOX][PUBLISH][FAIL][TABLE_MISSING] user={} title={} — ensure UM.NOTIF_INBOX exists.",
						username, request.getTitle());
			} else {
				log.warn("[NOTIF_INBOX][PUBLISH][FAIL] user={} category={} title={} err={}", username,
						request.getCategory(), request.getTitle(), ex.getMessage());
			}
			return false;
		}
	}

	private static boolean isMissingTable(Throwable ex) {
		Throwable cur = ex;
		while (cur != null) {
			if (cur instanceof SQLGrammarException) {
				return true;
			}
			String m = cur.getMessage();
			if (m != null) {
				String lc = m.toLowerCase();
				if (lc.contains("ora-00942") || lc.contains("does not exist")) {
					return true;
				}
			}
			cur = cur.getCause();
		}
		return false;
	}

	private static String trimToNull(String s) {
		if (s == null) {
			return null;
		}
		String t = s.trim();
		return t.isEmpty() ? null : t;
	}
}
