package com.settings.api.model;

import java.time.OffsetDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import com.settings.common.DatabaseConstants;

/**
 * Single-row-per-user scalar preferences. Today only carries the last dashboard the user opened;
 * structured this way so future scalar prefs (theme, density, etc.) can land in the same table
 * without another round of migrations.
 */
@Entity
@Table(name = DatabaseConstants.USER_PREF_TABLE, schema = DatabaseConstants.SCHEMA)
public class SettingsUserPreference {

	@Id
	@Column(name = "username", nullable = false, length = 120)
	private String username;

	@Column(name = "last_dashboard_id")
	private Long lastDashboardId;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	@PrePersist
	@PreUpdate
	void touchUpdatedAt() {
		this.updatedAt = OffsetDateTime.now();
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Long getLastDashboardId() {
		return lastDashboardId;
	}

	public void setLastDashboardId(Long lastDashboardId) {
		this.lastDashboardId = lastDashboardId;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}
}
