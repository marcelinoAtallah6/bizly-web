package com.auth.api.controllers.dto.registration;

/**
 * One business-type entry returned by {@code POST /auth/business-types}. A
 * business type is just a row in {@code um_role} flagged
 * {@code is_business_type = 1}, classified as BUSINESS-level, and not
 * system-restricted. Picking a type at sign-up time therefore also picks the
 * caller's role: there is no separate {@code business_type} catalog any more.
 *
 * <p>This DTO intentionally only exposes the id and a display name. The
 * registration controller never trusts a label sent back by the client — it
 * always re-resolves the role by id on the server so a tampered client cannot
 * select a hidden / system role even if it knows the id.</p>
 */
public class BusinessTypeOption {

	private Long id;
	/** Canonical role name in DB, e.g. {@code RESTAURANT}. */
	private String name;
	/** Optional human-friendly label. Falls back to {@code name} when null. */
	private String label;

	public BusinessTypeOption() {}

	public BusinessTypeOption(Long id, String name, String label) {
		this.id = id;
		this.name = name;
		this.label = label;
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public String getLabel() { return label; }
	public void setLabel(String label) { this.label = label; }
}
