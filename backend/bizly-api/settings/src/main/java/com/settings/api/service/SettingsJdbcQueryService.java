package com.settings.api.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;

/**
 * Lightweight wrapper around the data source that runs only SELECTs.
 *
 * <p>Two execution paths:
 * <ul>
 *   <li>{@link #executeSelect(String)} — pure-string statement for the Query
 *       Builder's "execute test" flow (no parameter binding).</li>
 *   <li>{@link #executeSelectWithParams(String, Map, Integer)} — parameterised
 *       binding for the Report Builder runtime. Auto-binds any
 *       {@code :placeholder} found in the SQL to {@code NULL} when the caller
 *       did not provide a value, so queries can use the
 *       {@code (:fromDate IS NULL OR col &gt;= :fromDate)} pattern safely.</li>
 * </ul>
 */
@Service
public class SettingsJdbcQueryService {

	@Autowired
	private DataSource dataSource;

	@Autowired
	private SqlQueryValidationService validationService;

	@Autowired
	private NamedParameterJdbcTemplate namedJdbc;

	@Value("${settings.query.max-rows:5000}")
	private int maxRows;

	/** Matches Oracle/JPA-style {@code :name} bind placeholders (ignores leading colons of `::cast`). */
	private static final Pattern BIND_PLACEHOLDER = Pattern.compile("(?<![:])(?<![A-Za-z0-9_]):([A-Za-z_][A-Za-z0-9_]*)");

	public List<Map<String, Object>> executeSelect(String sql) {
		validationService.validateSelectOnly(sql);
		String safe = stripTrailingSemicolon(validationService.stripCommentsAndNormalize(sql));

		List<Map<String, Object>> rows = new ArrayList<>();
		try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
			st.setMaxRows(maxRows);
			try (ResultSet rs = st.executeQuery(safe)) {
				ResultSetMetaData md = rs.getMetaData();
				int cols = md.getColumnCount();
				while (rs.next()) {
					Map<String, Object> row = new LinkedHashMap<>();
					for (int c = 1; c <= cols; c++) {
						String label = md.getColumnLabel(c);
						row.put(label != null ? label : md.getColumnName(c),
								JdbcResultValueConverter.normalize(rs.getObject(c)));
					}
					rows.add(row);
				}
			}
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException("Query execution failed: " + e.getMessage(), e);
		}
		return rows;
	}

	/**
	 * Run a parameterised SELECT and materialise the rows. Every placeholder
	 * present in the SQL is bound — anything the caller didn't supply becomes
	 * {@code NULL}, which lets reports keep optional filters without nullable-
	 * checking each one in Java.
	 *
	 * @param sql       the SELECT (or WITH … SELECT) statement
	 * @param params    caller-supplied named params (may be {@code null})
	 * @param rowLimit  optional row cap (defaults to {@code settings.query.max-rows})
	 */
	public List<Map<String, Object>> executeSelectWithParams(String sql,
			Map<String, ?> params, Integer rowLimit) {
		validationService.validateSelectOnly(sql);
		String safe = stripTrailingSemicolon(validationService.stripCommentsAndNormalize(sql));

		SqlParameterSource source = buildBoundParameters(safe, params);

		final int cap = rowLimit != null && rowLimit > 0 ? rowLimit : maxRows;
		return namedJdbc.query(safe, source, rs -> {
			List<Map<String, Object>> rows = new ArrayList<>();
			ResultSetMetaData md = rs.getMetaData();
			int cols = md.getColumnCount();
			int read = 0;
			while (rs.next() && read < cap) {
				Map<String, Object> row = new LinkedHashMap<>();
				for (int c = 1; c <= cols; c++) {
					String label = md.getColumnLabel(c);
					row.put(label != null ? label : md.getColumnName(c),
							JdbcResultValueConverter.normalize(rs.getObject(c)));
				}
				rows.add(row);
				read++;
			}
			return rows;
		});
	}

	/**
	 * Returns just the result-set metadata for a SELECT (column names + JDBC
	 * types) without fetching rows. Used by the Report Builder to auto-detect
	 * what columns the linked query exposes.
	 */
	public List<JdbcColumnMeta> describeColumns(String sql, Map<String, ?> params) {
		validationService.validateSelectOnly(sql);
		String safe = stripTrailingSemicolon(validationService.stripCommentsAndNormalize(sql));
		// FETCH FIRST 0 keeps the optimizer happy without round-tripping rows.
		String describeSql = "SELECT * FROM (" + safe + ") BIZLY_DESC FETCH FIRST 0 ROWS ONLY";

		SqlParameterSource source = buildBoundParameters(describeSql, params);

		return namedJdbc.query(describeSql, source, rs -> {
			List<JdbcColumnMeta> cols = new ArrayList<>();
			ResultSetMetaData md = rs.getMetaData();
			int n = md.getColumnCount();
			for (int c = 1; c <= n; c++) {
				JdbcColumnMeta meta = new JdbcColumnMeta();
				meta.name = md.getColumnLabel(c) != null ? md.getColumnLabel(c) : md.getColumnName(c);
				meta.jdbcType = md.getColumnType(c);
				meta.typeName = md.getColumnTypeName(c);
				cols.add(meta);
			}
			return cols;
		});
	}

	private SqlParameterSource buildBoundParameters(String sql, Map<String, ?> params) {
		MapSqlParameterSource src = new MapSqlParameterSource();
		if (params != null) {
			for (Map.Entry<String, ?> e : params.entrySet()) {
				src.addValue(e.getKey(), e.getValue());
			}
		}
		Matcher m = BIND_PLACEHOLDER.matcher(sql);
		while (m.find()) {
			String name = m.group(1);
			if (!src.hasValue(name)) {
				src.addValue(name, null);
			}
		}
		return src;
	}

	private String stripTrailingSemicolon(String sql) {
		String trimmed = sql.trim();
		while (trimmed.endsWith(";")) {
			trimmed = trimmed.substring(0, trimmed.length() - 1).trim();
		}
		return trimmed;
	}

	/** Minimal column descriptor returned by {@link #describeColumns}. */
	public static class JdbcColumnMeta {
		public String name;
		public int jdbcType;
		public String typeName;
	}
}
