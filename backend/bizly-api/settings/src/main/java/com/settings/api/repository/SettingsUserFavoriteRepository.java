package com.settings.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.settings.api.model.SettingsUserFavorite;
import com.settings.api.model.SettingsUserFavorite.FavoriteId;

@Repository
public interface SettingsUserFavoriteRepository extends JpaRepository<SettingsUserFavorite, FavoriteId> {

	@Query("SELECT f FROM SettingsUserFavorite f WHERE f.id.username = :username "
			+ "ORDER BY f.pinOrder ASC, f.createdAt ASC")
	List<SettingsUserFavorite> findAllForUser(@Param("username") String username);

	/**
	 * When multiple rows are incorrectly flagged default, callers use
	 * {@link #getLatestDefaultRoutes} so login still picks a deterministic route.
	 */
	@Query(value = "SELECT MENU_ROUTE FROM UM.SETTINGS_USER_FAVORITE "
			+ "WHERE USERNAME = :username AND IS_DEFAULT = 1 "
			+ "ORDER BY CREATED_AT DESC FETCH FIRST 1 ROW ONLY", nativeQuery = true)
	List<String> getLatestDefaultRoutes(@Param("username") String username);

	long countByIdUsername(String username);

	@Query(value = "SELECT COUNT(*) FROM UM.SETTINGS_USER_FAVORITE WHERE USERNAME = :username AND IS_DEFAULT = 1", nativeQuery = true)
	long countRowsWithDefaultTrue(@Param("username") String username);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query(value = "UPDATE UM.SETTINGS_USER_FAVORITE SET IS_DEFAULT = 0 WHERE USERNAME = :username", nativeQuery = true)
	int clearAllDefaultsNative(@Param("username") String username);

	/**
	 * Step 2 after {@link #clearAllDefaultsNative}: mark exactly one favorite
	 * as the default landing screen.
	 */
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query(value = "UPDATE UM.SETTINGS_USER_FAVORITE SET IS_DEFAULT = 1 "
			+ "WHERE USERNAME = :username AND MENU_ROUTE = :route", nativeQuery = true)
	int setDefaultTrueNative(@Param("username") String username, @Param("route") String route);

	/**
	 * If legacy data left more than one {@code IS_DEFAULT = 1} row per user,
	 * keep the newest (by {@code CREATED_AT}) and clear the rest. Idempotent.
	 */
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query(value = "UPDATE UM.SETTINGS_USER_FAVORITE f "
			+ "SET IS_DEFAULT = 0 "
			+ "WHERE f.USERNAME = :username AND f.IS_DEFAULT = 1 "
			+ "AND f.ROWID NOT IN ( "
			+ "  SELECT MAX(f2.ROWID) KEEP (DENSE_RANK LAST ORDER BY f2.CREATED_AT) "
			+ "  FROM UM.SETTINGS_USER_FAVORITE f2 "
			+ "  WHERE f2.USERNAME = :username AND f2.IS_DEFAULT = 1 "
			+ "  GROUP BY f2.USERNAME "
			+ ")", nativeQuery = true)
	int dedupeDefaultFlagsNative(@Param("username") String username);
}
