package com.settings.api.dto.reporting;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportTypesResponse {

	private List<ReportTypeMeta> items;

	public ReportTypesResponse() {}

	public ReportTypesResponse(List<ReportTypeMeta> items) {
		this.items = items;
	}

	public List<ReportTypeMeta> getItems() { return items; }
	public void setItems(List<ReportTypeMeta> items) { this.items = items; }
}
