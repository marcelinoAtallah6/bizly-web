package com.settings.api.model;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import com.settings.common.DatabaseConstants;

/**
 * One row per user-defined report (Report Builder).
 *
 * <p>A report is built on top of a saved {@link SettingsQueryDef} (the linked
 * SQL) plus a JSON description of how to expose filters/columns to the UI.
 * Concretely:
 *
 * <ul>
 *   <li>{@code filtersJson} — array of filter definitions. Each filter binds
 *       to one (or two, for ranges) named placeholders inside the linked
 *       query's SQL. Unbound placeholders are sent as {@code NULL} so SQL
 *       can short-circuit them (e.g. {@code AND (:fromDate IS NULL OR ...)} ).</li>
 *   <li>{@code columnsJson} — list of columns to expose in the UI, with
 *       labels, types, and sort/visibility flags. Auto-introspected at save
 *       time and overridable from the builder.</li>
 *   <li>{@code code} — stable URL-safe slug used by the runtime to look up
 *       this report by key (so renaming a report doesn't break bookmarks).</li>
 * </ul>
 */
@Entity
@Table(name = DatabaseConstants.REPORT_TABLE, schema = DatabaseConstants.SCHEMA)
public class SettingsReport {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** Tenant scope. NULL means global (admin-defined catalog template). */
	@Column(name = "business_id")
	private Long businessId;

	/** Stable slug used by the runtime (e.g. {@code my-audit-report}). */
	@Column(name = "code", nullable = false, length = 120, unique = true)
	private String code;

	@Column(nullable = false, length = 200)
	private String name;

	@Column(length = 500)
	private String description;

	/** Tabler icon name (matches existing menu icons). */
	@Column(length = 80)
	private String icon;

	/** ACTIVE / INACTIVE — only ACTIVE reports show up in the sidebar / runner. */
	@Column(name = "status", nullable = false, length = 20)
	private String status;

	/** FK to {@link SettingsQueryDef}.id — the source SQL for this report. */
	@Column(name = "query_def_id", nullable = false)
	private Long queryDefId;

	@Lob
	@Column(name = "filters_json")
	private String filtersJson;

	@Lob
	@Column(name = "columns_json")
	private String columnsJson;

	@Column(name = "default_sort_key", length = 200)
	private String defaultSortKey;

	@Column(name = "default_sort_dir", length = 8)
	private String defaultSortDir;

	@Column(name = "sort_order", nullable = false)
	private Integer sortOrder;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at")
	private Instant updatedAt;

	@Column(name = "created_by", length = 120)
	private String createdBy;

	@Column(name = "updated_by", length = 120)
	private String updatedBy;

	@PrePersist
	void prePersist() {
		Instant now = Instant.now();
		if (createdAt == null) {
			createdAt = now;
		}
		updatedAt = now;
		if (sortOrder == null) {
			sortOrder = 0;
		}
		if (status == null || status.isBlank()) {
			status = "ACTIVE";
		}
	}

	@PreUpdate
	void preUpdate() {
		updatedAt = Instant.now();
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public String getCode() { return code; }
	public void setCode(String code) { this.code = code; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
	public String getIcon() { return icon; }
	public void setIcon(String icon) { this.icon = icon; }
	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }
	public Long getQueryDefId() { return queryDefId; }
	public void setQueryDefId(Long queryDefId) { this.queryDefId = queryDefId; }
	public String getFiltersJson() { return filtersJson; }
	public void setFiltersJson(String filtersJson) { this.filtersJson = filtersJson; }
	public String getColumnsJson() { return columnsJson; }
	public void setColumnsJson(String columnsJson) { this.columnsJson = columnsJson; }
	public String getDefaultSortKey() { return defaultSortKey; }
	public void setDefaultSortKey(String defaultSortKey) { this.defaultSortKey = defaultSortKey; }
	public String getDefaultSortDir() { return defaultSortDir; }
	public void setDefaultSortDir(String defaultSortDir) { this.defaultSortDir = defaultSortDir; }
	public Integer getSortOrder() { return sortOrder; }
	public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
	public Instant getCreatedAt() { return createdAt; }
	public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
	public Instant getUpdatedAt() { return updatedAt; }
	public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
	public String getCreatedBy() { return createdBy; }
	public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
	public String getUpdatedBy() { return updatedBy; }
	public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
	public Long getBusinessId() { return businessId; }
	public void setBusinessId(Long businessId) { this.businessId = businessId; }
}
