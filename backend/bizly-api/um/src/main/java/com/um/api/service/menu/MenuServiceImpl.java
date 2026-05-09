package com.um.api.service.menu;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.um.api.dto.menu.NavGroupItemResponse;
import com.um.api.dto.menu.NavMenuItemResponse;
import com.um.api.model.menu.UmApplication;
import com.um.api.model.menu.UmMenu;
import com.um.api.repository.menu.UmApplicationRepository;

@Service
public class MenuServiceImpl implements IMenuService {

	@Autowired
	private UmApplicationRepository applicationRepository;

	@Override
	public List<NavGroupItemResponse> getMenus() {

		// fetch active applications
		List<UmApplication> applications = applicationRepository.findByIsActiveOrderByNameAsc(true);

		List<NavGroupItemResponse> groups = new ArrayList<>();

		for (UmApplication app : applications) {

			NavGroupItemResponse group = new NavGroupItemResponse();
			group.setId(app.getId());
			group.setName(app.getName());
			group.setIcon(app.getIcon());
			group.setRoute(app.getRoute());
			group.setDescription(app.getDescription());

			group.setMenus(buildMenuTree(app.getMenus()));

			groups.add(group);
		}

		return groups;
	}

	private List<NavMenuItemResponse> buildMenuTree(List<UmMenu> menus) {

		if (menus == null || menus.isEmpty()) {
			return new ArrayList<>();
		}

		List<NavMenuItemResponse> result = new ArrayList<>();

		for (UmMenu menu : menus) {
			if (Boolean.TRUE.equals(menu.getIsActive())) {
				result.add(toMenuItem(menu));
			}
		}

		return result;
	}

	private NavMenuItemResponse toMenuItem(UmMenu entity) {

		NavMenuItemResponse dto = new NavMenuItemResponse();

		dto.setId(entity.getId());
		dto.setName(entity.getName());
		dto.setRoute(entity.getRoute());
		dto.setIcon(entity.getIcon());
		dto.setDescription(entity.getApplication().getDescription());
		dto.setIsActive(Boolean.TRUE.equals(entity.getIsActive()));

		// Recursively map children
		if (entity.getMenus() != null && !entity.getMenus().isEmpty()) {

			List<NavMenuItemResponse> childDtos = new ArrayList<>();

			for (UmMenu child : entity.getMenus()) {
				if (Boolean.TRUE.equals(child.getIsActive())) {
					childDtos.add(toMenuItem(child));
				}
			}

			dto.setMenus(childDtos);
		}

		return dto;
	}
}