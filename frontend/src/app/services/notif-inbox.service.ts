import { Injectable, OnDestroy } from '@angular/core';
import { BehaviorSubject, EMPTY, Observable, Subject, Subscription, of, timer } from 'rxjs';
import { catchError, finalize, switchMap, takeUntil, tap } from 'rxjs/operators';
import { GlobalConstants } from '../common/GlobalConstants';
import { BusinessApiService } from './business-api.service';

/** Severity is mirrored from the backend enum. */
export type NotifSeverity = 'INFO' | 'SUCCESS' | 'WARN' | 'ERROR';

export interface NotifInboxItem {
  id: number;
  category: string;
  severity: NotifSeverity;
  title: string;
  body?: string | null;
  linkRoute?: string | null;
  resourceType?: string | null;
  resourceId?: string | null;
  createdAt: string;
  readAt?: string | null;
  unread: boolean;
}

export interface NotifInboxListResponse {
  items: NotifInboxItem[];
  unreadCount: number;
  totalCount: number;
  pageNumber: number;
  pageSize: number;
  hasMore: boolean;
}

/** Default page size for both the initial load and every loadMore() call. */
const PAGE_SIZE = 15;

/** Polling cadence while a subscriber is listening. */
const POLL_INTERVAL_MS = 45_000;

/**
 * Owns the user's in-app inbox state with **infinite-scroll pagination**.
 *
 * State model:
 *   - `items`: accumulated list across already-loaded pages, newest first.
 *   - `pageNumber`: the highest page index successfully fetched.
 *   - `hasMore`: whether the server still has older rows to deliver.
 *
 * Two ways the list mutates:
 *   1. **refresh()** — called by the 45s poll and the bell-open trigger.
 *      Fetches page 0 with size = max(PAGE_SIZE, items.length), so the user's
 *      already-scrolled range is refreshed in one round trip without losing
 *      scroll position. Any new server-side rows naturally land at the top.
 *   2. **loadMore()** — called by the scroll listener in the header. Fetches
 *      the next page (size = PAGE_SIZE) and appends it. No-op when already
 *      loading or when `hasMore` is false.
 */
@Injectable({ providedIn: 'root' })
export class NotifInboxService implements OnDestroy {
  private readonly itemsSubject = new BehaviorSubject<NotifInboxItem[]>([]);
  private readonly unreadSubject = new BehaviorSubject<number>(0);
  private readonly hasMoreSubject = new BehaviorSubject<boolean>(false);
  private readonly loadingMoreSubject = new BehaviorSubject<boolean>(false);
  private readonly stop$ = new Subject<void>();
  private pollSub: Subscription | null = null;

  /** Highest 0-based page successfully fetched. -1 means "nothing loaded yet". */
  private pageNumber = -1;

  readonly items$ = this.itemsSubject.asObservable();
  readonly unreadCount$ = this.unreadSubject.asObservable();
  readonly hasMore$ = this.hasMoreSubject.asObservable();
  readonly loadingMore$ = this.loadingMoreSubject.asObservable();

  constructor(private readonly api: BusinessApiService) {}

  ngOnDestroy(): void {
    this.stopPolling();
    this.stop$.next();
    this.stop$.complete();
  }

  /** Begin polling (idempotent). Safe to call from header on init. */
  startPolling(): void {
    if (this.pollSub) {
      return;
    }
    this.pollSub = timer(0, POLL_INTERVAL_MS)
      .pipe(
        takeUntil(this.stop$),
        switchMap(() => this.refreshInternal())
      )
      .subscribe();
  }

  stopPolling(): void {
    this.pollSub?.unsubscribe();
    this.pollSub = null;
  }

  /** Force-refresh now (used after the user opens the dropdown or marks-read). */
  refresh(): Observable<NotifInboxListResponse | null> {
    return this.refreshInternal();
  }

