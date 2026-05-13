package com.settings.api.reporting;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.settings.api.dto.reporting.ReportColumnDef;
import com.settings.api.dto.reporting.ReportPageResponse;

/**
 * Shared helpers for every {@link ReportProvider}:
 *  - safe parsing of incoming filter values out of the open {@code Map<String,Object>}
 *  - clamping pagination
 *  - validating sort columns against a whitelist
 *  - building the response envelope and turning JDBC dates into ISO strings
 *
 * Nothing here builds SQL — that stays in each provider, where the column
 * whitelist is owned. This class is intentionally a pile of static helpers
 * rather than a base class so providers compose freely.
 */
public final class ReportingSupport {

	private ReportingSupport() {}

	public static final int DEFAULT_PAGE_SIZE = 25;
	public static final int MAX_PAGE_SIZE = 200;
	public static final int DEFAULT_EXPORT_CAP = 10_000;
	public static final int MAX_EXPORT_CAP = 50_000;

	private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

	public static int safePageSize(int requested, boolean paginate) {
		if (!paginate) {
			return MAX_EXPORT_CAP;
		}
		if (requested <= 0) return DEFAULT_PAGE_SIZE;
		return Math.min(requested, MAX_PAGE_SIZE);
	}

	public static int safePageNumber(int requested) {
		return Math.max(0, requested);
	}

	public static int safeExportCap(Integer requested) {
		if (requested == null || requested <= 0) {
			return DEFAULT_EXPORT_CAP;
		}
		return Math.min(requested, MAX_EXPORT_CAP);
	}

	/**
	 * Resolves a user-supplied sort spec against a column whitelist. Returns
	 * a SQL fragment safe to splice into ORDER BY (column expression already
	 * mapped, direction validated).
	 */
	public static String resolveOrderBy(String requestedKey, String requestedDir,
			Map<String, String> columnSqlByKey, String defaultKey, String defaultDir) {
		String key = requestedKey;
		if (key == null || !columnSqlByKey.containsKey(key)) {
			key = defaultKey;
		}
		String dir = (requestedDir != null ? requestedDir.trim().toUpperCase() : "");
		if (!"ASC".equals(dir) && !"DESC".equals(dir)) {
			dir = (defaultDir != null ? defaultDir.trim().toUpperCase() : "DESC");
			if (!"ASC".equals(dir) && !"DESC".equals(dir)) {
				dir = "DESC";
			}
		}
		String sqlExpr = columnSqlByKey.get(key);
		if (sqlExpr == null) {
			return null;
		}
		return sqlExpr + " " + dir + " NULLS LAST";
	}

	/** Read a {@code String} from the filter map, trimming/blank-normalising. */
	public static String getString(Map<String, Object> filters, String key) {
		Object v = filters != null ? filters.get(key) : null;
		if (v == null) return null;
		String s = v.toString().trim();
		return s.isEmpty() ? null : s;
	}

	/** Read a nested {@code from} / {@code to} ISO date pair (DATE_RANGE filter). */
	public static LocalDate[] getDateRange(Map<String, Object> filters, String key) {
		Object v = filters != null ? filters.get(key) : null;
		if (!(v instanceof Map)) return new LocalDate[] { null, null };
		Map<?, ?> m = (Map<?, ?>) v;
		LocalDate from = parseLocalDate(m.get("from"));
		LocalDate to = parseLocalDate(m.get("to"));
		return new LocalDate[] { from, to };
	}

	/** Read a single ISO date. */
	public static LocalDate getDate(Map<String, Object> filters, String key) {
		Object v = filters != null ? filters.get(key) : null;
		return parseLocalDate(v);
	}

	/** Forgiving Long parse for SELECT options that come back as strings or numbers. */
	public static Long getLong(Map<String, Object> filters, String key) {
		Object v = filters != null ? filters.get(key) : null;
		if (v == null) return null;
		if (v instanceof Number) return ((Number) v).longValue();
		try { return Long.parseLong(v.toString().trim()); }
		catch (NumberFormatException e) { return null; }
	}

	public static List<String> splitCsv(String v) {
		if (v == null) return Collections.emptyList();
		String[] parts = v.split(",");
		List<String> out = new ArrayList<>(parts.length);
		for (String p : parts) {
			String t = p.trim();
			if (!t.isEmpty()) out.add(t);
		}
		return out;
	}

	private static LocalDate parseLocalDate(Object v) {
		if (v == null) return null;
		String s = v.toString().trim();
		if (s.isEmpty()) return null;
		try {
			// Accept "2024-01-15" or "2024-01-15T00:00:00.000Z"
			if (s.length() >= 10) {
				return LocalDate.parse(s.substring(0, 10), ISO_DATE);
			}
		} catch (DateTimeParseException ignored) {
			// fall through
		}
		return null;
	}

	/** Format a JDBC timestamp/date as an ISO string the frontend can parse uniformly. */
	public static String toIso(Object v) {
		if (v == null) return null;
		if (v instanceof Timestamp) {
			return ((Timestamp) v).toLocalDateTime().toString();
		}
		if (v instanceof LocalDateTime) {
			return ((LocalDateTime) v).toString();
		}
		if (v instanceof OffsetDateTime) {
			return ((OffsetDateTime) v).withOffsetSameInstant(ZoneOffset.UTC).toString();
		}
		if (v instanceof java.sql.Date) {
			return v.toString();
		}
		if (v instanceof LocalDate) {
			return ((LocalDate) v).toString();
		}
		return v.toString();
	}

	/** Build the response envelope from already-built rows / counts. */
	public static ReportPageResponse buildPage(String typeKey, List<ReportColumnDef> columns,
			List<Map<String, Object>> items, long totalCount, int pageNumber, int pageSize) {
		ReportPageResponse res = new ReportPageResponse();
		res.setTypeKey(typeKey);
		res.setColumns(columns);
		res.setItems(items);
		res.setTotalCount(totalCount);
		res.setPageNumber(pageNumber);
		res.setPageSize(pageSize);
		int totalPages = pageSize <= 0 ? 0 : (int) ((totalCount + pageSize - 1) / pageSize);
		res.setTotalPages(totalPages);
		res.setGeneratedAt(OffsetDateTime.now(ZoneOffset.UTC).toString());
		return res;
	}

	/** Convenience: a {@code LinkedHashMap} pre-populated from key/value alternation. */
	public static Map<String, Object> rowOf(Object... kv) {
		Map<String, Object> m = new LinkedHashMap<>();
		if (kv == null) return m;
		for (int i = 0; i + 1 < kv.length; i += 2) {
			m.put(String.valueOf(kv[i]), kv[i + 1]);
		}
		return m;
	}

	/** Wraps a value to allow IN (?) lists in JDBC. */
	public static List<Object> asList(Object... values) {
		return values == null ? Collections.emptyList() : Arrays.asList(values);
	}
}
