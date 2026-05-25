package com.settings.api.reporting;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.settings.api.dto.reporting.ReportColumnConfig;
import com.settings.api.dto.reporting.ReportColumnDef;
import com.settings.api.dto.reporting.ReportFilterConfig;
import com.settings.api.dto.reporting.ReportFilterDef.FilterType;
import com.settings.api.dto.reporting.ReportPageResponse;
import com.settings.api.dto.reporting.ReportRequest;
import com.settings.api.model.SettingsQueryDef;
import com.settings.api.model.SettingsReport;
import com.settings.api.repository.SettingsQueryDefRepository;
import com.settings.api.service.SettingsJdbcQueryService;
import com.settings.common.ApiMessages;
import com.settings.exception.ServiceException;
import com.settings.security.BusinessContextHolder;

/**
 * Executes one {@link SettingsReport} against its linked SQL.
 *
 * <p>Pipeline:
 * <ol>
 *   <li>Resolve the linked {@link SettingsQueryDef} and read its {@code sqlText}.</li>
 *   <li>Coerce each user-supplied filter value into the right JDBC type and
 *       map it to the filter's bound param name(s).</li>
 *   <li>Wrap the inner query in a {@code SELECT *} that applies the validated
 *       sort + OFFSET/FETCH NEXT pagination — so we never edit the saved SQL
 *       and any unbound {@code :placeholder} found inside the inner query
 *       gets a {@code NULL} bind automatically (see
 *       {@link SettingsJdbcQueryService#executeSelectWithParams}).</li>
 * </ol>
 *
 * <p>Counting uses a separate {@code SELECT COUNT(*) FROM (inner)} so totals
 * stay accurate even when the inner query has joins/aggregates.
 */
@Component
public class ReportSqlExecutor {

	private static final Logger log = LogManager.getLogger(ReportSqlExecutor.class);

	@Autowired
	private SettingsQueryDefRepository queryDefRepository;

	@Autowired
	private SettingsJdbcQueryService jdbc;

	@Autowired
	private ObjectMapper objectMapper;

	public ReportPageResponse execute(SettingsReport report, ReportRequest req, boolean paginate) {
		SettingsQueryDef qd = queryDefRepository.findById(report.getQueryDefId())
				.orElseThrow(() -> new ServiceException(ApiMessages.SETTINGS_QUERY_NOT_FOUND, HttpStatus.BAD_REQUEST));

		List<ReportFilterConfig> filters = parseFilters(report.getFiltersJson());
		List<ReportColumnConfig> columns = parseColumns(report.getColumnsJson());

		Map<String, Object> binds = bindFilters(filters, req.getFilters());
		String inner = qd.getSqlText();
		Long callerBiz = BusinessContextHolder.currentBusinessId();
		if (callerBiz != null && (inner.contains(":business_id") || inner.contains(":BUSINESS_ID"))) {
			binds.put("business_id", callerBiz);
		}
		Map<String, String> sortable = buildSortMap(columns);

		String orderBy = ReportingSupport.resolveOrderBy(req.getSortBy(), req.getSortDir(),
				sortable, report.getDefaultSortKey(), defaultDir(report));
		if (orderBy == null && !columns.isEmpty()) {
			orderBy = quoteColumn(columns.get(0).getKey()) + " DESC NULLS LAST";
		}

		int pageSize = ReportingSupport.safePageSize(req.getPageSize(), paginate);
		int pageNumber = ReportingSupport.safePageNumber(req.getPageNumber());
		int offset = pageNumber * pageSize;

		long total = countRows(inner, binds);

		String pagedSql = "SELECT * FROM (" + inner + ") BIZLY_RPT_INNER"
				+ (orderBy != null ? " ORDER BY " + orderBy : "")
				+ " OFFSET :__bizly_offset ROWS FETCH NEXT :__bizly_limit ROWS ONLY";

		Map<String, Object> params = new HashMap<>(binds);
		params.put("__bizly_offset", offset);
		params.put("__bizly_limit", pageSize);

		List<Map<String, Object>> raw;
		try {
			raw = jdbc.executeSelectWithParams(pagedSql, params, pageSize);
		} catch (RuntimeException e) {
			log.warn("[REPORT_EXEC] reportId={} failed: {}", report.getId(), e.getMessage());
			throw new ServiceException("Report execution failed: " + e.getMessage(), HttpStatus.BAD_REQUEST);
		}

		List<ReportColumnDef> publicCols = toPublicColumns(columns);
		List<Map<String, Object>> rows = projectRows(raw, columns);

		return ReportingSupport.buildPage(report.getCode(), publicCols, rows, total, pageNumber, pageSize);
	}

	// ---------------- Filter binding ----------------

