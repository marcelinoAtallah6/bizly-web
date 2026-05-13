package com.notification.repository;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;

import com.notification.workflow.CustomerBirthdayCandidate;

@Repository
public class KycCustomerBirthdayQueryDao {

	@PersistenceContext
	private EntityManager entityManager;

	@SuppressWarnings("unchecked")
	public List<CustomerBirthdayCandidate> findCustomersWithBirthdayToday() {
		String sql = ""
				+ "SELECT c.ID, c.EMAIL, c.FIRST_NAME, c.LAST_NAME, c.FULL_NAME FROM UM.KYC_CUSTOMER c "
				+ "WHERE c.EMAIL IS NOT NULL "
				+ "AND c.DOB IS NOT NULL "
				+ "AND EXTRACT(MONTH FROM c.DOB) = EXTRACT(MONTH FROM CAST(SYSDATE AS DATE)) "
				+ "AND EXTRACT(DAY FROM c.DOB) = EXTRACT(DAY FROM CAST(SYSDATE AS DATE))";
		List<Object[]> rows = entityManager.createNativeQuery(sql).getResultList();
		List<CustomerBirthdayCandidate> out = new ArrayList<>(rows.size());
		for (Object[] r : rows) {
			long id = ((Number) r[0]).longValue();
			String email = r[1] != null ? r[1].toString() : null;
			String fn = r[2] != null ? r[2].toString() : "";
			String ln = r[3] != null ? r[3].toString() : "";
			String full = r[4] != null ? r[4].toString() : "";
			String display = full != null && !full.isBlank() ? full : (fn + " " + ln).trim();
			out.add(new CustomerBirthdayCandidate(id, email, fn, ln, display));
		}
		return out;
	}
}
