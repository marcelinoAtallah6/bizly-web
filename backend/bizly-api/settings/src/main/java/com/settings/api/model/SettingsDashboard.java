package com.settings.api.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import com.settings.common.DatabaseConstants;

@Entity
@Table(name = DatabaseConstants.DASHBOARD_TABLE, schema = DatabaseConstants.SCHEMA)
public class SettingsDashboard {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 200)
	private String name;

	@Column(nullable = false, unique = true, length = 120)
	private String slug;

	@Column(length = 500)
	private String description;

	@Lob
	@Column(name = "layout_json")
	private String layoutJson;

	@Column(name = "is_builtin", nullable = false)
	private boolean builtin;

	/**
	 * Tenant scope. {@code NULL} means a global / built-in dashboard available to every business
	 * (the seed scripts leave built-in rows NULL); a non-null value scopes the dashboard to a
	 * single business. The service layer must filter with
	 * {@code WHERE business_id IS NULL OR business_id = :bid} for reads.
	 */
	@Column(name = "business_id")
	private Long businessId;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at")
	private Instant updatedAt;

	@OneToMany(mappedBy = "dashboard", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<SettingsWidget> widgets = new ArrayList<>();

	@PrePersist
	void prePersist() {
		Instant now = Instant.now();
		if (createdAt == null) {
			createdAt = now;
		}
		updatedAt = now;
	}

	@PreUpdate
	void preUpdate() {
		updatedAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLayoutJson() {
		return layoutJson;
	}

	public void setLayoutJson(String layoutJson) {
		this.layoutJson = layoutJson;
	}

	public boolean isBuiltin() {
		return builtin;
	}

	public void setBuiltin(boolean builtin) {
		this.builtin = builtin;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}

	public List<SettingsWidget> getWidgets() {
		return widgets;
	}

	public void setWidgets(List<SettingsWidget> widgets) {
		this.widgets = widgets;
	}

	public Long getBusinessId() { return businessId; }
	public void setBusinessId(Long businessId) { this.businessId = businessId; }
}