	private Map<String, Object> bindFilters(List<ReportFilterConfig> filters,
			Map<String, Object> values) {
		Map<String, Object> binds = new HashMap<>();
		if (filters == null) return binds;
		for (ReportFilterConfig f : filters) {
			if (f.getType() == null) continue;
			switch (f.getType()) {
			case DATE_RANGE: {
				LocalDate[] range = ReportingSupport.getDateRange(values, f.getKey());
				putParam(binds, f.getFromParamName(), range[0] != null
						? Timestamp.valueOf(range[0].atStartOfDay()) : null);
				putParam(binds, f.getToParamName(), range[1] != null
						? Timestamp.valueOf(range[1].plusDays(1).atStartOfDay()) : null);
				break;
			}
			case DATE: {
				LocalDate d = ReportingSupport.getDate(values, f.getKey());
				putParam(binds, f.getParamName(), d != null ? Timestamp.valueOf(d.atStartOfDay()) : null);
				break;
			}
			case NUMBER: {
				Long n = ReportingSupport.getLong(values, f.getKey());
				putParam(binds, f.getParamName(), n);
				break;
			}
			case TEXT:
			case USER:
			case SELECT:
			default: {
				String s = ReportingSupport.getString(values, f.getKey());
				if (s != null && s.matches("-?\\d+")) {
					putParam(binds, f.getParamName(), Long.parseLong(s));
				} else {
					putParam(binds, f.getParamName(), s);
				}
				break;
			}
			}
		}
		return binds;
	}

	private void putParam(Map<String, Object> out, String name, Object value) {
		if (name == null || name.isBlank()) return;
		out.put(name, value);
	}

	// ---------------- Count helper ----------------

	private long countRows(String inner, Map<String, Object> binds) {
		String countSql = "SELECT COUNT(*) AS RPT_CNT FROM (" + inner + ") BIZLY_RPT_CNT";
		try {
			List<Map<String, Object>> rows = jdbc.executeSelectWithParams(countSql, binds, 1);
			if (rows.isEmpty()) return 0L;
			Object v = rows.get(0).values().iterator().next();
			if (v instanceof Number) return ((Number) v).longValue();
			return Long.parseLong(String.valueOf(v));
		} catch (RuntimeException e) {
			log.warn("[REPORT_EXEC] count failed: {}", e.getMessage());
			return 0L;
		}
	}

	// ---------------- Column / row helpers ----------------

	private Map<String, String> buildSortMap(List<ReportColumnConfig> columns) {
		Map<String, String> m = new LinkedHashMap<>();
		if (columns == null) return m;
		for (ReportColumnConfig c : columns) {
			if (c.isSortable() && c.getKey() != null) {
				m.put(c.getKey(), quoteColumn(c.getKey()));
			}
		}
		return m;
	}

	private String quoteColumn(String key) {
		// Oracle treats unquoted identifiers as uppercase. Quote to preserve exact case.
		return "\"" + key.replace("\"", "\"\"") + "\"";
	}

	private String defaultDir(SettingsReport r) {
		String d = r.getDefaultSortDir();
		if (d == null || d.isBlank()) return "DESC";
		return d.toUpperCase();
	}

	private List<Map<String, Object>> projectRows(List<Map<String, Object>> raw,
			List<ReportColumnConfig> columns) {
		if (columns == null || columns.isEmpty()) {
			List<Map<String, Object>> out = new ArrayList<>(raw.size());
			for (Map<String, Object> r : raw) {
				Map<String, Object> projected = new LinkedHashMap<>();
				for (Map.Entry<String, Object> e : r.entrySet()) {
					projected.put(e.getKey(), ReportingSupport.toIso(e.getValue()));
				}
				out.add(projected);
			}
			return out;
		}
		List<Map<String, Object>> out = new ArrayList<>(raw.size());
		for (Map<String, Object> r : raw) {
			Map<String, Object> projected = new LinkedHashMap<>();
			for (ReportColumnConfig c : columns) {
				Object v = r.containsKey(c.getKey()) ? r.get(c.getKey()) : null;
				projected.put(c.getKey(), ReportingSupport.toIso(v));
			}
			out.add(projected);
		}
		return out;
	}

	private List<ReportColumnDef> toPublicColumns(List<ReportColumnConfig> columns) {
		if (columns == null || columns.isEmpty()) return Collections.emptyList();
		List<ReportColumnDef> out = new ArrayList<>(columns.size());
		for (ReportColumnConfig c : columns) {
			ReportColumnDef d = ReportColumnDef.of(c.getKey(),
					c.getLabel() != null && !c.getLabel().isBlank() ? c.getLabel() : c.getKey(),
					c.getType() != null ? c.getType() : ReportColumnDef.ColumnType.STRING,
					c.isSortable());
			out.add(d);
		}
		return out;
	}

	// ---------------- JSON helpers ----------------

