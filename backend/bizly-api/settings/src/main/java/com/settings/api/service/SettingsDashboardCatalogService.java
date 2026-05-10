package com.settings.api.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.settings.api.dto.dashboard.DashboardDetailResponse;
import com.settings.api.dto.dashboard.DashboardLoadRequest;
import com.settings.api.dto.dashboard.DashboardSummaryResponse;
import com.settings.api.dto.dashboard.WidgetDetailResponse;
import com.settings.api.model.SettingsDashboard;
import com.settings.api.model.SettingsDashboardRoleGrant;
import com.settings.api.model.SettingsDashboardUserGrant;
import com.settings.api.model.SettingsNavPreference;
import com.settings.api.model.SettingsWidget;
import com.settings.api.repository.SettingsDashboardRepository;
import com.settings.api.repository.SettingsDashboardRoleGrantRepository;
import com.settings.api.repository.SettingsDashboardUserGrantRepository;
import com.settings.api.repository.SettingsNavPreferenceRepository;
import com.settings.api.repository.SettingsWidgetRepository;

@Service
public class SettingsDashboardCatalogService {

	@Autowired
	private SettingsDashboardRepository dashboardRepository;

	@Autowired
	private SettingsDashboardRoleGrantRepository roleGrantRepository;

	@Autowired
	private SettingsDashboardUserGrantRepository userGrantRepository;

	@Autowired
	private SettingsNavPreferenceRepository navPreferenceRepository;

	@Autowired
	private SettingsWidgetRepository widgetRepository;

	@Autowired
	private DashboardAccessEvaluator accessEvaluator;

	@Transactional(readOnly = true)
	public List<DashboardSummaryResponse> listForNavbar(String username,
			Collection<? extends GrantedAuthority> authorities) {
		Set<Long> ids = collectGrantedDashboardIds(username, authorities);
		if (ids.isEmpty()) {
			return List.of();
		}
		Map<Long, Boolean> hidden = navPreferenceRepository.findByIdUsernameIgnoreCase(username).stream()
				.collect(Collectors.toMap(p -> p.getId().getDashboardId(), SettingsNavPreference::isHiddenNav,
						(a, b) -> b));

		List<DashboardSummaryResponse> list = new ArrayList<>();
		for (Long id : ids) {
			if (Boolean.TRUE.equals(hidden.get(id))) {
				continue;
			}
			dashboardRepository.findById(id).ifPresent(d -> list.add(toSummary(d)));
		}
		list.sort(Comparator.comparing(DashboardSummaryResponse::getName, String.CASE_INSENSITIVE_ORDER));
		return list;
	}

	@Transactional(readOnly = true)
	public List<DashboardSummaryResponse> listAllDefinitions() {
		return dashboardRepository.findAll().stream().map(this::toSummary)
				.sorted(Comparator.comparing(DashboardSummaryResponse::getName, String.CASE_INSENSITIVE_ORDER))
				.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public DashboardDetailResponse loadForUser(DashboardLoadRequest req, String username,
			Collection<? extends GrantedAuthority> authorities) {
		SettingsDashboard d = null;
		if (req.getId() != null) {
			d = dashboardRepository.findById(req.getId()).orElse(null);
		} else if (req.getSlug() != null && !req.getSlug().isBlank()) {
			d = dashboardRepository.findBySlugIgnoreCase(req.getSlug().trim()).orElse(null);
		}
		if (d == null) {
			return null;
		}
		if (!accessEvaluator.canAccess(username, authorities, d)) {
			return null;
		}
		return toDetail(d, widgetRepository.findForDashboardWithQuery(d.getId()));
	}

	private Set<Long> collectGrantedDashboardIds(String username, Collection<? extends GrantedAuthority> authorities) {
		Set<Long> ids = new HashSet<>();
		for (GrantedAuthority a : authorities) {
			String rn = DashboardAccessEvaluator.normalizeRole(a.getAuthority());
			for (SettingsDashboardRoleGrant g : roleGrantRepository.findGrantsForRole(rn)) {
				ids.add(g.getId().getDashboardId());
			}
		}
		for (SettingsDashboardUserGrant g : userGrantRepository.findByIdUsernameIgnoreCase(username)) {
			ids.add(g.getId().getDashboardId());
		}
		return ids;
	}

	private DashboardSummaryResponse toSummary(SettingsDashboard d) {
		DashboardSummaryResponse s = new DashboardSummaryResponse();
		s.setId(d.getId());
		s.setName(d.getName());
		s.setSlug(d.getSlug());
		s.setDescription(d.getDescription());
		s.setBuiltin(d.isBuiltin());
		return s;
	}

	public DashboardDetailResponse toDetail(SettingsDashboard d, List<SettingsWidget> widgetRows) {
		DashboardDetailResponse r = new DashboardDetailResponse();
		r.setId(d.getId());
		r.setName(d.getName());
		r.setSlug(d.getSlug());
		r.setDescription(d.getDescription());
		r.setLayoutJson(d.getLayoutJson());
		r.setBuiltin(d.isBuiltin());
		List<WidgetDetailResponse> widgets = new ArrayList<>();
		for (SettingsWidget w : widgetRows) {
			widgets.add(toWidgetDetail(w));
		}
		widgets.sort(Comparator.comparingInt(WidgetDetailResponse::getSortOrder));
		r.setWidgets(widgets);
		r.setGrantRoles(roleGrantRepository.findByIdDashboardId(d.getId()).stream()
				.map(g -> g.getId().getRoleName()).collect(Collectors.toList()));
		r.setGrantUsernames(userGrantRepository.findByIdDashboardId(d.getId()).stream()
				.map(g -> g.getId().getUsername()).collect(Collectors.toList()));
		return r;
	}

	private WidgetDetailResponse toWidgetDetail(SettingsWidget w) {
		WidgetDetailResponse x = new WidgetDetailResponse();
		x.setId(w.getId());
		x.setWidgetType(w.getWidgetType());
		x.setTitle(w.getTitle());
		x.setConfigJson(w.getConfigJson());
		x.setGridX(w.getGridX());
		x.setGridY(w.getGridY());
		x.setGridW(w.getGridW());
		x.setGridH(w.getGridH());
		x.setRefreshSec(w.getRefreshSec());
		x.setSortOrder(w.getSortOrder());
		if (w.getQueryDef() != null) {
			x.setQueryDefId(w.getQueryDef().getId());
			x.setQueryName(w.getQueryDef().getName());
		}
		return x;
	}
}
