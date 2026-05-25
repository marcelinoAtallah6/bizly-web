package com.um.api.dto.catalog;

import java.util.ArrayList;
import java.util.List;

public class ApplicationCatalogResponse {

	private List<CatalogApplicationDto> applications = new ArrayList<>();

	public List<CatalogApplicationDto> getApplications() {
		return applications;
	}

	public void setApplications(List<CatalogApplicationDto> applications) {
		this.applications = applications;
	}
}