	public List<ReportFilterConfig> parseFilters(String json) {
		if (json == null || json.isBlank()) return new ArrayList<>();
		try {
			List<ReportFilterConfig> list = objectMapper.readValue(json,
					new TypeReference<List<ReportFilterConfig>>() {});
			return list != null ? list : new ArrayList<>();
		} catch (RuntimeException | java.io.IOException e) {
			log.warn("[REPORT_EXEC] could not parse filtersJson: {}", e.getMessage());
			return new ArrayList<>();
		}
	}

	public List<ReportColumnConfig> parseColumns(String json) {
		if (json == null || json.isBlank()) return new ArrayList<>();
		try {
			List<ReportColumnConfig> list = objectMapper.readValue(json,
					new TypeReference<List<ReportColumnConfig>>() {});
			return list != null ? list : new ArrayList<>();
		} catch (RuntimeException | java.io.IOException e) {
			log.warn("[REPORT_EXEC] could not parse columnsJson: {}", e.getMessage());
			return new ArrayList<>();
		}
	}

	public String filtersToJson(List<ReportFilterConfig> filters) {
		try {
			return filters != null ? objectMapper.writeValueAsString(filters) : null;
		} catch (RuntimeException | java.io.IOException e) {
			throw new ServiceException("Could not serialise filters: " + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public String columnsToJson(List<ReportColumnConfig> columns) {
		try {
			return columns != null ? objectMapper.writeValueAsString(columns) : null;
		} catch (RuntimeException | java.io.IOException e) {
			throw new ServiceException("Could not serialise columns: " + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Reads the column metadata from a query without fetching rows. Used by
	 * the Report Builder's auto-detect to seed the columns list.
	 */
	public List<ReportColumnConfig> introspectColumns(String sqlText, List<ReportFilterConfig> filters) {
		Map<String, Object> emptyBinds = new HashMap<>();
		// Bind every declared filter param to NULL so the introspect query parses.
		if (filters != null) {
			for (ReportFilterConfig f : filters) {
				if (f.getParamName() != null) emptyBinds.put(f.getParamName(), null);
				if (f.getFromParamName() != null) emptyBinds.put(f.getFromParamName(), null);
				if (f.getToParamName() != null) emptyBinds.put(f.getToParamName(), null);
			}
		}
		List<SettingsJdbcQueryService.JdbcColumnMeta> jdbcCols;
		try {
			jdbcCols = jdbc.describeColumns(sqlText, emptyBinds);
		} catch (RuntimeException e) {
			throw new ServiceException("Could not introspect query columns: " + e.getMessage(),
					HttpStatus.BAD_REQUEST);
		}
		List<ReportColumnConfig> out = new ArrayList<>(jdbcCols.size());
		for (SettingsJdbcQueryService.JdbcColumnMeta m : jdbcCols) {
			ReportColumnConfig c = new ReportColumnConfig();
			c.setKey(m.name);
			c.setLabel(prettifyLabel(m.name));
			c.setType(mapJdbcType(m.jdbcType, m.typeName));
			c.setSortable(true);
			c.setVisible(true);
			out.add(c);
		}
		return out;
	}

	private String prettifyLabel(String key) {
		if (key == null) return null;
		String spaced = key.replace('_', ' ').toLowerCase();
		StringBuilder sb = new StringBuilder(spaced.length());
		boolean cap = true;
		for (int i = 0; i < spaced.length(); i++) {
			char ch = spaced.charAt(i);
			if (Character.isWhitespace(ch)) {
				cap = true;
				sb.append(ch);
			} else if (cap) {
				sb.append(Character.toUpperCase(ch));
				cap = false;
			} else {
				sb.append(ch);
			}
		}
		return sb.toString();
	}

	private ReportColumnDef.ColumnType mapJdbcType(int jdbcType, String typeName) {
		switch (jdbcType) {
		case java.sql.Types.BIT:
		case java.sql.Types.BOOLEAN:
			return ReportColumnDef.ColumnType.BOOLEAN;
		case java.sql.Types.TINYINT:
		case java.sql.Types.SMALLINT:
		case java.sql.Types.INTEGER:
		case java.sql.Types.BIGINT:
			return ReportColumnDef.ColumnType.NUMBER;
		case java.sql.Types.FLOAT:
		case java.sql.Types.REAL:
		case java.sql.Types.DOUBLE:
		case java.sql.Types.NUMERIC:
		case java.sql.Types.DECIMAL:
			// We default to NUMBER; builder can flip to MONEY in the UI.
			return ReportColumnDef.ColumnType.NUMBER;
		case java.sql.Types.DATE:
			return ReportColumnDef.ColumnType.DATE;
		case java.sql.Types.TIME:
		case java.sql.Types.TIMESTAMP:
		case java.sql.Types.TIMESTAMP_WITH_TIMEZONE:
		case java.sql.Types.TIME_WITH_TIMEZONE:
			return ReportColumnDef.ColumnType.DATETIME;
		default:
			return ReportColumnDef.ColumnType.STRING;
		}
	}
}
