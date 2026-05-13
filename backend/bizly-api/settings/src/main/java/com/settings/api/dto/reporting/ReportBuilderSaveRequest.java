package com.settings.api.dto.reporting;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Add/update payload for the Report Builder.
 *
 * <p>If {@code id} is {@code null} → create. If present → update. The
 * service auto-introspects the linked query when {@link #columns} is empty
 * or {@link #autoDetectColumns} is true.
 */
public class ReportBuilderSaveRequest {

	private Long id;

	/** Optional — derived from {@code name} when blank. */
	@Size(max = 120)
	private String code;

	@NotBlank
	@Size(max = 200)
	private String name;

	@Size(max = 500)
	private String description;

	@Size(max = 80)
	private String icon;

	@Size(max = 20)
	private String status;

	@NotNull
	private Long queryDefId;

	private List<ReportFilterConfig> filters;

	private List<ReportColumnConfig> columns;

	private String defaultSortKey;

	@Size(max = 8)
	private String defaultSortDir;

	private Integer sortOrder;

	/** When true (or when {@code columns} is null/empty), the server runs the
	 *  linked query with all NULL filter binds to read the column metadata. */
	private Boolean autoDetectColumns;

	/**
	 * Role-based visibility. Each entry is a UM role NAME (matches
	 * {@code GetRoleResponse.name}); the service translates to the stable
	 * {@code UM_ROLE.role_type} before persisting, exactly like the dashboard
	 * builder. {@code null} or an empty list means "no role restriction" — see
	 * {@link #grantUsernames} for how that combines with user grants.
	 */
	private List<String> grantRoles;

	/**
	 * User-based visibility. {@code null} or empty means no user restriction.
	 * When BOTH {@code grantRoles} and {@code grantUsernames} are empty the
	 * report is treated as global within the tenant (any business user can see
	 * it). When EITHER is non-empty, only matching users / roles can see it.
	 */
	private List<String> grantUsernames;

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
	public List<ReportFilterConfig> getFilters() { return filters; }
	public void setFilters(List<ReportFilterConfig> filters) { this.filters = filters; }
	public List<ReportColumnConfig> getColumns() { return columns; }
	public void setColumns(List<ReportColumnConfig> columns) { this.columns = columns; }
	public String getDefaultSortKey() { return defaultSortKey; }
	public void setDefaultSortKey(String defaultSortKey) { this.defaultSortKey = defaultSortKey; }
	public String getDefaultSortDir() { return defaultSortDir; }
	public void setDefaultSortDir(String defaultSortDir) { this.defaultSortDir = defaultSortDir; }
	public Integer getSortOrder() { return sortOrder; }
	public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
	public Boolean getAutoDetectColumns() { return autoDetectColumns; }
	public void setAutoDetectColumns(Boolean autoDetectColumns) { this.autoDetectColumns = autoDetectColumns; }
	public List<String> getGrantRoles() { return grantRoles; }
	public void setGrantRoles(List<String> grantRoles) { this.grantRoles = grantRoles; }
	public List<String> getGrantUsernames() { return grantUsernames; }
	public void setGrantUsernames(List<String> grantUsernames) { this.grantUsernames = grantUsernames; }
}
