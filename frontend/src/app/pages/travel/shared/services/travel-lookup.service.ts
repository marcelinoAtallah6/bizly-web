import { Injectable } from '@angular/core';
import { Observable, forkJoin, map, of, tap } from 'rxjs';
import {
  TravelBookingRow,
  TravelClientRow,
  TravelInvoiceRow,
  TravelPackageRow,
} from 'src/app/core/models/travel.models';
import { GetUserResponse } from 'src/app/core/models/um.models';
import { UmUserService } from 'src/app/pages/um/services/um-user.service';
import { TravelBookingService } from '../../services/travel-booking.service';
import { TravelClientService } from '../../services/travel-client.service';
import { TravelInvoiceService } from '../../services/travel-invoice.service';
import { TravelPackageService } from '../../services/travel-package.service';
import { TravelEntityPick } from '../models/travel-entity-pick.model';

@Injectable({ providedIn: 'root' })
export class TravelLookupService {
  private clients: TravelClientRow[] = [];
  private packages: TravelPackageRow[] = [];
  private bookings: TravelBookingRow[] = [];
  private invoices: TravelInvoiceRow[] = [];
  private users: GetUserResponse[] = [];
  private loaded = {
    clients: false,
    packages: false,
    bookings: false,
    invoices: false,
    users: false,
  };

  constructor(
    private readonly clientApi: TravelClientService,
    private readonly packageApi: TravelPackageService,
    private readonly bookingApi: TravelBookingService,
    private readonly invoiceApi: TravelInvoiceService,
    private readonly userApi: UmUserService
  ) {}

  ensureCatalog(
    ...keys: Array<'clients' | 'packages' | 'bookings' | 'invoices' | 'users'>
  ): Observable<void> {
    const jobs: Observable<unknown>[] = [];
    if (keys.includes('clients') && !this.loaded.clients) {
      jobs.push(
        this.clientApi.gets({ pageNumber: 0, pageSize: 500 }).pipe(
          tap((p) => {
            this.clients = p.items ?? [];
            this.loaded.clients = true;
          })
        )
      );
    }
    if (keys.includes('packages') && !this.loaded.packages) {
      jobs.push(
        this.packageApi.gets({ pageNumber: 0, pageSize: 500 }).pipe(
          tap((p) => {
            this.packages = p.items ?? [];
            this.loaded.packages = true;
          })
        )
      );
    }
    if (keys.includes('bookings') && !this.loaded.bookings) {
      jobs.push(
        this.bookingApi.gets({ pageNumber: 0, pageSize: 500 }).pipe(
          tap((p) => {
            this.bookings = p.items ?? [];
            this.loaded.bookings = true;
          })
        )
      );
    }
    if (keys.includes('invoices') && !this.loaded.invoices) {
      jobs.push(
        this.invoiceApi.gets({ pageNumber: 0, pageSize: 500 }).pipe(
          tap((p) => {
            this.invoices = p.items ?? [];
            this.loaded.invoices = true;
          })
        )
      );
    }
    if (keys.includes('users') && !this.loaded.users) {
      jobs.push(
        this.userApi.gets({ pageNumber: 0, pageSize: 500 }).pipe(
          tap((p) => {
            this.users = p.items ?? [];
            this.loaded.users = true;
          })
        )
      );
    }
    return jobs.length ? forkJoin(jobs).pipe(map(() => undefined)) : of(undefined);
  }

  ensureAllForGrids(): Observable<void> {
    return this.ensureCatalog('clients', 'packages', 'bookings', 'invoices');
  }

  clientLabel(id?: number | null): string {
    if (id == null) {
      return '—';
    }
    const c = this.clients.find((x) => x.id === id);
    return c ? this.toClientPick(c).primaryLabel : `Client #${id}`;
  }

  packageLabel(id?: number | null): string {
    if (id == null) {
      return '—';
    }
    const p = this.packages.find((x) => x.id === id);
    return p ? this.toPackagePick(p).primaryLabel : `Package #${id}`;
  }

  bookingLabel(id?: number | null): string {
    if (id == null) {
      return '—';
    }
    const b = this.bookings.find((x) => x.id === id);
    return b ? this.toBookingPick(b).primaryLabel : `Booking #${id}`;
  }

  invoiceLabel(id?: number | null): string {
    if (id == null) {
      return '—';
    }
    const inv = this.invoices.find((x) => x.id === id);
    return inv ? this.toInvoicePick(inv).primaryLabel : `Invoice #${id}`;
  }

  toClientPick(c: TravelClientRow): TravelEntityPick {
    return {
      id: c.id,
      primaryLabel: c.fullName?.trim() || `Client #${c.id}`,
      secondaryLabel: [c.email, c.phone].filter(Boolean).join(' · ') || undefined,
      raw: c,
    };
  }

  toPackagePick(p: TravelPackageRow): TravelEntityPick {
    return {
      id: p.id,
      primaryLabel: p.name?.trim() || `Package #${p.id}`,
      secondaryLabel: [p.destination, p.code].filter(Boolean).join(' · ') || undefined,
      raw: p,
    };
  }

  toBookingPick(b: TravelBookingRow): TravelEntityPick {
    const client = this.clientLabel(b.clientId);
    return {
      id: b.id,
      primaryLabel: b.referenceNo?.trim() || `Booking #${b.id}`,
      secondaryLabel: `${client} · ${b.status ?? ''}`.trim(),
      raw: b,
    };
  }

  toInvoicePick(inv: TravelInvoiceRow): TravelEntityPick {
    const amt = inv.amount != null ? `${Number(inv.amount).toFixed(2)} ${inv.currency ?? ''}`.trim() : '';
    return {
      id: inv.id,
      primaryLabel: inv.invoiceNo?.trim() || `Invoice #${inv.id}`,
      secondaryLabel: [amt, inv.status].filter(Boolean).join(' · ') || undefined,
      raw: inv,
    };
  }

  toUserPick(u: GetUserResponse): TravelEntityPick {
    const name = `${u.firstName ?? ''} ${u.lastName ?? ''}`.trim() || u.username;
    return {
      id: u.id,
      primaryLabel: name,
      secondaryLabel: u.email || u.username,
      raw: u,
    };
  }

  picksFor(
    type: 'client' | 'package' | 'booking' | 'invoice' | 'user',
    filterClientId?: number | null
  ): TravelEntityPick[] {
    switch (type) {
      case 'client':
        return this.clients.map((c) => this.toClientPick(c));
      case 'package':
        return this.packages.map((p) => this.toPackagePick(p));
      case 'booking':
        return this.bookings
          .filter((b) => filterClientId == null || b.clientId === filterClientId)
          .map((b) => this.toBookingPick(b));
      case 'invoice':
        return this.invoices.map((i) => this.toInvoicePick(i));
      case 'user':
        return this.users.map((u) => this.toUserPick(u));
    }
  }

  findPick(type: 'client' | 'package' | 'booking' | 'invoice' | 'user', id?: number | null): TravelEntityPick | null {
    if (id == null) {
      return null;
    }
    return this.picksFor(type).find((p) => p.id === id) ?? null;
  }

  invalidate(): void {
    this.loaded.clients = false;
    this.loaded.packages = false;
    this.loaded.bookings = false;
    this.loaded.invoices = false;
    this.loaded.users = false;
  }
}
