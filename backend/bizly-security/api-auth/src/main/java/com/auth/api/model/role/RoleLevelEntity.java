package com.auth.api.model.role;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Classification of roles into ADMIN-level (system-wide) vs BUSINESS-level
 * (tenant-scoped). The registration flow only offers roles whose level has
 * {@code isAssignableOnRegistration = true}.
 */
@Entity
@Table(name = "um_role_level", schema = "um")
public class RoleLevelEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 40, unique = true)
	private String code;

	@Column(nullable = false, length = 120)
	private String name;

	@Column(length = 500)
	private String description;

	@Column(name = "is_assignable_on_registration", nullable = false)
	private Integer isAssignableOnRegistration;

	@Column(name = "sort_order", nullable = false)
	private Integer sortOrder;

	public static final String CODE_ADMIN = "ADMIN";
	public static final String CODE_BUSINESS = "BUSINESS";

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public String getCode() { return code; }
	public void setCode(String code) { this.code = code; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
	public Integer getIsAssignableOnRegistration() { return isAssignableOnRegistration; }
	public void setIsAssignableOnRegistration(Integer v) { this.isAssignableOnRegistration = v; }
	public Integer getSortOrder() { return sortOrder; }
	public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

	public boolean isAssignableOnRegistration() {
		return isAssignableOnRegistration != null && isAssignableOnRegistration == 1;
	}
}
