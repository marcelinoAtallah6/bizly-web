package com.um.api.service.catalog;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.um.api.dto.catalog.ApplicationCatalogResponse;
import com.um.api.dto.catalog.CatalogApplicationDto;
import com.um.api.dto.catalog.CatalogIdRequest;
import com.um.api.dto.catalog.CatalogMenuDto;
import com.um.api.dto.catalog.CatalogSaveResponse;
import com.um.api.dto.catalog.ReorderCatalogRequest;
import com.um.api.dto.catalog.SaveApplicationCatalogRequest;
import com.um.api.dto.catalog.SaveMenuCatalogRequest;
import com.um.api.model.menu.UmApplication;
import com.um.api.model.menu.UmMenu;
import com.um.api.repository.menu.UmApplicationRepository;
import com.um.api.repository.menu.UmMenuRepository;
import com.um.api.repository.role.RoleMenuPermissionRepository;
import com.um.api.service.menu.MenuTreeSupport;
import com.um.exception.ServiceException;
import com.um.security.BusinessContextHolder;

@Service
public class ApplicationCatalogServiceImpl implements IApplicationCatalogService {

	private static final int SORT_STEP = 10;

	@Autowired
	private UmApplicationRepository applicationRepository;

	@Autowired
	private UmMenuRepository menuRepository;

	@Autowired
	private RoleMenuPermissionRepository roleMenuPermissionRepository;

	@Override
	@Transactional(readOnly = true)
	public ApplicationCatalogResponse getCatalog() {
		assertPortalRootAdmin();
		ApplicationCatalogResponse out = new ApplicationCatalogResponse();
		List<UmApplication> apps = applicationRepository.findAllByOrderBySortOrderAscNameAsc();
		for (UmApplication app : apps) {
			CatalogApplicationDto dto = toApplicationDto(app);
			dto.setMenus(buildMenuTree(menuRepository.findByApplicationIdOrderBySortOrderAscNameAsc(app.getId()),
					app.getId()));
			out.getApplications().add(dto);
		}
		return out;
	}

	@Override
	@Transactional
	public CatalogSaveResponse saveApplication(SaveApplicationCatalogRequest request) {
		assertPortalRootAdmin();
		UmApplication entity;
		if (request.getId() != null) {
			entity = applicationRepository.findById(request.getId())
					.orElseThrow(() -> new ServiceException("Application not found", HttpStatus.NOT_FOUND));
		} else {
			entity = new UmApplication();
			entity.setSortOrder(nextApplicationSortOrder());
		}
		entity.setName(request.getName().trim());
		entity.setDescription(trimToNull(request.getDescription()));
		entity.setIcon(normalizeIcon(request.getIcon()));
		entity.setRoute(normalizeRoute(request.getRoute()));
		entity.setIsActive(request.getIsActive() == null ? Boolean.TRUE : request.getIsActive());
		entity.setAllowedRoles(null);
		if (request.getSortOrder() != null) {
			entity.setSortOrder(request.getSortOrder());
		}
		applicationRepository.save(entity);
		return new CatalogSaveResponse(entity.getId());
	}

