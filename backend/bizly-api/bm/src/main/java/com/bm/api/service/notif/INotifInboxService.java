package com.bm.api.service.notif;

import com.bm.api.dto.notif.NotifInboxListResponse;

public interface INotifInboxService {

	/**
	 * Persist a notification for a single recipient.
	 * <p>
	 * Runs in {@code REQUIRES_NEW} so a database failure (missing table,
	 * constraint violation, etc.) <strong>cannot</strong> rollback the caller's
	 * transaction — appointment writes must still succeed even if the inbox
	 * insert fails.
	 *
	 * @return {@code true} if a row was written, {@code false} if validation
	 *         skipped it (e.g. blank username) or the DB rejected it.
	 */
	boolean publish(NotifPublishRequest request);

	/**
	 * Same as {@link #publish(NotifPublishRequest)} but skips the insert when
	 * a row already exists for (username, resourceType, resourceId). Use this
	 * from scheduled producers to avoid duplicate reminders if the job runs
	 * twice or a window overlaps.
	 */
	boolean publishOnce(NotifPublishRequest request);

	/**
	 * Returns a single page of the user's inbox, newest first, plus the global
	 * unread count and a {@code hasMore} flag for infinite-scroll callers.
	 * Values are sanitised: negative pages become 0, {@code pageSize} is
	 * clamped to {@code [1, 100]} with a default of 15 when null/non-positive.
	 */
	NotifInboxListResponse recent(String username, Integer pageNumber, Integer pageSize);

	long unreadCount(String username);

	void markRead(String username, Long id);

	void markAllRead(String username);
}
