package com.um.api.service.catalog;

import com.um.api.dto.catalog.ApplicationCatalogResponse;
import com.um.api.dto.catalog.CatalogIdRequest;
import com.um.api.dto.catalog.CatalogSaveResponse;
import com.um.api.dto.catalog.ReorderCatalogRequest;
import com.um.api.dto.catalog.SaveApplicationCatalogRequest;
import com.um.api.dto.catalog.SaveMenuCatalogRequest;

public interface IApplicationCatalogService {

	ApplicationCatalogResponse getCatalog();

	CatalogSaveResponse saveApplication(SaveApplicationCatalogRequest request);

	CatalogSaveResponse saveMenu(SaveMenuCatalogRequest request);

	void deleteApplication(CatalogIdRequest request);

	void deleteMenu(CatalogIdRequest request);

	void reorder(ReorderCatalogRequest request);
}
