package com.settings.api.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.settings.api.dto.favorite.UserFavoriteItem;
import com.settings.api.dto.favorite.UserFavoritesResponse;
import com.settings.api.model.SettingsUserFavorite;
import com.settings.api.model.SettingsUserFavorite.FavoriteId;
import com.settings.api.repository.SettingsUserFavoriteRepository;

/**
 * Owns the user's starred menus + the one marked as the post-login default.
 *
 * <p>We intentionally do <strong>not</strong> validate routes against UM_MENU
 * on this side. The frontend only offers Add Favorite on routes the user can
 * see, so any string that lands here is by construction reachable. When access
 * is later revoked the frontend filters the favorite out at render time;
 * keeping the row around means re-grant restores the favorite for free.
 *
 * <p>Maximum favorites per user is capped at {@value #MAX_FAVORITES} to bound
 * the dropdown size and protect the table from runaway input.
 */
@Service
public class SettingsUserFavoritesService {

	private static final Logger log = LogManager.getLogger(SettingsUserFavoritesService.class);

	public static final int MAX_FAVORITES = 30;

	@Autowired
	private SettingsUserFavoriteRepository repository;

	/**
	 * Lists favorites for the navbar. Not read-only: if the DB ever contains
	 * more than one {@code IS_DEFAULT = 1} row for this user (legacy / race),
	 * we heal it once per list so the UI and unique index stay consistent.
	 */
	@Transactional
	public UserFavoritesResponse list(String username) {
		UserFavoritesResponse res = new UserFavoritesResponse();
		String u = trim(username);
		if (u == null) {
			res.setItems(new ArrayList<>());
			return res;
		}
		if (repository.countRowsWithDefaultTrue(u) > 1) {
			repository.dedupeDefaultFlagsNative(u);
		}
		List<SettingsUserFavorite> rows = repository.findAllForUser(u);
		List<UserFavoriteItem> items = new ArrayList<>(rows.size());
		/* At most one canonical default route in the API, even if rows were odd before dedupe. */
		String defaultRoute = null;
		for (SettingsUserFavorite f : rows) {
			if (f.isDefault() && defaultRoute == null) {
				defaultRoute = f.getId().getMenuRoute();
			}
		}
		for (SettingsUserFavorite f : rows) {
			boolean itemDefault = defaultRoute != null && defaultRoute.equals(f.getId().getMenuRoute());
			items.add(new UserFavoriteItem(f.getId().getMenuRoute(), f.getPinOrder(), itemDefault));
		}
		res.setItems(items);
		res.setDefaultRoute(defaultRoute);
		return res;
	}

	/**
	 * Pins {@code route} for {@code username}. No-op if it's already starred.
	 * Capped at {@link #MAX_FAVORITES}; further adds are silently ignored
	 * (the frontend should disable the "star" affordance once the cap is hit).
	 */
	@Transactional
	public boolean add(String username, String route) {
		String u = trim(username);
		String r = trim(route);
		if (u == null || r == null) {
			return false;
		}
		FavoriteId id = new FavoriteId(u, r);
		if (repository.existsById(id)) {
			return false;
		}
		long current = repository.countByIdUsername(u);
		if (current >= MAX_FAVORITES) {
			log.info("[USER_FAV][ADD][CAPPED] user={} route={} (cap={})", u, r, MAX_FAVORITES);
			return false;
		}
		SettingsUserFavorite row = new SettingsUserFavorite(u, r);
		row.setPinOrder((int) current);
		row.setDefault(false);
		repository.save(row);
		return true;
	}

	@Transactional
	public boolean remove(String username, String route) {
		String u = trim(username);
		String r = trim(route);
		if (u == null || r == null) {
			return false;
		}
		FavoriteId id = new FavoriteId(u, r);
		if (!repository.existsById(id)) {
			return false;
		}
		repository.deleteById(id);
		return true;
	}

	/**
	 * Marks the given route as the user's default landing screen. Pass
	 * {@code route = null} to clear any existing default.
	 * The route must already be a favorite when non-null — returns {@code false}
	 * without mutating defaults if the route is not starred.
	 *
	 * <p>The DB schema no longer enforces uniqueness of {@code IS_DEFAULT=1}
	 * per user — that invariant is owned here. We run a three-step flow
	 * (dedupe legacy → clear all → set exactly one) so {@code list()} and
	 * {@code getDefaultRoute()} always observe at most one default, even if
	 * an old install still has duplicate flags from a prior bug.
	 */
	@Transactional
	public boolean setDefault(String username, String route) {
		String u = trim(username);
		if (u == null) {
			return false;
		}
		String r = trim(route);
		if (r != null) {
			FavoriteId id = new FavoriteId(u, r);
			if (!repository.existsById(id)) {
				log.info("[USER_FAV][SET_DEFAULT][NO_SUCH_FAV] user={} route={}", u, r);
				return false;
			}
		}
		repository.dedupeDefaultFlagsNative(u);
		repository.clearAllDefaultsNative(u);
		if (r == null) {
			return true;
		}
		int updated = repository.setDefaultTrueNative(u, r);
		if (updated != 1) {
			log.warn("[USER_FAV][SET_DEFAULT][UNEXPECTED_ROWCOUNT] user={} route={} updated={}", u, r, updated);
		}
		return true;
	}

	@Transactional
	public void reorder(String username, List<String> routes) {
		String u = trim(username);
		if (u == null || routes == null || routes.isEmpty()) {
			return;
		}
		int order = 0;
		for (String raw : routes) {
			String r = trim(raw);
			if (r == null) {
				continue;
			}
			Optional<SettingsUserFavorite> opt = repository.findById(new FavoriteId(u, r));
			if (opt.isPresent()) {
				SettingsUserFavorite row = opt.get();
				row.setPinOrder(order++);
				repository.save(row);
			}
		}
	}

	@Transactional(readOnly = true)
	public Optional<String> getDefaultRoute(String username) {
		String u = trim(username);
		if (u == null) {
			return Optional.empty();
		}
		List<String> routes = repository.getLatestDefaultRoutes(u);
		if (routes == null || routes.isEmpty()) {
			return Optional.empty();
		}
		return Optional.ofNullable(routes.get(0));
	}

	private static String trim(String s) {
		if (s == null) return null;
		String t = s.trim();
		return t.isEmpty() ? null : t;
	}
}
