package com.settings.api.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.settings.api.dto.query.QueryDefGetsRequest;
import com.settings.api.dto.query.QueryDefIdRequest;
import com.settings.api.dto.query.QueryDefResponse;
import com.settings.api.dto.query.QueryDefSaveRequest;
import com.settings.api.model.SettingsQueryDef;
import com.settings.api.model.SettingsQueryDefRoleGrant;
import com.settings.api.model.SettingsQueryDefUserGrant;
import com.settings.api.model.UmRoleRef;
import com.settings.api.repository.SettingsQueryDefRepository;
import com.settings.api.repository.SettingsQueryDefRoleGrantRepository;
import com.settings.api.repository.SettingsQueryDefUserGrantRepository;
import com.settings.api.repository.UmRoleRefRepository;
import com.settings.common.ApiMessages;
import com.settings.common.PageResponse;
import com.settings.exception.ServiceException;
import com.settings.security.BusinessContextHolder;

@Service
public class SettingsQueryDefService {

	@Autowired
	private SettingsQueryDefRepository queryDefRepository;

	@Autowired
	private SqlQueryValidationService validationService;

	@Autowired
	private SettingsQueryDefRoleGrantRepository roleGrantRepository;

	@Autowired
	private SettingsQueryDefUserGrantRepository userGrantRepository;

	@Autowired
	private UmRoleRefRepository umRoleRefRepository;

	public PageResponse<QueryDefResponse> listPage(QueryDefGetsRequest req) {
		return listPage(req, null, null);
	}

	/**
	 * Visibility-aware variant. When {@code username} is non-null the result is filtered to
	 * queries the caller is granted access to. Admins (role-level=ADMIN) bypass the filter.
	 * Queries with no grants are global within the tenant.
	 */
	public PageResponse<QueryDefResponse> listPage(QueryDefGetsRequest req, String username,
			Collection<? extends GrantedAuthority> authorities) {
		Pageable pageable = org.springframework.data.domain.PageRequest.of(req.getPageNumber(), req.getPageSize(),
				Sort.by(Sort.Direction.ASC, "name"));
		String fragment = req.getNameSearch() == null ? "" : req.getNameSearch().trim();

		boolean adminBypass = BusinessContextHolder.canBypassTenant();
		Long businessId = BusinessContextHolder.currentBusinessId();
		// Tenant scope: business callers see own + global query defs; admin sees everything.
		Page<SettingsQueryDef> page;
		if (adminBypass) {
			page = fragment.isEmpty()
					? queryDefRepository.findAll(pageable)
					: queryDefRepository.findByNameContainingIgnoreCase(fragment, pageable);
		} else if (businessId != null) {
			page = fragment.isEmpty()
					? queryDefRepository.findAllForBusinessOrGlobal(businessId, pageable)
					: queryDefRepository.findByNameForBusinessOrGlobal(fragment, businessId, pageable);
		} else {
			page = Page.empty(pageable);
		}

		Set<Integer> userRoleTypes = adminBypass ? Collections.emptySet() : currentRoleTypes(authorities);

		List<QueryDefResponse> items = new ArrayList<>();
		for (SettingsQueryDef q : page.getContent()) {
			if (adminBypass || canAccessQuery(q.getId(), username, userRoleTypes)) {
				items.add(toResponse(q));
			}
		}
		PageResponse<QueryDefResponse> out = new PageResponse<>();
		out.setItems(items);
		out.setTotalCount(page.getTotalElements());
		out.setPageNumber(page.getNumber());
		out.setPageSize(page.getSize());
		out.setTotalPages(page.getTotalPages());
		return out;
	}

