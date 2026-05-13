package com.settings.api.dto.reporting;

import javax.validation.constraints.NotNull;

/** Generic id-only payload for builder get / delete. */
public class ReportBuilderIdRequest {

	@NotNull
	private Long id;

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
}
