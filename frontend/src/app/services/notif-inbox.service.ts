import { Injectable, OnDestroy } from '@angular/core';
import { BehaviorSubject, EMPTY, Observable, Subject, Subscription, forkJoin, of, timer } from 'rxjs';
import { catchError, finalize, map, switchMap, takeUntil, tap } from 'rxjs/operators';
import { GlobalConstants } from '../common/GlobalConstants';
import { AuthService } from './auth.service';
import { BusinessApiService } from './business-api.service';
import { UmWorkflowService } from '../pages/um/services/um-workflow.service';

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
  /** Synthetic row merged from UM workflow queue — not stored in BM inbox. */
  isWorkflowReminder?: boolean;
}

export interface NotifInboxListResponse {
  items: NotifInboxItem[];
  unreadCount: number;
  totalCount: number;
  pageNumber: number;
  pageSize: number;
  hasMore: boolean;
}

/** Synthetic inbox ids for workflow reminders (no collision with real BM ids). */
const WORKFLOW_NOTIF_ID_BASE = 9_000_000_000;

/** Default page size for both the initial load and every loadMore() call. */
const PAGE_SIZE = 15;

/** Polling cadence while a subscriber is listening. */
const POLL_INTERVAL_MS = 45_000;

@Injectable({
  providedIn: 'root',
})
export class NotifInboxService implements OnDestroy {
  private readonly itemsSubject = new BehaviorSubject<NotifInboxItem[]>([]);
  private readonly unreadSubject = new BehaviorSubject<number>(0);
  private readonly hasMoreSubject = new BehaviorSubject<boolean>(false);
  private readonly loadingMoreSubject = new BehaviorSubject<boolean>(false);
  private readonly stop$ = new Subject<void>();
  private pollSub: Subscription | null = null;

  private pageNumber = -1;

  readonly items$ = this.itemsSubject.asObservable();
  readonly unreadCount$ = this.unreadSubject.asObservable();
  readonly hasMore$ = this.hasMoreSubject.asObservable();
  readonly loadingMore$ = this.loadingMoreSubject.asObservable();

  constructor(
    private readonly api: BusinessApiService,
    private readonly umWorkflow: UmWorkflowService,
    private readonly auth: AuthService
  ) {}

  ngOnDestroy(): void {
    this.stopPolling();
    this.stop$.next();
    this.stop$.complete();
  }

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

  refresh(): Observable<NotifInboxListResponse | null> {
    return this.refreshInternal();
  }

  loadMore(): Observable<NotifInboxListResponse | null> {
    if (!this.auth.isAuthenticated()) {
      return of(null);
    }
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
          const existingIds = new Set(existing.map((n) => n.id));
          const fresh = (res.items ?? []).filter((n) => !existingIds.has(n.id));
          this.itemsSubject.next([...existing, ...fresh]);
          this.unreadSubject.next(this.itemsSubject.value.filter((n) => n.unread).length);
          this.hasMoreSubject.next(!!res.hasMore);
          this.pageNumber = res.pageNumber ?? nextPage;
        }),
        catchError(() => of(null)),
        finalize(() => this.loadingMoreSubject.next(false))
      );
  }

  markRead(id: number): void {
    if (id >= WORKFLOW_NOTIF_ID_BASE) {
      const items = this.itemsSubject.value.map((n) =>
        n.id === id ? { ...n, unread: false, readAt: new Date().toISOString() } : n
      );
      this.itemsSubject.next(items);
      this.unreadSubject.next(items.filter((n) => n.unread).length);
      return;
    }
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
            this.itemsSubject.value.map((n) =>
              n.isWorkflowReminder ? n : { ...n, unread: false, readAt: now }
            )
          );
          this.unreadSubject.next(this.itemsSubject.value.filter((n) => n.unread).length);
        }),
        catchError(() => EMPTY)
      )
      .subscribe();
  }

  seedTest(): Observable<NotifInboxListResponse | null> {
    return this.api
      .postEnvelope<string>(
        GlobalConstants.API_ENDPOINTS.bm.notifInbox.seedTest,
        {},
        'success-and-errors'
      )
      .pipe(
        switchMap(() => this.refreshInternal()),
        catchError(() => of(null))
      );
  }

  private workflowToNotifItems(rows: { id: number; screenName?: string | null; actionName?: string | null; createdAt?: string | null }[]): NotifInboxItem[] {
    return (rows ?? []).map((row) => ({
      id: WORKFLOW_NOTIF_ID_BASE + row.id,
      category: 'WORKFLOW',
      severity: 'WARN' as NotifSeverity,
      title: 'Pending approval',
      body: `${row.screenName ?? '—'} · ${row.actionName ?? ''}`.trim(),
      linkRoute: '/um/workflow-queue',
      resourceType: 'WORKFLOW_INSTANCE',
      resourceId: String(row.id),
      createdAt: row.createdAt ?? new Date().toISOString(),
      unread: true,
      isWorkflowReminder: true,
    }));
  }

  private refreshInternal(): Observable<NotifInboxListResponse | null> {
    if (!this.auth.isAuthenticated()) {
      this.itemsSubject.next([]);
      this.unreadSubject.next(0);
      this.hasMoreSubject.next(false);
      this.pageNumber = -1;
      return of(null);
    }

    const currentlyLoaded = this.itemsSubject.value.length;
    const refreshSize = Math.max(PAGE_SIZE, currentlyLoaded);

    return forkJoin({
      inbox: this.api
        .postEnvelope<NotifInboxListResponse>(
          GlobalConstants.API_ENDPOINTS.bm.notifInbox.recent,
          { pageNumber: 0, pageSize: refreshSize },
          'silent'
        )
        .pipe(catchError(() => of(null))),
      wf: this.umWorkflow.queue({ status: 'PENDING' }).pipe(catchError(() => of([]))),
    }).pipe(
      map(({ inbox, wf }) => {
        const wfItems = this.workflowToNotifItems(wf ?? []);
        const baseItems = inbox?.items ?? [];
        const merged = [...wfItems, ...baseItems];
        const bmUnread = baseItems.filter((n) => n.unread).length;
        const out: NotifInboxListResponse = {
          items: merged,
          unreadCount: bmUnread + wfItems.length,
          totalCount: (inbox?.totalCount ?? baseItems.length) + wfItems.length,
          pageNumber: inbox?.pageNumber ?? 0,
          pageSize: refreshSize,
          hasMore: inbox?.hasMore ?? false,
        };
        this.itemsSubject.next(merged);
        this.unreadSubject.next(out.unreadCount);
        this.hasMoreSubject.next(!!out.hasMore);
        const fetched = baseItems.length;
        this.pageNumber = fetched === 0 ? -1 : Math.floor((fetched - 1) / PAGE_SIZE);
        return out;
      }),
      catchError(() => of(null))
    );
  }
}
