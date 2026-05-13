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
import com.settings.api.model.UmRoleRef;
import com.settings.api.repository.SettingsDashboardRepository;
import com.settings.api.repository.SettingsDashboardRoleGrantRepository;
import com.settings.api.repository.SettingsDashboardUserGrantRepository;
import com.settings.api.repository.SettingsNavPreferenceRepository;
import com.settings.api.repository.SettingsWidgetRepository;
import com.settings.api.repository.UmRoleRefRepository;
import com.settings.security.BusinessContextHolder;

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

	@Autowired
	private UmRoleRefRepository umRoleRefRepository;

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

		/*
		 * Tenant scope: even if a grant accidentally references a dashboard from another tenant
		 * (e.g. a SUPER_ADMIN seeded a cross-tenant grant), business users must NOT see it. Admins
		 * see everything.
		 */
		boolean canBypass = BusinessContextHolder.canBypassTenant();
		Long businessId = BusinessContextHolder.currentBusinessId();

		List<DashboardSummaryResponse> list = new ArrayList<>();
		for (Long id : ids) {
			if (Boolean.TRUE.equals(hidden.get(id))) {
				continue;
			}
			java.util.Optional<SettingsDashboard> found = canBypass
					? dashboardRepository.findById(id)
					: (businessId != null
							? dashboardRepository.findByIdForBusinessOrGlobal(id, businessId)
							: java.util.Optional.empty());
			found.ifPresent(d -> list.add(toSummary(d)));
		}
		list.sort(Comparator.comparing(DashboardSummaryResponse::getName, String.CASE_INSENSITIVE_ORDER));
		return list;
	}

	@Transactional(readOnly = true)
	public List<DashboardSummaryResponse> listAllDefinitions() {
		/*
		 * Tenant scope: SUPER_ADMIN sees every definition across all tenants. Business callers see
		 * their own dashboards PLUS the global built-in catalogs. Without this, the admin "All
		 * definitions" screen would leak rows across businesses.
		 */
		List<SettingsDashboard> source;
		if (BusinessContextHolder.canBypassTenant()) {
			source = dashboardRepository.findAll();
		} else {
			Long businessId = BusinessContextHolder.currentBusinessId();
			source = businessId == null
					? List.of()
					: dashboardRepository.findAllForBusinessOrGlobal(businessId);
		}
		return source.stream().map(this::toSummary)
				.sorted(Comparator.comparing(DashboardSummaryResponse::getName, String.CASE_INSENSITIVE_ORDER))
				.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public SettingsDashboard resolveDashboard(DashboardLoadRequest req) {
		boolean canBypass = BusinessContextHolder.canBypassTenant();
		Long businessId = BusinessContextHolder.currentBusinessId();
		if (req.getId() != null) {
			if (canBypass) {
				return dashboardRepository.findById(req.getId()).orElse(null);
			}
			if (businessId == null) {
				return null;
			}
			return dashboardRepository.findByIdForBusinessOrGlobal(req.getId(), businessId).orElse(null);
		}
		if (req.getSlug() != null && !req.getSlug().isBlank()) {
			String slug = req.getSlug().trim();
			if (canBypass) {
				return dashboardRepository.findBySlugIgnoreCase(slug).orElse(null);
			}
			if (businessId == null) {
				return null;
			}
			return dashboardRepository.findBySlugForBusinessOrGlobal(slug, businessId).orElse(null);
		}
		return null;
	}

	@Transactional(readOnly = true)
	public DashboardDetailResponse loadForUser(DashboardLoadRequest req, String username,
			Collection<? extends GrantedAuthority> authorities) {
		SettingsDashboard d = resolveDashboard(req);
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
		for (Integer roleType : accessEvaluator.currentRoleTypes(authorities)) {
			for (SettingsDashboardRoleGrant g : roleGrantRepository.findByIdRoleType(roleType)) {
				ids.add(g.getId().getDashboardId());
			}
		}
		for (SettingsDashboardUserGrant g : userGrantRepository.findByIdUsernameIgnoreCase(username)) {
			ids.add(g.getId().getDashboardId());
		}
		/*
		 * Include dashboards that have no grants at all — these are treated as public by
		 * {@link DashboardAccessEvaluator#canAccess(String, Collection, SettingsDashboard)} so the navbar
		 * list must surface them too. Without this the dashboard exists but is invisible to the user.
		 *
		 * Tenant scope: business users see their own + global dashboards. Admin (role-level=ADMIN) sees
		 * every dashboard regardless of tenant.
		 */
		boolean canBypass = BusinessContextHolder.canBypassTenant();
		Long businessId = BusinessContextHolder.currentBusinessId();
		List<SettingsDashboard> source;
		if (canBypass) {
			source = dashboardRepository.findAll();
		} else if (businessId != null) {
			source = dashboardRepository.findAllForBusinessOrGlobal(businessId);
		} else {
			source = List.of();
		}
		for (SettingsDashboard d : source) {
			if (roleGrantRepository.countByIdDashboardId(d.getId()) == 0
					&& userGrantRepository.countByIdDashboardId(d.getId()) == 0) {
				ids.add(d.getId());
			}
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
		/*
		 * Translate stored role_type → current UM_ROLE.NAME so the UI sees the *live* name even after
		 * a rename. Grants no longer carry the name; type is the source of truth.
		 */
		List<Integer> grantedTypes = roleGrantRepository.findByIdDashboardId(d.getId()).stream()
				.map(g -> g.getId().getRoleType()).collect(Collectors.toList());
		List<String> grantRoleNames;
		if (grantedTypes.isEmpty()) {
			grantRoleNames = List.of();
		} else {
			Map<Integer, String> typeToName = umRoleRefRepository.findByRoleTypeIn(grantedTypes).stream()
					.collect(Collectors.toMap(UmRoleRef::getRoleType, UmRoleRef::getName, (a, b) -> a));
			grantRoleNames = grantedTypes.stream().map(t -> typeToName.getOrDefault(t, "ROLE_TYPE_" + t))
					.collect(Collectors.toList());
		}
		r.setGrantRoles(grantRoleNames);
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
