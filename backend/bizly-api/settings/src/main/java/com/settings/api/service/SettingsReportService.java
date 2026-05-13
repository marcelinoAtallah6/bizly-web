package com.settings.api.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.settings.api.dto.reporting.ActiveReportRef;
import com.settings.api.dto.reporting.ReportBuilderItem;
import com.settings.api.dto.reporting.ReportBuilderSaveRequest;
import com.settings.api.dto.reporting.ReportColumnConfig;
import com.settings.api.model.SettingsQueryDef;
import com.settings.api.model.SettingsReport;
import com.settings.api.model.SettingsReportRoleGrant;
import com.settings.api.model.SettingsReportUserGrant;
import com.settings.api.model.UmRoleRef;
import com.settings.api.reporting.ReportSqlExecutor;
import com.settings.api.repository.SettingsQueryDefRepository;
import com.settings.api.repository.SettingsReportRepository;
import com.settings.api.repository.SettingsReportRoleGrantRepository;
import com.settings.api.repository.SettingsReportUserGrantRepository;
import com.settings.api.repository.UmRoleRefRepository;
import com.settings.common.ApiMessages;
import com.settings.common.PageResponse;
import com.settings.exception.ServiceException;
import com.settings.security.BusinessContextHolder;

/**
 * CRUD + lookup service for the Report Builder.
 *
 * <p>Save flow on create / update:
 * <ol>
 *   <li>Validate inputs (name uniqueness, code uniqueness, linked query exists).</li>
 *   <li>Persist filters + columns as JSON on the entity.</li>
 *   <li>If {@code autoDetectColumns} is true (or columns is empty), the
 *       linked query is introspected with all filter params bound to NULL
 *       to discover the result-set shape, and that becomes the column list.</li>
 * </ol>
 */
@Service
public class SettingsReportService {

	@Autowired
	private SettingsReportRepository reportRepository;

	@Autowired
	private SettingsQueryDefRepository queryDefRepository;

	@Autowired
	private ReportSqlExecutor executor;

	@Autowired
	private SettingsReportRoleGrantRepository roleGrantRepository;

	@Autowired
	private SettingsReportUserGrantRepository userGrantRepository;

	@Autowired
	private UmRoleRefRepository umRoleRefRepository;

	// ---------------- Reads ----------------

	@Transactional(readOnly = true)
	public PageResponse<ReportBuilderItem> list(int pageNumber, int pageSize, String nameSearch) {
		return list(pageNumber, pageSize, nameSearch, null, null);
	}

	/**
	 * Visibility-aware list. When {@code username} is non-null the result is filtered to only
	 * reports the caller can see — admins (role-level=ADMIN via {@link BusinessContextHolder})
	 * see everything. Reports with no grants are treated as global within the tenant.
	 *
	 * The page metadata is rebuilt to reflect post-filter totals so the front-end pagination
	 * does not lie about counts when many rows are hidden.
	 */
	@Transactional(readOnly = true)
	public PageResponse<ReportBuilderItem> list(int pageNumber, int pageSize, String nameSearch,
			String username, Collection<? extends GrantedAuthority> authorities) {
		PageRequest pr = PageRequest.of(Math.max(0, pageNumber), Math.max(1, Math.min(pageSize, 200)));
		String search = (nameSearch != null && !nameSearch.isBlank()) ? nameSearch.trim() : null;

		boolean adminBypass = BusinessContextHolder.canBypassTenant();
		Long businessId = BusinessContextHolder.currentBusinessId();
		// Tenant scope: business callers only see own-business + global reports; admins see everything.
		Page<SettingsReport> page = adminBypass
				? reportRepository.searchByName(search, pr)
				: (businessId != null
						? reportRepository.searchByNameForBusinessOrGlobal(search, businessId, pr)
						: Page.empty(pr));
		Set<Integer> userRoleTypes = adminBypass ? Collections.emptySet() : currentRoleTypes(authorities);

		List<ReportBuilderItem> items = new ArrayList<>();
		for (SettingsReport r : page.getContent()) {
			if (adminBypass || canAccessReport(r.getId(), username, userRoleTypes)) {
				items.add(toItem(r));
			}
		}
		PageResponse<ReportBuilderItem> resp = new PageResponse<>();
		resp.setItems(items);
		resp.setTotalCount(page.getTotalElements());
		resp.setPageNumber(page.getNumber());
		resp.setPageSize(page.getSize());
		resp.setTotalPages(page.getTotalPages());
		return resp;
	}