	private boolean canAccessQuery(Long queryDefId, String username, Set<Integer> userRoleTypes) {
		long roleGrants = roleGrantRepository.countByIdQueryDefId(queryDefId);
		long userGrants = userGrantRepository.countByIdQueryDefId(queryDefId);
		if (roleGrants == 0 && userGrants == 0) return true;
		for (SettingsQueryDefRoleGrant g : roleGrantRepository.findByIdQueryDefId(queryDefId)) {
			if (userRoleTypes.contains(g.getId().getRoleType())) return true;
		}
		if (username != null) {
			for (SettingsQueryDefUserGrant g : userGrantRepository.findByIdQueryDefId(queryDefId)) {
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

	public QueryDefResponse get(QueryDefIdRequest req) {
		return toResponse(loadQueryForCaller(req.getId()));
	}

	private SettingsQueryDef loadQueryForCaller(Long id) {
		if (BusinessContextHolder.canBypassTenant()) {
			return queryDefRepository.findById(id)
					.orElseThrow(() -> new ServiceException(ApiMessages.SETTINGS_QUERY_NOT_FOUND, HttpStatus.NOT_FOUND));
		}
		Long businessId = BusinessContextHolder.currentBusinessId();
		if (businessId == null) {
			throw new ServiceException(ApiMessages.SETTINGS_QUERY_NOT_FOUND, HttpStatus.NOT_FOUND);
		}
		return queryDefRepository.findByIdForBusinessOrGlobal(id, businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.SETTINGS_QUERY_NOT_FOUND, HttpStatus.NOT_FOUND));
	}

	@Transactional
	public QueryDefResponse save(QueryDefSaveRequest req, String username) {
		validationService.validateSelectOnly(req.getSqlText());

		/*
		 * Tenant scope: business callers' saves are stamped with their business id; SUPER_ADMIN can
		 * curate global templates (business_id = NULL) when no override is in effect.
		 */
		boolean canBypass = BusinessContextHolder.canBypassTenant();
		Long businessId = BusinessContextHolder.currentBusinessId();

		SettingsQueryDef entity;
		if (req.getId() != null) {
			entity = loadQueryForCaller(req.getId());
		} else {
			entity = new SettingsQueryDef();
			if (!canBypass) {
				entity.setBusinessId(businessId);
			}
			if (queryDefRepository.existsByNameIgnoreCase(req.getName())) {
				throw new ServiceException(ApiMessages.SETTINGS_QUERY_NAME_EXISTS, HttpStatus.CONFLICT);
			}
		}
		entity.setName(req.getName().trim());
		entity.setDescription(req.getDescription());
		entity.setSqlText(req.getSqlText());
		entity.setParametersJson(req.getParametersJson());
		if (entity.getCreatedBy() == null && username != null) {
			entity.setCreatedBy(username);
		}
		entity = queryDefRepository.save(entity);
		persistGrants(entity.getId(), req.getGrantRoles(), req.getGrantUsernames());
		return toResponse(entity);
	}

	private void persistGrants(Long queryDefId, List<String> roles, List<String> usernames) {
		roleGrantRepository.deleteByIdQueryDefId(queryDefId);
		userGrantRepository.deleteByIdQueryDefId(queryDefId);
		if (roles != null) {
			Set<Integer> seen = new HashSet<>();
			for (String name : roles) {
				if (name == null || name.isBlank()) continue;
				Integer rt = umRoleRefRepository.findFirstByNameIgnoreCaseWithType(name.trim())
						.map(UmRoleRef::getRoleType).orElse(null);
				if (rt == null || !seen.add(rt)) continue;
				SettingsQueryDefRoleGrant g = new SettingsQueryDefRoleGrant();
				SettingsQueryDefRoleGrant.GrantId gid = new SettingsQueryDefRoleGrant.GrantId();
				gid.setQueryDefId(queryDefId);
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
				SettingsQueryDefUserGrant g = new SettingsQueryDefUserGrant();
				SettingsQueryDefUserGrant.UserGrantId gid = new SettingsQueryDefUserGrant.UserGrantId();
				gid.setQueryDefId(queryDefId);
				gid.setUsername(trimmed);
				g.setId(gid);
				userGrantRepository.save(g);
			}
		}
	}

	@Transactional
	public void delete(QueryDefIdRequest req) {
		SettingsQueryDef entity = loadQueryForCaller(req.getId());
		queryDefRepository.delete(entity);
	}

	private QueryDefResponse toResponse(SettingsQueryDef q) {
		QueryDefResponse r = new QueryDefResponse();
		r.setId(q.getId());
		r.setName(q.getName());
		r.setDescription(q.getDescription());
		r.setSqlText(q.getSqlText());
		r.setParametersJson(q.getParametersJson());
		r.setCreatedAt(q.getCreatedAt());
		r.setUpdatedAt(q.getUpdatedAt());
		r.setCreatedBy(q.getCreatedBy());

		List<SettingsQueryDefRoleGrant> grants = roleGrantRepository.findByIdQueryDefId(q.getId());
		List<String> grantRoles = new ArrayList<>();
		if (!grants.isEmpty()) {
			List<Integer> roleTypes = grants.stream().map(g -> g.getId().getRoleType()).collect(Collectors.toList());
			for (UmRoleRef ref : umRoleRefRepository.findByRoleTypeIn(roleTypes)) {
				grantRoles.add(ref.getName());
			}
		}
		r.setGrantRoles(grantRoles);

		List<String> grantUsers = new ArrayList<>();
		for (SettingsQueryDefUserGrant g : userGrantRepository.findByIdQueryDefId(q.getId())) {
			grantUsers.add(g.getId().getUsername());
		}
		r.setGrantUsernames(grantUsers);

		return r;
	}
}
