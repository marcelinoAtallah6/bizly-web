package com.notification.repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class KycCustomerWelcomeFlagDao {

	@PersistenceContext
	private EntityManager entityManager;

	@Transactional
	public void updateWelcomeFlags(Long customerId, int flag, int status) {
		if (customerId == null) {
			return;
		}
		entityManager.createNativeQuery(
				"UPDATE UM.KYC_CUSTOMER SET NOTIF_WELCOME_FLAG = :flag, NOTIF_WELCOME_STATUS = :st WHERE ID = :id")
				.setParameter("flag", flag)
				.setParameter("st", status)
				.setParameter("id", customerId)
				.executeUpdate();
	}
}
