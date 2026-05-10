package com.notification.repository;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;

import com.notification.workflow.WelcomePendingUser;

/**
 * Users pending welcome email: {@code NOTIF_WELCOME_FLAG = 0} on {@code UM.UM_USER}.
 */
@Repository
public class UmUserWelcomeQueryDao {

	@PersistenceContext
	private EntityManager entityManager;

	@SuppressWarnings("unchecked")
	public List<WelcomePendingUser> findPendingWelcome(int maxResults) {
		String sql = ""
				+ "SELECT u.ID, u.USERNAME, u.EMAIL, u.FIRST_NAME, u.LAST_NAME "
				+ "FROM UM.UM_USER u "
				+ "WHERE u.EMAIL IS NOT NULL "
				+ "AND NVL(u.NOTIF_WELCOME_FLAG, 0) = 0 "
				+ "ORDER BY u.ID";
		List<Object[]> rows = entityManager.createNativeQuery(sql).setMaxResults(maxResults).getResultList();
		List<WelcomePendingUser> out = new ArrayList<>(rows.size());
		for (Object[] r : rows) {
			long id = ((Number) r[0]).longValue();
			String username = r[1] != null ? r[1].toString() : "";
			String email = r[2] != null ? r[2].toString() : "";
			String fn = r[3] != null ? r[3].toString() : "";
			String ln = r[4] != null ? r[4].toString() : "";
			out.add(new WelcomePendingUser(id, username, email, fn, ln));
		}
		return out;
	}
}
