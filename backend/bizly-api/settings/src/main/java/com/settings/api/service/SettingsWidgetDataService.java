package com.settings.api.service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.settings.api.model.SettingsDashboard;
import com.settings.api.model.SettingsWidget;
import com.settings.api.repository.SettingsWidgetRepository;
import com.settings.common.ApiMessages;
import com.settings.exception.ServiceException;
import com.settings.security.BusinessContextHolder;

@Service
public class SettingsWidgetDataService {

	@Autowired
	private SettingsWidgetRepository widgetRepository;

	@Autowired
	private DashboardAccessEvaluator accessEvaluator;

	@Autowired
	private SettingsJdbcQueryService jdbcQueryService;

	@Transactional(readOnly = true)
	public List<Map<String, Object>> executeWidgetQuery(String username,
			Collection<? extends GrantedAuthority> authorities, Long widgetId) {
		SettingsWidget w = widgetRepository.findWithDashboardAndQuery(widgetId)
				.orElseThrow(() -> new ServiceException(ApiMessages.SETTINGS_WIDGET_NOT_FOUND, HttpStatus.NOT_FOUND));
		if (!accessEvaluator.canAccess(username, authorities, w.getDashboard())) {
			throw new ServiceException(ApiMessages.SETTINGS_ACCESS_DENIED, HttpStatus.FORBIDDEN);
		}
		/*
		 * Tenant scope: even if a widget is reachable via a stale URL, the parent dashboard's
		 * business id must match the caller's. Globally-published dashboards (business_id IS NULL)
		 * are visible to everyone; admin callers (role-level=ADMIN) bypass the check entirely.
		 */
		if (!BusinessContextHolder.canBypassTenant()) {
			SettingsDashboard dash = w.getDashboard();
			Long dashBiz = dash != null ? dash.getBusinessId() : null;
			Long callerBiz = BusinessContextHolder.currentBusinessId();
			if (dashBiz != null && !dashBiz.equals(callerBiz)) {
				throw new ServiceException(ApiMessages.SETTINGS_ACCESS_DENIED, HttpStatus.FORBIDDEN);
			}
		}
		String type = w.getWidgetType() != null ? w.getWidgetType().toUpperCase() : "";
		if ("QUICK_ACTION".equals(type)) {
			return Collections.emptyList();
		}
		if (w.getQueryDef() == null || w.getQueryDef().getSqlText() == null) {
			throw new ServiceException(ApiMessages.SETTINGS_QUERY_NOT_FOUND, HttpStatus.BAD_REQUEST);
		}
		return jdbcQueryService.executeSelect(w.getQueryDef().getSqlText());
	}
}
