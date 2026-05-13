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
import com.settings.api.model.UmRoleRef;
import com.settings.api.repository.SettingsDashboardRepository;
import com.settings.api.repository.SettingsDashboardRoleGrantRepository;
import com.settings.api.repository.SettingsDashboardUserGrantRepository;
import com.settings.api.repository.SettingsQueryDefRepository;
import com.settings.api.repository.SettingsWidgetRepository;
import com.settings.api.repository.UmRoleRefRepository;
import com.settings.common.ApiMessages;
import com.settings.exception.ServiceException;
import com.settings.security.BusinessContextHolder;
import java.util.HashSet;
import java.util.Set;

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

	@Autowired
	private UmRoleRefRepository umRoleRefRepository;

	@Transactional
	public DashboardDetailResponse save(DashboardSaveRequest req) {
		if (req.getWidgets() == null) {
			req.setWidgets(Collections.emptyList());
		}

		/*
		 * Tenant scope: an unprivileged caller's saves are always stamped with their business id
		 * (and existing rows must already belong to the same tenant). SUPER_ADMIN (role-level=ADMIN)
		 * can curate global dashboards (business_id = NULL) when no business override is in effect.
		 */
		Long businessId = BusinessContextHolder.currentBusinessId();
		boolean canBypass = BusinessContextHolder.canBypassTenant();

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
				SettingsQueryDef q = loadQueryForCaller(w.getQueryDefId(), businessId, canBypass);
				validationService.validateSelectOnly(q.getSqlText());
			}
		}

		SettingsDashboard d;
		if (req.getId() != null) {
			d = loadDashboardForCaller(req.getId(), businessId, canBypass);
			widgetRepository.deleteByDashboard_Id(d.getId());
			roleGrantRepository.deleteByIdDashboardId(d.getId());
			userGrantRepository.deleteByIdDashboardId(d.getId());
		} else {
			d = new SettingsDashboard();
			if (!canBypass) {
				if (businessId == null) {
					throw new ServiceException(ApiMessages.SETTINGS_DASHBOARD_NOT_FOUND, HttpStatus.FORBIDDEN);
				}
				d.setBusinessId(businessId);
			}
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
				sw.setQueryDef(loadQueryForCaller(wd.getQueryDefId(), businessId, canBypass));
			}
			rows.add(sw);
		}
		widgetRepository.saveAll(rows);

		saveGrants(d.getId(), req.getGrantRoles(), req.getGrantUsernames());

		List<SettingsWidget> loaded = widgetRepository.findForDashboardWithQuery(d.getId());
		SettingsDashboard fresh = loadDashboardForCaller(d.getId(), businessId, canBypass);
		return catalogService.toDetail(fresh, loaded);
	}

	/** Loads a dashboard the current caller is allowed to see — tenant own + globals, or anything when admin-bypassed. */
	private SettingsDashboard loadDashboardForCaller(Long id, Long businessId, boolean canBypass) {
		if (canBypass) {
			return dashboardRepository.findById(id)
					.orElseThrow(() -> new ServiceException(ApiMessages.SETTINGS_DASHBOARD_NOT_FOUND, HttpStatus.NOT_FOUND));
		}
		if (businessId == null) {
			throw new ServiceException(ApiMessages.SETTINGS_DASHBOARD_NOT_FOUND, HttpStatus.NOT_FOUND);
		}
		return dashboardRepository.findByIdForBusinessOrGlobal(id, businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.SETTINGS_DASHBOARD_NOT_FOUND, HttpStatus.NOT_FOUND));
	}

	private SettingsQueryDef loadQueryForCaller(Long id, Long businessId, boolean canBypass) {
		if (canBypass) {
			return queryDefRepository.findById(id)
					.orElseThrow(() -> new ServiceException(ApiMessages.SETTINGS_QUERY_NOT_FOUND, HttpStatus.NOT_FOUND));
		}
		if (businessId == null) {
			throw new ServiceException(ApiMessages.SETTINGS_QUERY_NOT_FOUND, HttpStatus.NOT_FOUND);
		}
		return queryDefRepository.findByIdForBusinessOrGlobal(id, businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.SETTINGS_QUERY_NOT_FOUND, HttpStatus.NOT_FOUND));
	}

	private void saveGrants(Long dashboardId, List<String> roles, List<String> users) {
		if (roles != null) {
			/*
			 * Grants are persisted by role_type (numeric, stable) — not role name. Translate each
			 * supplied name to its UM_ROLE.ROLE_TYPE; skip silently when the role is missing or has no
			 * type configured. Deduplicate so the same type is never inserted twice.
			 */
			Set<Integer> persisted = new HashSet<>();
			for (String r : roles) {
				if (r == null || r.isBlank()) {
					continue;
				}
				String stripped = DashboardAccessEvaluator.normalizeRole(r);
				if (stripped.isEmpty()) {
					continue;
				}
				Optional<UmRoleRef> refOpt = umRoleRefRepository.findFirstByNameIgnoreCaseWithType(stripped);
				Integer type = refOpt.map(UmRoleRef::getRoleType).orElse(null);
				if (type == null || !persisted.add(type)) {
					continue;
				}
				String persistedName = refOpt.map(UmRoleRef::getName).orElse(stripped);
				SettingsDashboardRoleGrant g = new SettingsDashboardRoleGrant();
				SettingsDashboardRoleGrant.GrantId id = new SettingsDashboardRoleGrant.GrantId();
				id.setDashboardId(dashboardId);
				id.setRoleType(type);
				g.setId(id);
				/*
				 * Legacy Oracle installs keep a NOT NULL ROLE_NAME column alongside ROLE_TYPE.
				 * Always stamp the canonical UM_ROLE.NAME so INSERT never violates ORA-01400.
				 */
				g.setRoleName(persistedName);
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
		SettingsDashboard d = loadDashboardForCaller(id, BusinessContextHolder.currentBusinessId(),
				BusinessContextHolder.canBypassTenant());
		widgetRepository.deleteByDashboard_Id(d.getId());
		roleGrantRepository.deleteByIdDashboardId(d.getId());
		userGrantRepository.deleteByIdDashboardId(d.getId());
		dashboardRepository.delete(d);
	}
}
