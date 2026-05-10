package com.settings.api.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.settings.api.dto.dashboard.DashboardDetailResponse;
import com.settings.api.dto.dashboard.DashboardSaveRequest;
import com.settings.api.dto.dashboard.WidgetDto;
import com.settings.api.dto.dashboard.DashboardIdRequest;
import com.settings.api.model.SettingsDashboard;
import com.settings.api.model.SettingsDashboardRoleGrant;
import com.settings.api.model.SettingsDashboardUserGrant;
import com.settings.api.model.SettingsQueryDef;
import com.settings.api.model.SettingsWidget;
import com.settings.api.repository.SettingsDashboardRepository;
import com.settings.api.repository.SettingsDashboardRoleGrantRepository;
import com.settings.api.repository.SettingsDashboardUserGrantRepository;
import com.settings.api.repository.SettingsQueryDefRepository;
import com.settings.api.repository.SettingsWidgetRepository;
import com.settings.common.ApiMessages;
import com.settings.exception.ServiceException;

@Service
public class SettingsDashboardManagementService {

	@Autowired
	private SettingsDashboardRepository dashboardRepository;

	@Autowired
	private SettingsWidgetRepository widgetRepository;

	@Autowired
	private SettingsQueryDefRepository queryDefRepository;

	@Autowired
	private SettingsDashboardRoleGrantRepository roleGrantRepository;

	@Autowired
	private SettingsDashboardUserGrantRepository userGrantRepository;

	@Autowired
	private SqlQueryValidationService validationService;

	@Autowired
	private SettingsDashboardCatalogService catalogService;

	@Transactional
	public DashboardDetailResponse save(DashboardSaveRequest req) {
		if (req.getWidgets() == null) {
			req.setWidgets(Collections.emptyList());
		}
		Optional<SettingsDashboard> slugOwner = dashboardRepository.findBySlugIgnoreCase(req.getSlug().trim());
		if (req.getId() == null) {
			if (slugOwner.isPresent()) {
				throw new ServiceException(ApiMessages.SETTINGS_DASHBOARD_SLUG_EXISTS, HttpStatus.CONFLICT);
			}
		} else if (slugOwner.isPresent() && !slugOwner.get().getId().equals(req.getId())) {
			throw new ServiceException(ApiMessages.SETTINGS_DASHBOARD_SLUG_EXISTS, HttpStatus.CONFLICT);
		}

		for (WidgetDto w : req.getWidgets()) {
			String type = w.getWidgetType() != null ? w.getWidgetType().trim().toUpperCase() : "";
			if (!"QUICK_ACTION".equals(type) && (w.getQueryDefId() == null)) {
				throw new ServiceException("Widget requires queryDefId unless widgetType is QUICK_ACTION",
						HttpStatus.BAD_REQUEST);
			}
			if (w.getQueryDefId() != null) {
				SettingsQueryDef q = queryDefRepository.findById(w.getQueryDefId())
						.orElseThrow(() -> new ServiceException(ApiMessages.SETTINGS_QUERY_NOT_FOUND,
								HttpStatus.NOT_FOUND));
				validationService.validateSelectOnly(q.getSqlText());
			}
		}

		SettingsDashboard d;
		if (req.getId() != null) {
			d = dashboardRepository.findById(req.getId())
					.orElseThrow(() -> new ServiceException(ApiMessages.SETTINGS_DASHBOARD_NOT_FOUND,
							HttpStatus.NOT_FOUND));
			widgetRepository.deleteByDashboard_Id(d.getId());
			roleGrantRepository.deleteByIdDashboardId(d.getId());
			userGrantRepository.deleteByIdDashboardId(d.getId());
		} else {
			d = new SettingsDashboard();
		}

		d.setName(req.getName().trim());
		d.setSlug(req.getSlug().trim().toLowerCase());
		d.setDescription(req.getDescription());
		d.setLayoutJson(req.getLayoutJson());
		d.setBuiltin(req.isBuiltin());

		d = dashboardRepository.save(d);

		List<SettingsWidget> rows = new ArrayList<>();
		for (WidgetDto wd : req.getWidgets()) {
			SettingsWidget sw = new SettingsWidget();
			sw.setDashboard(d);
			sw.setWidgetType(wd.getWidgetType().trim().toUpperCase());
			sw.setTitle(wd.getTitle().trim());
			sw.setConfigJson(wd.getConfigJson());
			sw.setGridX(wd.getGridX());
			sw.setGridY(wd.getGridY());
			sw.setGridW(wd.getGridW());
			sw.setGridH(wd.getGridH());
			sw.setRefreshSec(wd.getRefreshSec());
			sw.setSortOrder(wd.getSortOrder());
			if (wd.getQueryDefId() != null) {
				sw.setQueryDef(queryDefRepository.findById(wd.getQueryDefId()).orElseThrow(
						() -> new ServiceException(ApiMessages.SETTINGS_QUERY_NOT_FOUND, HttpStatus.NOT_FOUND)));
			}
			rows.add(sw);
		}
		widgetRepository.saveAll(rows);

		saveGrants(d.getId(), req.getGrantRoles(), req.getGrantUsernames());

		List<SettingsWidget> loaded = widgetRepository.findForDashboardWithQuery(d.getId());
		SettingsDashboard fresh = dashboardRepository.findById(d.getId())
				.orElseThrow(() -> new ServiceException(ApiMessages.SETTINGS_DASHBOARD_NOT_FOUND, HttpStatus.NOT_FOUND));
		return catalogService.toDetail(fresh, loaded);
	}

	private void saveGrants(Long dashboardId, List<String> roles, List<String> users) {
		if (roles != null) {
			for (String r : roles) {
				if (r == null || r.isBlank()) {
					continue;
				}
				SettingsDashboardRoleGrant g = new SettingsDashboardRoleGrant();
				SettingsDashboardRoleGrant.GrantId id = new SettingsDashboardRoleGrant.GrantId();
				id.setDashboardId(dashboardId);
				String rn = r.trim();
				id.setRoleName(DashboardAccessEvaluator
						.normalizeRole(rn.regionMatches(true, 0, "ROLE_", 0, 5) ? rn : "ROLE_" + rn));
				g.setId(id);
				roleGrantRepository.save(g);
			}
		}
		if (users != null) {
			for (String u : users) {
				if (u == null || u.isBlank()) {
					continue;
				}
				SettingsDashboardUserGrant g = new SettingsDashboardUserGrant();
				SettingsDashboardUserGrant.UserGrantId id = new SettingsDashboardUserGrant.UserGrantId();
				id.setDashboardId(dashboardId);
				id.setUsername(u.trim());
				g.setId(id);
				userGrantRepository.save(g);
			}
		}
	}

	@Transactional
	public void delete(DashboardIdRequest req) {
		Long id = req.getId();
		if (!dashboardRepository.existsById(id)) {
			throw new ServiceException(ApiMessages.SETTINGS_DASHBOARD_NOT_FOUND, HttpStatus.NOT_FOUND);
		}
		widgetRepository.deleteByDashboard_Id(id);
		roleGrantRepository.deleteByIdDashboardId(id);
		userGrantRepository.deleteByIdDashboardId(id);
		dashboardRepository.deleteById(id);
	}
}
