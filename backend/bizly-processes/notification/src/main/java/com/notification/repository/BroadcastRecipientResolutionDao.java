package com.notification.repository;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;

@Repository
public class BroadcastRecipientResolutionDao {

	@PersistenceContext
	private EntityManager entityManager;

	@SuppressWarnings("unchecked")
	public List<String> findAllUserEmails() {
		List<String> emails = entityManager.createNativeQuery(
				"SELECT DISTINCT LOWER(u.EMAIL) FROM UM.UM_USER u WHERE u.EMAIL IS NOT NULL").getResultList();
		return normalize(emails);
	}

	@SuppressWarnings("unchecked")
	public List<String> findAllCustomerEmails() {
		List<String> emails = entityManager.createNativeQuery(
				"SELECT DISTINCT LOWER(c.EMAIL) FROM UM.KYC_CUSTOMER c WHERE c.EMAIL IS NOT NULL").getResultList();
		return normalize(emails);
	}

	@SuppressWarnings("unchecked")
	public List<String> findUserEmailsForRole(long roleId) {
		List<String> emails = entityManager.createNativeQuery(""
				+ "SELECT DISTINCT LOWER(u.EMAIL) FROM UM.UM_USER u "
				+ "JOIN UM.um_user_role ur ON ur.USER_ID = u.ID "
				+ "WHERE ur.ROLE_ID = :rid AND u.EMAIL IS NOT NULL")
				.setParameter("rid", roleId)
				.getResultList();
		return normalize(emails);
	}

	private static List<String> normalize(List<?> raw) {
		Set<String> set = new LinkedHashSet<>();
		for (Object o : raw) {
			if (o == null) {
				continue;
			}
			String e = o.toString().trim();
			if (!e.isEmpty()) {
				set.add(e);
			}
		}
		return new ArrayList<>(set);
	}
}