	@Override
	@Transactional
	public CatalogSaveResponse saveMenu(SaveMenuCatalogRequest request) {
		assertPortalRootAdmin();
		UmApplication app = applicationRepository.findById(request.getApplicationId())
				.orElseThrow(() -> new ServiceException("Application not found", HttpStatus.NOT_FOUND));

		String route = normalizeRoute(request.getRoute());
		assertRouteUnique(route, request.getId());

		UmMenu entity;
		LocalDateTime now = LocalDateTime.now();
		if (request.getId() != null) {
			entity = menuRepository.findById(request.getId())
					.orElseThrow(() -> new ServiceException("Menu not found", HttpStatus.NOT_FOUND));
			entity.setUpdatedAt(now);
		} else {
			entity = new UmMenu();
			entity.setCreatedAt(now);
			entity.setUpdatedAt(now);
			entity.setSortOrder(
					request.getSortOrder() != null ? request.getSortOrder() : nextMenuSortOrder(app.getId()));
		}
		entity.setApplication(app);
		entity.setName(request.getName().trim());
		entity.setRoute(route);
		entity.setIcon(normalizeIcon(request.getIcon()));
		entity.setIsActive(request.getIsActive() == null ? Boolean.TRUE : request.getIsActive());
		entity.setAllowedRoles(null);
		if (request.getSortOrder() != null) {
			entity.setSortOrder(request.getSortOrder());
		}

		if (request.getParentId() != null) {
			UmMenu parent = menuRepository.findById(request.getParentId())
					.orElseThrow(() -> new ServiceException("Parent menu not found", HttpStatus.BAD_REQUEST));
			if (!parent.getApplication().getId().equals(app.getId())) {
				throw new ServiceException("Parent menu must belong to the same application", HttpStatus.BAD_REQUEST);
			}
			entity.setParentMenu(parent);
		} else {
			entity.setParentMenu(null);
		}

		menuRepository.save(entity);
		return new CatalogSaveResponse(entity.getId());
	}

	@Override
	@Transactional
	public void deleteApplication(CatalogIdRequest request) {
		assertPortalRootAdmin();
		UmApplication app = applicationRepository.findById(request.getId())
				.orElseThrow(() -> new ServiceException("Application not found", HttpStatus.NOT_FOUND));
		List<UmMenu> menus = menuRepository.findByApplicationIdOrderBySortOrderAscNameAsc(app.getId());
		for (UmMenu menu : menus) {
			deleteMenuRecursive(menu.getId());
		}
		applicationRepository.delete(app);
	}

	@Override
	@Transactional
	public void deleteMenu(CatalogIdRequest request) {
		assertPortalRootAdmin();
		deleteMenuRecursive(request.getId());
	}

	@Override
	@Transactional
	public void reorder(ReorderCatalogRequest request) {
		assertPortalRootAdmin();
		String scope = request.getScope() == null ? "" : request.getScope().trim().toUpperCase(Locale.ROOT);
		List<Long> ids = request.getOrderedIds();
		int order = SORT_STEP;
		if ("APPLICATION".equals(scope)) {
			for (Long id : ids) {
				UmApplication app = applicationRepository.findById(id).orElseThrow(
						() -> new ServiceException("Application not found: " + id, HttpStatus.BAD_REQUEST));
				app.setSortOrder(order);
				order += SORT_STEP;
				applicationRepository.save(app);
			}
			return;
		}
		if ("MENU".equals(scope)) {
			if (request.getApplicationId() == null) {
				throw new ServiceException("applicationId is required for menu reorder", HttpStatus.BAD_REQUEST);
			}
			Long expectedParentId = request.getParentId();
			for (Long id : ids) {
				UmMenu menu = menuRepository.findById(id)
						.orElseThrow(() -> new ServiceException("Menu not found: " + id, HttpStatus.BAD_REQUEST));
				if (!menu.getApplication().getId().equals(request.getApplicationId())) {
					throw new ServiceException(
							"Menu " + id + " does not belong to application " + request.getApplicationId(),
							HttpStatus.BAD_REQUEST);
				}
				Long actualParentId = menu.getParentMenu() != null ? menu.getParentMenu().getId() : null;
				if (!Objects.equals(expectedParentId, actualParentId)) {
					throw new ServiceException(
							"Menu " + id + " is not a sibling under parent " + expectedParentId,
							HttpStatus.BAD_REQUEST);
				}
				menu.setSortOrder(order);
				order += SORT_STEP;
				menuRepository.save(menu);
			}
			return;
		}
		throw new ServiceException("scope must be APPLICATION or MENU", HttpStatus.BAD_REQUEST);
	}

	private void deleteMenuRecursive(Long menuId) {
		UmMenu menu = menuRepository.findById(menuId)
				.orElseThrow(() -> new ServiceException("Menu not found", HttpStatus.NOT_FOUND));
		List<UmMenu> children = menuRepository
				.findByApplicationIdOrderBySortOrderAscNameAsc(menu.getApplication().getId());
		for (UmMenu child : children) {
			if (child.getParentMenu() != null && menuId.equals(child.getParentMenu().getId())) {
				deleteMenuRecursive(child.getId());
			}
		}
		roleMenuPermissionRepository.deleteByIdMenuId(menuId);
		menuRepository.delete(menu);
	}

