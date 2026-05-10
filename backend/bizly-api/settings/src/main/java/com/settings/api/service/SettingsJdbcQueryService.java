package com.settings.api.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SettingsJdbcQueryService {

	@Autowired
	private DataSource dataSource;

	@Autowired
	private SqlQueryValidationService validationService;

	@Value("${settings.query.max-rows:5000}")
	private int maxRows;

	public List<Map<String, Object>> executeSelect(String sql) {
		validationService.validateSelectOnly(sql);
		String trimmed = validationService.stripCommentsAndNormalize(sql);
		String safe = trimmed.endsWith(";") ? trimmed.substring(0, trimmed.length() - 1).trim() : trimmed;

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
						row.put(label != null ? label : md.getColumnName(c), rs.getObject(c));
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
}
