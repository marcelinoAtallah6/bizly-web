package com.bm.api.util;

import java.time.LocalDateTime;

/**
 * Interval overlap rule used for appointment validation:
 * two intervals overlap iff {@code A.start < B.end AND A.end > B.start}.
 */
public final class AppointmentIntervalUtil {

	private AppointmentIntervalUtil() {
	}

	public static boolean overlaps(LocalDateTime aStart, LocalDateTime aEnd, LocalDateTime bStart, LocalDateTime bEnd) {
		return aStart.isBefore(bEnd) && aEnd.isAfter(bStart);
	}
}