	/**
	 * Access check: empty grants = global (any authenticated user in the tenant). Otherwise the
	 * caller's role types or username must match at least one grant.
	 */
	private boolean canAccessReport(Long reportId, String username, Set<Integer> userRoleTypes) {
		long roleGrants = roleGrantRepository.countByIdReportId(reportId);
		long userGrants = userGrantRepository.countByIdReportId(reportId);
		if (roleGrants == 0 && userGrants == 0) return true;
		for (SettingsReportRoleGrant g : roleGrantRepository.findByIdReportId(reportId)) {
			if (userRoleTypes.contains(g.getId().getRoleType())) return true;
		}
		if (username != null) {
			for (SettingsReportUserGrant g : userGrantRepository.findByIdReportId(reportId)) {
				if (g.getId().getUsername().equalsIgnoreCase(username)) return true;
			}
		}
		return false;
	}

	private Set<Integer> currentRoleTypes(Collection<? extends GrantedAuthority> authorities) {
		Set<Integer> types = new HashSet<>();
		if (authorities == null) return types;
		for (GrantedAuthority a : authorities) {
			String name = a.getAuthority();
			if (name == null) continue;
			String stripped = name.regionMatches(true, 0, "ROLE_", 0, 5) ? name.substring(5) : name;
			umRoleRefRepository.findFirstByNameIgnoreCaseWithType(stripped)
					.map(UmRoleRef::getRoleType)
					.ifPresent(types::add);
		}
		return types;
	}

	@Transactional(readOnly = true)
	public ReportBuilderItem get(Long id) {
		return toItem(loadReportForCaller(id));
	}

	@Transactional(readOnly = true)
	public SettingsReport getEntity(Long id) {
		return loadReportForCaller(id);
	}

