package com.settings.api.service;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;

/**
 * Converts JDBC / Oracle driver types into JSON-safe values (no streams or
 * proprietary {@code oracle.sql.*} beans).
 */
final class JdbcResultValueConverter {

	private JdbcResultValueConverter() {}

	static Object normalize(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof String || value instanceof Number || value instanceof Boolean) {
			return value;
		}
		if (value instanceof byte[]) {
			return value;
		}
		if (value instanceof Timestamp) {
			Timestamp ts = (Timestamp) value;
			return ts.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().toString();
		}
		if (value instanceof Date) {
			Date sqlDate = (Date) value;
			return sqlDate.toLocalDate().toString();
		}
		if (value instanceof Time) {
			Time time = (Time) value;
			return time.toLocalTime().toString();
		}
		if (value instanceof java.util.Date) {
			java.util.Date utilDate = (java.util.Date) value;
			return Instant.ofEpochMilli(utilDate.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime()
					.toString();
		}
		if (value instanceof Calendar) {
			Calendar cal = (Calendar) value;
			return cal.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().toString();
		}
		if (value instanceof BigDecimal) {
			return value;
		}
		if (value instanceof Clob) {
			return readClob((Clob) value);
		}
		if (value instanceof InputStream) {
			return readStream((InputStream) value);
		}
		if (value instanceof Reader) {
			try {
				return readReader((Reader) value);
			} catch (Exception e) {
				return null;
			}
		}
		String className = value.getClass().getName();
		if (className.startsWith("oracle.sql.")) {
			return oracleWrapperToString(value);
		}
		if (value instanceof LocalDateTime) {
			return value.toString();
		}
		if (value instanceof LocalDate) {
			return value.toString();
		}
		if (value instanceof Instant) {
			Instant inst = (Instant) value;
			return inst.atZone(ZoneId.systemDefault()).toLocalDateTime().toString();
		}
		return value.toString();
	}

	private static String oracleWrapperToString(Object value) {
		try {
			if (value instanceof java.util.Date) {
				java.util.Date utilDate = (java.util.Date) value;
				return Instant.ofEpochMilli(utilDate.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime()
						.toString();
			}
			try {
				java.lang.reflect.Method m = value.getClass().getMethod("timestampValue");
				Object tsObj = m.invoke(value);
				if (tsObj instanceof Timestamp) {
					Timestamp ts = (Timestamp) tsObj;
					return ts.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().toString();
				}
			} catch (ReflectiveOperationException ignored) {
				// fall through
			}
			try {
				java.lang.reflect.Method m = value.getClass().getMethod("dateValue");
				Object dateObj = m.invoke(value);
				if (dateObj instanceof java.util.Date) {
					java.util.Date utilDate = (java.util.Date) dateObj;
					return Instant.ofEpochMilli(utilDate.getTime()).atZone(ZoneId.systemDefault()).toLocalDate()
							.toString();
				}
			} catch (ReflectiveOperationException ignored) {
				// fall through
			}
			return value.toString();
		} catch (Exception e) {
			return value.toString();
		}
	}

	private static String readClob(Clob clob) {
		try (Reader r = clob.getCharacterStream()) {
			return readReader(r);
		} catch (Exception e) {
			return null;
		}
	}

	private static String readStream(InputStream in) {
		try (InputStream stream = in) {
			return new String(stream.readAllBytes());
		} catch (Exception e) {
			return null;
		}
	}

	private static String readReader(Reader reader) throws Exception {
		StringBuilder sb = new StringBuilder();
		char[] buf = new char[4096];
		int n;
		while ((n = reader.read(buf)) >= 0) {
			sb.append(buf, 0, n);
		}
		return sb.toString();
	}
}
