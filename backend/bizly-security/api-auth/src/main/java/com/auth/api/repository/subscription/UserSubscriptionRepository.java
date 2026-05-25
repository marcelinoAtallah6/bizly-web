package com.auth.api.repository.subscription;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.auth.api.model.subscription.UserSubscriptionEntity;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscriptionEntity, Long> {

	Optional<UserSubscriptionEntity> findByExternalRef(String externalRef);

	boolean existsByExternalRef(String externalRef);

	boolean existsByUserIdAndStatus(Long userId, String status);
}
