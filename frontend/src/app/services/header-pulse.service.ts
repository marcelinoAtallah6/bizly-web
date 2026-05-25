import { Injectable, OnDestroy } from '@angular/core';
import { BehaviorSubject, EMPTY, Observable, Subject, Subscription, of, timer } from 'rxjs';
import { catchError, switchMap, takeUntil, tap } from 'rxjs/operators';
import { GlobalConstants } from '../common/GlobalConstants';
import { AuthService } from './auth.service';
import { BusinessApiService } from './business-api.service';

export interface HeaderPulse {
  appointmentsToday?: number | null;
  appointmentsOpenToday?: number | null;
  appointmentsUpcoming24h?: number | null;
  salesCountToday?: number | null;
  salesAmountToday?: number | null;
}

const POLL_INTERVAL_MS = 60_000;

/** Lightweight ticker data for the header. Cheap to refresh, so we poll every 60s. */
@Injectable({ providedIn: 'root' })
export class HeaderPulseService implements OnDestroy {
  private readonly pulseSubject = new BehaviorSubject<HeaderPulse | null>(null);
  private readonly stop$ = new Subject<void>();
  private pollSub: Subscription | null = null;

  readonly pulse$ = this.pulseSubject.asObservable();

  constructor(
    private readonly api: BusinessApiService,
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
        switchMap(() => this.fetchInternal())
      )
      .subscribe();
  }

  stopPolling(): void {
    this.pollSub?.unsubscribe();
    this.pollSub = null;
  }

  refresh(): Observable<HeaderPulse | null> {
    return this.fetchInternal();
  }

  private fetchInternal(): Observable<HeaderPulse | null> {
    if (!this.auth.isAuthenticated()) {
      this.pulseSubject.next(null);
      return of(null);
    }
    return this.api
      .postEnvelope<HeaderPulse>(
        GlobalConstants.API_ENDPOINTS.bm.headerPulse.get,
        {},
        'silent'
      )
      .pipe(
        tap((p) => {
          if (p) {
            this.pulseSubject.next(p);
          }
        }),
        catchError((err) => {
          // eslint-disable-next-line no-console
          console.warn('[header-pulse] fetch FAILED', err);
          return of(null);
        })
      );
  }
}
