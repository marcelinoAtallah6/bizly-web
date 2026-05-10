package com.notification.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.notification.domain.EmailTemplateEntity;

public interface EmailTemplateRepository extends JpaRepository<EmailTemplateEntity, Long> {

	Optional<EmailTemplateEntity> findByTemplateKeyAndActiveInd(String templateKey, String activeInd);
}
