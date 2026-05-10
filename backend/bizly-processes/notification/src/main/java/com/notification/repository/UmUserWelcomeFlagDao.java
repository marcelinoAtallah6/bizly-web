package com.notification.repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Updates welcome-notification columns on {@code UM.UM_USER}.
 */
@Repository
public class UmUserWelcomeFlagDao {

	@PersistenceContext
	private EntityManager entityManager;

	@Transactional
	public void updateWelcomeFlags(Long userId, int flag, int status) {
		if (userId == null) {
			return;
		}
		entityManager.createNativeQuery(
				"UPDATE UM.UM_USER SET NOTIF_WELCOME_FLAG = :flag, NOTIF_WELCOME_STATUS = :st WHERE ID = :id")
				.setParameter("flag", flag)
				.setParameter("st", status)
				.setParameter("id", userId)
				.executeUpdate();
	}
}
