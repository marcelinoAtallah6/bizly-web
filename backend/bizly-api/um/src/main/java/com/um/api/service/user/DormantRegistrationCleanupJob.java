package com.um.api.service.user;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.um.api.model.user.User;
import com.um.api.repository.business.BusinessRepository;
import com.um.api.repository.subscription.UserSubscriptionRepository;
import com.um.api.repository.user.UserRepository;

@Component
public class DormantRegistrationCleanupJob {

	private static final Logger log = LoggerFactory.getLogger(DormantRegistrationCleanupJob.class);

	@Autowired private UserRepository userRepository;
	@Autowired private BusinessRepository businessRepository;
	@Autowired private UserSubscriptionRepository subscriptionRepository;

	@Scheduled(cron = "0 15 3 * * *")
	@Transactional
	public void purgeExpiredDormantPublicRegistrations() {
		LocalDateTime now = LocalDateTime.now();
		List<User> expired = userRepository.findExpiredDormantPublicUsers(now);
		for (User u : expired) {
			if (subscriptionRepository.existsByUserIdAndStatus(u.getId(), "PAID")) {
				continue;
			}
			Long businessId = u.getBusinessId();
			String userStatus = u.getStatus();
			userRepository.delete(u);
			if (businessId != null) {
				businessRepository.findById(businessId).ifPresent(b -> {
					if ("PENDING_APPROVAL".equalsIgnoreCase(b.getStatus())
							|| "PENDING_EMAIL_VERIFICATION".equalsIgnoreCase(userStatus)) {
						businessRepository.delete(b);
					}
				});
			}
			log.info("[DORMANT_PURGE] removed userId={} businessId={}", u.getId(), businessId);
		}
	}
}
