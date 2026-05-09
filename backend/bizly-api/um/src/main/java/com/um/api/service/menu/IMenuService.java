package com.um.api.service.menu;

import java.util.List;

import com.um.api.dto.menu.NavGroupItemResponse;

public interface IMenuService {

	List<NavGroupItemResponse> getMenus();
}