	@Transactional(readOnly = true)
	public SettingsReport getEntityByCode(String code) {
		// Tenant scope: business callers can resolve global + own-business codes; admins resolve any.
		if (BusinessContextHolder.canBypassTenant()) {
			return reportRepository.findByCode(code)
					.orElseThrow(() -> new ServiceException(ApiMessages.REPORTING_REPORT_NOT_FOUND, HttpStatus.NOT_FOUND));
		}
		Long businessId = BusinessContextHolder.currentBusinessId();
		if (businessId == null) {
			throw new ServiceException(ApiMessages.REPORTING_REPORT_NOT_FOUND, HttpStatus.NOT_FOUND);
		}
		return reportRepository.findByCodeForBusinessOrGlobal(code, businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.REPORTING_REPORT_NOT_FOUND, HttpStatus.NOT_FOUND));
	}

	private SettingsReport loadReportForCaller(Long id) {
		if (BusinessContextHolder.canBypassTenant()) {
			return reportRepository.findById(id)
					.orElseThrow(() -> new ServiceException(ApiMessages.REPORTING_REPORT_NOT_FOUND, HttpStatus.NOT_FOUND));
		}
		Long businessId = BusinessContextHolder.currentBusinessId();
		if (businessId == null) {
			throw new ServiceException(ApiMessages.REPORTING_REPORT_NOT_FOUND, HttpStatus.NOT_FOUND);
		}
		return reportRepository.findByIdForBusinessOrGlobal(id, businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.REPORTING_REPORT_NOT_FOUND, HttpStatus.NOT_FOUND));
	}

	@Transactional(readOnly = true)
	public List<ActiveReportRef> listActiveForSidebar() {
		return listActiveForSidebar(null, null);
	}

	/**
	 * Visibility-aware sidebar list. Admins see every ACTIVE report; everyone else only sees
	 * reports they have been granted access to (or reports with no grants = global).
	 */
	@Transactional(readOnly = true)
	public List<ActiveReportRef> listActiveForSidebar(String username,
			Collection<? extends GrantedAuthority> authorities) {
		boolean adminBypass = BusinessContextHolder.canBypassTenant();
		Long businessId = BusinessContextHolder.currentBusinessId();
		// Tenant scope: own + global for business callers; everything for admin; nothing if neither.
		List<SettingsReport> rows;
		if (adminBypass) {
			rows = reportRepository.findByStatusOrderBySortOrderAscNameAsc("ACTIVE");
		} else if (businessId != null) {
			rows = reportRepository.findByStatusForBusinessOrGlobal("ACTIVE", businessId);
		} else {
			rows = Collections.emptyList();
		}
		if (rows == null || rows.isEmpty()) return Collections.emptyList();
		Set<Integer> userRoleTypes = adminBypass ? Collections.emptySet() : currentRoleTypes(authorities);
		List<ActiveReportRef> out = new ArrayList<>(rows.size());
		for (SettingsReport r : rows) {
			if (!adminBypass && !canAccessReport(r.getId(), username, userRoleTypes)) continue;
			ActiveReportRef ref = new ActiveReportRef();
			ref.setId(r.getId());
			ref.setCode(r.getCode());
			ref.setName(r.getName());
			ref.setDescription(r.getDescription());
			ref.setIcon(r.getIcon());
			ref.setSortOrder(r.getSortOrder());
			ref.setRoute("/reporting/run/" + r.getId());
			out.add(ref);
		}
		return out;
	}

	// ---------------- Writes ----------------

	@Transactional
	public ReportBuilderItem save(ReportBuilderSaveRequest req, String currentUsername) {
		if (req == null) {
			throw new ServiceException(ApiMessages.REPORTING_INVALID, HttpStatus.BAD_REQUEST);
		}

		/*
		 * Tenant scope: an unprivileged caller's save is stamped with their business id and the
		 * linked query must be either their own or a global template. SUPER_ADMIN can publish
		 * global reports (business_id = NULL) when no override is in effect.
		 */
		boolean canBypass = BusinessContextHolder.canBypassTenant();
		Long businessId = BusinessContextHolder.currentBusinessId();

		SettingsQueryDef qd;
		if (canBypass) {
			qd = queryDefRepository.findById(req.getQueryDefId())
					.orElseThrow(() -> new ServiceException(ApiMessages.SETTINGS_QUERY_NOT_FOUND, HttpStatus.BAD_REQUEST));
		} else {
			if (businessId == null) {
				throw new ServiceException(ApiMessages.SETTINGS_QUERY_NOT_FOUND, HttpStatus.BAD_REQUEST);
			}
			qd = queryDefRepository.findByIdForBusinessOrGlobal(req.getQueryDefId(), businessId)
					.orElseThrow(() -> new ServiceException(ApiMessages.SETTINGS_QUERY_NOT_FOUND, HttpStatus.BAD_REQUEST));
		}

		SettingsReport entity;
		if (req.getId() != null) {
			entity = loadReportForCaller(req.getId());
		} else {
			entity = new SettingsReport();
			if (!canBypass) {
				entity.setBusinessId(businessId);
			}
		}

		String trimmedName = req.getName() != null ? req.getName().trim() : null;
		if (trimmedName == null || trimmedName.isEmpty()) {
			throw new ServiceException(ApiMessages.REPORTING_INVALID, HttpStatus.BAD_REQUEST);
		}

		String code = req.getCode() != null && !req.getCode().isBlank()
				? slugify(req.getCode().trim())
				: slugify(trimmedName);

		if (entity.getId() == null) {
			if (reportRepository.existsByNameIgnoreCase(trimmedName)) {
				throw new ServiceException(ApiMessages.REPORTING_NAME_EXISTS, HttpStatus.CONFLICT);
			}
			if (reportRepository.existsByCodeIgnoreCase(code)) {
				throw new ServiceException(ApiMessages.REPORTING_CODE_EXISTS, HttpStatus.CONFLICT);
			}
		} else {
			if (reportRepository.existsByNameIgnoreCaseAndIdNot(trimmedName, entity.getId())) {
				throw new ServiceException(ApiMessages.REPORTING_NAME_EXISTS, HttpStatus.CONFLICT);
			}
			if (reportRepository.existsByCodeIgnoreCaseAndIdNot(code, entity.getId())) {
				throw new ServiceException(ApiMessages.REPORTING_CODE_EXISTS, HttpStatus.CONFLICT);
			}
		}

		entity.setCode(code);
		entity.setName(trimmedName);
		entity.setDescription(req.getDescription());
		entity.setIcon(req.getIcon() != null && !req.getIcon().isBlank() ? req.getIcon().trim() : "report");
		entity.setStatus(normaliseStatus(req.getStatus()));
		entity.setQueryDefId(qd.getId());
		entity.setFiltersJson(executor.filtersToJson(req.getFilters()));

		List<ReportColumnConfig> columns = req.getColumns();
		boolean autoDetect = Boolean.TRUE.equals(req.getAutoDetectColumns())
				|| columns == null || columns.isEmpty();
		if (autoDetect) {
			columns = executor.introspectColumns(qd.getSqlText(), req.getFilters());
		}
		entity.setColumnsJson(executor.columnsToJson(columns));

		entity.setDefaultSortKey(req.getDefaultSortKey());
		entity.setDefaultSortDir(normaliseDir(req.getDefaultSortDir()));
		entity.setSortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0);

		if (entity.getId() == null) {
			entity.setCreatedBy(currentUsername);
		}
		entity.setUpdatedBy(currentUsername);
		entity.setUpdatedAt(Instant.now());

		SettingsReport saved = reportRepository.save(entity);
		persistGrants(saved.getId(), req.getGrantRoles(), req.getGrantUsernames());
		return toItem(saved);
	}

	/**
	 * Replaces all role + user grants for the report. Roles are resolved by name via
	 * {@link UmRoleRefRepository} so the persisted row keeps the stable {@code role_type}.
	 * Unknown role names are silently dropped (consistent with the dashboard builder).
	 */
	private void persistGrants(Long reportId, List<String> roles, List<String> usernames) {
		roleGrantRepository.deleteByIdReportId(reportId);
		userGrantRepository.deleteByIdReportId(reportId);
		if (roles != null) {
			Set<Integer> seen = new HashSet<>();
			for (String name : roles) {
				if (name == null || name.isBlank()) continue;
				Integer rt = umRoleRefRepository.findFirstByNameIgnoreCaseWithType(name.trim())
						.map(UmRoleRef::getRoleType).orElse(null);
				if (rt == null || !seen.add(rt)) continue;
				SettingsReportRoleGrant g = new SettingsReportRoleGrant();
				SettingsReportRoleGrant.GrantId gid = new SettingsReportRoleGrant.GrantId();
				gid.setReportId(reportId);
				gid.setRoleType(rt);
				g.setId(gid);
				roleGrantRepository.save(g);
			}
		}
		if (usernames != null) {
			Set<String> seen = new HashSet<>();
			for (String u : usernames) {
				if (u == null || u.isBlank()) continue;
				String trimmed = u.trim();
				if (!seen.add(trimmed.toLowerCase())) continue;
				SettingsReportUserGrant g = new SettingsReportUserGrant();
				SettingsReportUserGrant.UserGrantId gid = new SettingsReportUserGrant.UserGrantId();
				gid.setReportId(reportId);
				gid.setUsername(trimmed);
				g.setId(gid);
				userGrantRepository.save(g);
			}
		}
	}

	@Transactional
	public void delete(Long id) {
		SettingsReport r = loadReportForCaller(id);
		reportRepository.delete(r);
	}

	// ---------------- Mapping ----------------

	private ReportBuilderItem toItem(SettingsReport r) {
		ReportBuilderItem item = new ReportBuilderItem();
		item.setId(r.getId());
		item.setCode(r.getCode());
		item.setName(r.getName());
		item.setDescription(r.getDescription());
		item.setIcon(r.getIcon());
		item.setStatus(r.getStatus());
		item.setQueryDefId(r.getQueryDefId());
		item.setFilters(executor.parseFilters(r.getFiltersJson()));
		item.setColumns(executor.parseColumns(r.getColumnsJson()));
		item.setDefaultSortKey(r.getDefaultSortKey());
		item.setDefaultSortDir(r.getDefaultSortDir());
		item.setSortOrder(r.getSortOrder());
		item.setCreatedAt(r.getCreatedAt());
		item.setUpdatedAt(r.getUpdatedAt());
		item.setCreatedBy(r.getCreatedBy());
		item.setUpdatedBy(r.getUpdatedBy());
		queryDefRepository.findById(r.getQueryDefId()).ifPresent(q -> item.setQueryDefName(q.getName()));

		// Translate role_type back to role names for the UI (matches how the user picked them).
		List<SettingsReportRoleGrant> grants = roleGrantRepository.findByIdReportId(r.getId());
		List<String> grantRoles = new ArrayList<>();
		if (!grants.isEmpty()) {
			List<Integer> roleTypes = grants.stream().map(g -> g.getId().getRoleType())
					.collect(java.util.stream.Collectors.toList());
			for (UmRoleRef ref : umRoleRefRepository.findByRoleTypeIn(roleTypes)) {
				grantRoles.add(ref.getName());
			}
		}
		item.setGrantRoles(grantRoles);

		List<String> grantUsernames = new ArrayList<>();
		for (SettingsReportUserGrant g : userGrantRepository.findByIdReportId(r.getId())) {
			grantUsernames.add(g.getId().getUsername());
		}
		item.setGrantUsernames(grantUsernames);

		return item;
	}

	private String normaliseStatus(String s) {
		if (s == null) return "ACTIVE";
		String u = s.trim().toUpperCase();
		return "INACTIVE".equals(u) ? "INACTIVE" : "ACTIVE";
	}

	private String normaliseDir(String d) {
		if (d == null || d.isBlank()) return "DESC";
		String u = d.trim().toUpperCase();
		return "ASC".equals(u) ? "ASC" : "DESC";
	}

	/**
	 * Generates a URL-safe slug. Keeps lowercase ASCII + digits + dashes, with
	 * a fallback to ensure non-blank output.
	 */
	private String slugify(String input) {
		if (input == null) return "report";
		String slug = input.trim().toLowerCase()
				.replaceAll("[^a-z0-9]+", "-")
				.replaceAll("^-+", "")
				.replaceAll("-+$", "");
		if (slug.length() > 120) {
			slug = slug.substring(0, 120);
		}
		return slug.isEmpty() ? "report" : slug;
	}
}
