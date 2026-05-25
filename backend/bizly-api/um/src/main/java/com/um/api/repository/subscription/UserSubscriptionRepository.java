package com.um.api.repository.subscription;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.um.api.model.subscription.UserSubscription;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {

	Optional<UserSubscription> findByExternalRef(String externalRef);

	boolean existsByExternalRef(String externalRef);

	boolean existsByUserIdAndStatus(Long userId, String status);
}
