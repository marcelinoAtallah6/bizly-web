package com.travel.api.service.notif;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Resolves active users in a business who can VIEW {@code /travel/bookings} — the same gate used
 * by the header inbox filter in the BM service.
 */
@Service
public class TravelNotificationRecipientService {

	private static final String BOOKINGS_MENU_ROUTE = "/travel/bookings";

	private static final String SQL = ""
			+ "SELECT DISTINCT u.username "
			+ "FROM um.um_user u "
			+ "INNER JOIN um.um_user_role ur ON ur.user_id = u.id "
			+ "INNER JOIN um.um_role_menu_perm rmp ON rmp.role_id = ur.role_id "
			+ "INNER JOIN um.um_menus m ON m.id = rmp.menu_id "
			+ "WHERE u.business_id = ? "
			+ "AND UPPER(TRIM(COALESCE(u.status, ''))) = 'ACTIVE' "
			+ "AND rmp.allow_view = 1 "
			+ "AND UPPER(TRIM(m.route)) = UPPER(TRIM(?)) "
			+ "ORDER BY u.username";

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public List<String> findUsernamesWithBookingsView(Long businessId) {
		if (businessId == null) {
			return Collections.emptyList();
		}
		return jdbcTemplate.query(SQL,
				(rs, rowNum) -> rs.getString(1),
				businessId, BOOKINGS_MENU_ROUTE);
	}
}
