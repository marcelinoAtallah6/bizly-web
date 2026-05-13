package com.settings.api.model;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import com.settings.common.DatabaseConstants;

/**
 * One starred menu per (USERNAME, MENU_ROUTE). At most one row per user
 * should have {@code isDefault=true} — that invariant is enforced by
 * {@code SettingsUserFavoritesService} (dedupe → clear → set, plus a
 * self-heal on every {@code list()}) rather than by a DB unique index.
 */
@Entity
@Table(name = DatabaseConstants.USER_FAVORITE_TABLE, schema = DatabaseConstants.SCHEMA)
public class SettingsUserFavorite {

	@EmbeddedId
	private FavoriteId id;

	@Column(name = "pin_order", nullable = false)
	private int pinOrder;

	@Column(name = "is_default", nullable = false)
	private boolean isDefault;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	@PrePersist
	void onPersist() {
		if (this.createdAt == null) {
			this.createdAt = OffsetDateTime.now();
		}
	}

	public SettingsUserFavorite() {
	}

	public SettingsUserFavorite(String username, String menuRoute) {
		this.id = new FavoriteId(username, menuRoute);
	}

	public FavoriteId getId() {
		return id;
	}

	public void setId(FavoriteId id) {
		this.id = id;
	}

	public int getPinOrder() {
		return pinOrder;
	}

	public void setPinOrder(int pinOrder) {
		this.pinOrder = pinOrder;
	}

	public boolean isDefault() {
		return isDefault;
	}

	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	@Embeddable
	public static class FavoriteId implements Serializable {

		private static final long serialVersionUID = 1L;

		@Column(name = "username", length = 120, nullable = false)
		private String username;

		@Column(name = "menu_route", length = 400, nullable = false)
		private String menuRoute;

		public FavoriteId() {
		}

		public FavoriteId(String username, String menuRoute) {
			this.username = username;
			this.menuRoute = menuRoute;
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getMenuRoute() {
			return menuRoute;
		}

		public void setMenuRoute(String menuRoute) {
			this.menuRoute = menuRoute;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof FavoriteId)) return false;
			FavoriteId that = (FavoriteId) o;
			return Objects.equals(username, that.username) && Objects.equals(menuRoute, that.menuRoute);
		}

		@Override
		public int hashCode() {
			return Objects.hash(username, menuRoute);
		}
	}
}
