package com.notification.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.notification.domain.NotificationTemplateEntity;

public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplateEntity, Long> {

	Optional<NotificationTemplateEntity> findByTemplateKeyAndActiveInd(String templateKey, String activeInd);
}