	private List<CatalogMenuDto> buildMenuTree(List<UmMenu> flat, Long applicationId) {
		List<CatalogMenuDto> roots = MenuTreeSupport.buildTree(flat, m -> true, m -> toMenuDto(m, applicationId),
				CatalogMenuDto::getChildren);
		MenuTreeSupport.sortTree(roots, catalogMenuSortComparator(), CatalogMenuDto::getChildren);
		return roots;
	}

	private static Comparator<CatalogMenuDto> catalogMenuSortComparator() {
		return Comparator
				.comparing(CatalogMenuDto::getSortOrder, Comparator.nullsLast(Integer::compareTo))
				.thenComparing(CatalogMenuDto::getName, Comparator.nullsLast(String::compareToIgnoreCase));
	}

	private static CatalogApplicationDto toApplicationDto(UmApplication app) {
		CatalogApplicationDto dto = new CatalogApplicationDto();
		dto.setId(app.getId());
		dto.setName(app.getName());
		dto.setDescription(app.getDescription());
		dto.setIcon(app.getIcon());
		dto.setRoute(app.getRoute());
		dto.setIsActive(app.getIsActive());
		dto.setAllowedRoles(app.getAllowedRoles());
		dto.setSortOrder(app.getSortOrder());
		return dto;
	}

	private static CatalogMenuDto toMenuDto(UmMenu m, Long applicationId) {
		CatalogMenuDto dto = new CatalogMenuDto();
		dto.setId(m.getId());
		dto.setApplicationId(applicationId);
		dto.setParentId(m.getParentMenu() != null ? m.getParentMenu().getId() : null);
		dto.setName(m.getName());
		dto.setRoute(m.getRoute());
		dto.setIcon(m.getIcon());
		dto.setIsActive(m.getIsActive());
		dto.setAllowedRoles(m.getAllowedRoles());
		dto.setSortOrder(m.getSortOrder());
		dto.setChildren(new ArrayList<>());
		return dto;
	}

	private int nextApplicationSortOrder() {
		Integer max = applicationRepository.findMaxSortOrder();
		return (max == null ? 0 : max) + SORT_STEP;
	}

	private int nextMenuSortOrder(Long applicationId) {
		Integer max = menuRepository.findMaxSortOrderByApplicationId(applicationId);
		return (max == null ? 0 : max) + SORT_STEP;
	}

	private void assertRouteUnique(String route, Long excludeId) {
		if (route == null || route.isBlank()) {
			return;
		}
		Optional<UmMenu> existing = excludeId == null ? menuRepository.findFirstByRouteIgnoreCaseOrderByIdAsc(route)
				: menuRepository.findFirstByRouteIgnoreCaseAndIdNot(route, excludeId);
		if (existing.isPresent()) {
			throw new ServiceException("Route already used by menu: " + route, HttpStatus.CONFLICT);
		}
	}

	private static String normalizeRoute(String route) {
		if (route == null) {
			return null;
		}
		String t = route.trim();
		if (t.isEmpty()) {
			return null;
		}
		return t.startsWith("/") ? t : "/" + t;
	}

	private static String trimToNull(String s) {
		if (s == null) {
			return null;
		}
		String t = s.trim();
		return t.isEmpty() ? null : t;
	}

	/**
	 * Tabler icon names: at least two chars, letters/digits/hyphens (avoids
	 * single-letter console spam).
	 */
	private static String normalizeIcon(String icon) {
		String t = trimToNull(icon);
		if (t == null || t.length() < 2 || !t.matches("(?i)[a-z][a-z0-9\\-]*")) {
			return null;
		}
		return t;
	}

	private static void assertPortalRootAdmin() {
		if (!BusinessContextHolder.canBypassTenant()) {
			throw new ServiceException("Application catalog is restricted to portal root administrators",
					HttpStatus.FORBIDDEN);
		}
	}
}
