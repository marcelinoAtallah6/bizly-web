package com.auth.api.controllers.dto.admin;

/** Generic autocomplete request body shared by the admin context-switcher endpoints. */
public class AdminSearchRequest {

	/** Text fragment — username / email / business name. Required, min 2 chars. */
	private String q;

	/** Page size cap. Defaults to 20 server-side. */
	private Integer limit;

	public String getQ() { return q; }
	public void setQ(String q) { this.q = q; }
	public Integer getLimit() { return limit; }
	public void setLimit(Integer limit) { this.limit = limit; }
}
