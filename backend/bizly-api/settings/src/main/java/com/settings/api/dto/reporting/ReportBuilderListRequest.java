package com.settings.api.dto.reporting;

import javax.validation.constraints.Size;

import com.settings.common.PageRequest;

public class ReportBuilderListRequest extends PageRequest {

	@Size(max = 200)
	private String nameSearch;

	public String getNameSearch() { return nameSearch; }
	public void setNameSearch(String nameSearch) { this.nameSearch = nameSearch; }
}
