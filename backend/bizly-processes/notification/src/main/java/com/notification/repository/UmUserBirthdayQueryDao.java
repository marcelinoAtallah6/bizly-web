package com.notification.repository;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;

import com.notification.workflow.BirthdayCandidate;

@Repository
public class UmUserBirthdayQueryDao {

	@PersistenceContext
	private EntityManager entityManager;

	@SuppressWarnings("unchecked")
	public List<BirthdayCandidate> findUsersWithBirthdayToday() {
		String sql = ""
				+ "SELECT u.ID, u.EMAIL, u.FIRST_NAME, u.LAST_NAME FROM UM.UM_USER u "
				+ "WHERE u.EMAIL IS NOT NULL "
				+ "AND u.DATE_OF_BIRTH IS NOT NULL "
				+ "AND EXTRACT(MONTH FROM u.DATE_OF_BIRTH) = EXTRACT(MONTH FROM CAST(SYSDATE AS DATE)) "
				+ "AND EXTRACT(DAY FROM u.DATE_OF_BIRTH) = EXTRACT(DAY FROM CAST(SYSDATE AS DATE))";
		List<Object[]> rows = entityManager.createNativeQuery(sql).getResultList();
		List<BirthdayCandidate> out = new ArrayList<>(rows.size());
		for (Object[] r : rows) {
			long id = ((Number) r[0]).longValue();
			String email = r[1] != null ? r[1].toString() : null;
			String fn = r[2] != null ? r[2].toString() : "";
			String ln = r[3] != null ? r[3].toString() : "";
			out.add(new BirthdayCandidate(id, email, fn, ln));
		}
		return out;
	}
}
