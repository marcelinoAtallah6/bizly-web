package com.settings.api.reporting;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.settings.api.dto.reporting.ReportColumnConfig;
import com.settings.api.dto.reporting.ReportColumnDef;
import com.settings.api.dto.reporting.ReportFilterConfig;
import com.settings.api.dto.reporting.ReportFilterDef;
import com.settings.api.dto.reporting.ReportPageResponse;
import com.settings.api.dto.reporting.ReportRequest;
import com.settings.api.dto.reporting.ReportTypeMeta;
import com.settings.api.model.SettingsReport;
import com.settings.api.service.SettingsReportService;
import com.settings.common.ApiMessages;
import com.settings.exception.ServiceException;

/**
 * Public reporting API. Now backed by user-defined reports from
 * {@code SETTINGS_REPORT} (built via the Report Builder). The legacy
 * hardcoded providers have been retired.
 */
@Service
public class ReportingService {

	@Autowired
	private SettingsReportService reportService;

	@Autowired
	private ReportSqlExecutor executor;

	/** Active reports the caller may run (visibility grants applied). */
	@Transactional(readOnly = true)
	public List<ReportTypeMeta> listTypes() {
		List<ReportTypeMeta> out = new ArrayList<>();
		for (SettingsReport r : reportService.listActiveEntitiesForCaller()) {
			out.add(toMeta(r));
		}
		return out;
	}

	@Transactional(readOnly = true)
	public ReportPageResponse generate(ReportRequest req) {
		SettingsReport rep = lookup(req);
		return executor.execute(rep, req, true);
	}

	@Transactional(readOnly = true)
	public ReportPageResponse export(ReportRequest req) {
		SettingsReport rep = lookup(req);
		req.setPageNumber(0);
		return executor.execute(rep, req, false);
	}

	private SettingsReport lookup(ReportRequest req) {
		if (req == null || req.getTypeKey() == null || req.getTypeKey().isBlank()) {
			throw new ServiceException(ApiMessages.REPORTING_TYPE_NOT_FOUND, HttpStatus.NOT_FOUND);
		}
		String key = req.getTypeKey().trim();
		SettingsReport report;
		// Accept either the report code (slug) or a numeric id.
		if (key.chars().allMatch(Character::isDigit)) {
			try {
				report = reportService.getEntity(Long.parseLong(key));
			} catch (NumberFormatException nfe) {
				report = reportService.getEntityByCode(key);
			}
		} else {
			report = reportService.getEntityByCode(key);
		}
		if (!"ACTIVE".equalsIgnoreCase(report.getStatus())) {
			throw new ServiceException(ApiMessages.REPORTING_TYPE_NOT_FOUND, HttpStatus.NOT_FOUND);
		}
		return report;
	}

	private ReportTypeMeta toMeta(SettingsReport r) {
		List<ReportFilterConfig> filters = executor.parseFilters(r.getFiltersJson());
		List<ReportColumnConfig> columns = executor.parseColumns(r.getColumnsJson());

		ReportTypeMeta meta = new ReportTypeMeta();
		meta.setKey(r.getCode());
		meta.setName(r.getName());
		meta.setDescription(r.getDescription());
		meta.setIcon(r.getIcon());
		meta.setCategory("Reports");
		meta.setDefaultSortKey(r.getDefaultSortKey());
		meta.setDefaultSortDir(r.getDefaultSortDir());

		List<ReportFilterDef> filterDefs = new ArrayList<>(filters.size());
		for (ReportFilterConfig f : filters) {
			ReportFilterDef d = ReportFilterDef.of(
					f.getKey(),
					f.getLabel() != null ? f.getLabel() : f.getKey(),
					f.getType());
			d.withPlaceholder(f.getPlaceholder());
			if (f.getOptions() != null) d.withOptions(f.getOptions());
			filterDefs.add(d);
		}
		meta.setFilters(filterDefs);

		List<ReportColumnDef> columnDefs = new ArrayList<>(columns.size());
		for (ReportColumnConfig c : columns) {
			ReportColumnDef cd = ReportColumnDef.of(
					c.getKey(),
					c.getLabel() != null && !c.getLabel().isBlank() ? c.getLabel() : c.getKey(),
					c.getType() != null ? c.getType() : ReportColumnDef.ColumnType.STRING,
					c.isSortable());
			columnDefs.add(cd);
		}
		meta.setColumns(columnDefs);

		return meta;
	}
}
