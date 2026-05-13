import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { GlobalConstants } from 'src/app/common/GlobalConstants';
import {
  CheckoutRequest,
  CheckoutResponse,
  GetSaleRequest,
  GetSaleResponse,
  GetsSalesRequest,
  GetsSalesResponse,
} from 'src/app/core/models/pm.models';
import { BusinessApiService } from 'src/app/services/business-api.service';

@Injectable({
  providedIn: 'root',
})
export class PmSaleService {
  constructor(private readonly api: BusinessApiService) {}

  checkout(body: CheckoutRequest): Observable<CheckoutResponse> {
    /* Unified product + service checkout now lives in PM ({@code /pm/sale/checkout}). The role only
       needs {@code /pm/products} edit to call it — services are read-only here, looked up in BM. */
    return this.api.postEnvelope<CheckoutResponse>(
      GlobalConstants.API_ENDPOINTS.pm.sale.checkout,
      body,
      'success-and-errors'
    );
  }

  get(body: GetSaleRequest): Observable<GetSaleResponse> {
    return this.api
      .postEnvelope<GetSaleResponse>(GlobalConstants.API_ENDPOINTS.pm.sale.get, body)
      .pipe(map((row) => normalizeGetSaleResponse(row)));
  }

  gets(body: GetsSalesRequest): Observable<GetsSalesResponse> {
    return this.api.postEnvelope<GetsSalesResponse>(GlobalConstants.API_ENDPOINTS.pm.sale.gets, body);
  }
}

function num(v: unknown): number | undefined {
  if (v == null || v === '') {
    return undefined;
  }
  const n = typeof v === 'number' ? v : Number(v);
  return Number.isFinite(n) ? n : undefined;
}

function str(v: unknown): string | undefined {
  if (v == null) {
    return undefined;
  }
  if (typeof v === 'string') {
    return v;
  }
  if (typeof v === 'number' || typeof v === 'boolean') {
    return String(v);
  }
  return undefined;
}

/** Some proxies wrap payload as `{ data: { ... } }` once or twice. */
function unwrapRecord(raw: unknown): Record<string, unknown> {
  let cur: unknown = raw;
  for (let depth = 0; depth < 4 && cur != null && typeof cur === 'object' && !Array.isArray(cur); depth++) {
    const o = cur as Record<string, unknown>;
    const inner = o['data'];
    if (inner != null && typeof inner === 'object' && !Array.isArray(inner)) {
      cur = inner;
    } else {
      break;
    }
  }
  return cur != null && typeof cur === 'object' && !Array.isArray(cur)
    ? (cur as Record<string, unknown>)
    : {};
}

/** Map gateway / JSON variants (snake_case, string numbers) to our model. */
function normalizeGetSaleResponse(row: unknown): GetSaleResponse {
  const r = unwrapRecord(row);
  const cid = num(r['customerId'] ?? r['customer_id']);
  const total = num(r['totalAmount'] ?? r['total_amount']);
  const linesRaw = r['lines'];
  return {
    id: num(r['id']) ?? 0,
    customerId: cid ?? 0,
    customerDisplayName: str(r['customerDisplayName'] ?? r['customer_display_name']),
    totalAmount: total ?? 0,
    status: str(r['status']),
    createdAt:
      typeof r['createdAt'] === 'string'
        ? (r['createdAt'] as string)
        : str(r['created_at']),
    lines: Array.isArray(linesRaw) ? (linesRaw as GetSaleResponse['lines']) : undefined,
  };
}