  /**
   * Append the next page of older items. Idempotent — bails out cheaply if
   * we're already fetching or there's nothing left to fetch.
   */
  loadMore(): Observable<NotifInboxListResponse | null> {
    if (this.loadingMoreSubject.value || !this.hasMoreSubject.value) {
      return of(null);
    }
    const nextPage = Math.max(this.pageNumber + 1, 1);
    this.loadingMoreSubject.next(true);

    return this.api
      .postEnvelope<NotifInboxListResponse>(
        GlobalConstants.API_ENDPOINTS.bm.notifInbox.recent,
        { pageNumber: nextPage, pageSize: PAGE_SIZE },
        'silent'
      )
      .pipe(
        tap((res) => {
          if (!res) return;
          const existing = this.itemsSubject.value;
          // De-dup against existing ids so a poll-interleave can't produce dupes.
          const existingIds = new Set(existing.map((n) => n.id));
          const fresh = (res.items ?? []).filter((n) => !existingIds.has(n.id));
          this.itemsSubject.next([...existing, ...fresh]);
          this.unreadSubject.next(res.unreadCount ?? 0);
          this.hasMoreSubject.next(!!res.hasMore);
          this.pageNumber = res.pageNumber ?? nextPage;
          // eslint-disable-next-line no-console
          console.debug(
            '[notif-inbox] loadMore OK · page=' + this.pageNumber +
              ' · added=' + fresh.length +
              ' · total=' + this.itemsSubject.value.length +
              ' · hasMore=' + res.hasMore
          );
        }),
        catchError((err) => {
          // eslint-disable-next-line no-console
          console.warn('[notif-inbox] loadMore FAILED', err);
          return of(null);
        }),
        finalize(() => this.loadingMoreSubject.next(false))
      );
  }

  markRead(id: number): void {
    this.api
      .postEnvelope<void>(GlobalConstants.API_ENDPOINTS.bm.notifInbox.markRead, { id }, 'silent')
      .pipe(
        tap(() => {
          const items = this.itemsSubject.value.map((n) =>
            n.id === id ? { ...n, unread: false, readAt: new Date().toISOString() } : n
          );
          this.itemsSubject.next(items);
          this.unreadSubject.next(items.filter((n) => n.unread).length);
        }),
        catchError(() => EMPTY)
      )
      .subscribe();
  }

  markAllRead(): void {
    this.api
      .postEnvelope<void>(GlobalConstants.API_ENDPOINTS.bm.notifInbox.markAllRead, {}, 'silent')
      .pipe(
        tap(() => {
          const now = new Date().toISOString();
          this.itemsSubject.next(
            this.itemsSubject.value.map((n) => ({ ...n, unread: false, readAt: now }))
          );
          this.unreadSubject.next(0);
        }),
        catchError(() => EMPTY)
      )
      .subscribe();
  }

  /**
   * Diagnostic: publish a test notification to myself, then refresh. Uses
   * 'success-and-errors' so the user sees a snackbar on either outcome.
   */
  seedTest(): Observable<NotifInboxListResponse | null> {
    return this.api
      .postEnvelope<string>(
        GlobalConstants.API_ENDPOINTS.bm.notifInbox.seedTest,
        {},
        'success-and-errors'
      )
      .pipe(
        switchMap(() => this.refreshInternal()),
        catchError((err) => {
          // eslint-disable-next-line no-console
          console.warn('[notif-inbox] seed-test FAILED', err);
          return of(null);
        })
      );
  }

  /**
   * Re-fetch from page 0 with size = max(PAGE_SIZE, currently-loaded-count).
   * One request refreshes the entire visible range so scroll position is
   * preserved and any new top-of-list arrivals appear immediately.
   */
  private refreshInternal(): Observable<NotifInboxListResponse | null> {
    const currentlyLoaded = this.itemsSubject.value.length;
    const refreshSize = Math.max(PAGE_SIZE, currentlyLoaded);

    return this.api
      .postEnvelope<NotifInboxListResponse>(
        GlobalConstants.API_ENDPOINTS.bm.notifInbox.recent,
        { pageNumber: 0, pageSize: refreshSize },
        'silent'
      )
      .pipe(
        tap((res) => {
          // eslint-disable-next-line no-console
          console.debug(
            '[notif-inbox] refresh OK · items=' + (res?.items?.length ?? 0) +
              ' · unread=' + (res?.unreadCount ?? 0) +
              ' · hasMore=' + (res?.hasMore ?? false)
          );
          if (!res) return;
          this.itemsSubject.next(res.items ?? []);
          this.unreadSubject.next(res.unreadCount ?? 0);
          this.hasMoreSubject.next(!!res.hasMore);
          // Pin pageNumber to the count we just fetched. If we asked for size
          // 45 (=3 pages worth), the "last loaded" virtual page is 2 so the
          // next loadMore() correctly asks for page 3.
          const fetched = res.items?.length ?? 0;
          this.pageNumber = fetched === 0 ? -1 : Math.floor((fetched - 1) / PAGE_SIZE);
        }),
        catchError((err) => {
          // eslint-disable-next-line no-console
          console.warn(
            '[notif-inbox] fetch FAILED — check (a) DDL applied? (b) bm service restarted? (c) gateway routing /bm/notif-inbox/**',
            err
          );
          return of(null);
        })
      );
  }
}
