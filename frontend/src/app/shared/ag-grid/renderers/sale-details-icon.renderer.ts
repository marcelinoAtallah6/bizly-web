import { ICellRendererParams } from 'ag-grid-community';

/** Row shape must include numeric `id` (sale id). */
export type SaleRowWithId = { id?: number };

/**
 * Grid `context` shape: same handler row double-click uses (opens sale detail dialog).
 */
export interface SaleDetailIconGridContext {
  openSaleDetail: (saleId: number) => void;
}

type IconParams = ICellRendererParams<SaleRowWithId> & {
  /** Fallback when `context.openSaleDetail` is not set (see {@link createSaleDetailsIconRenderer}). */
  onOpen?: (saleId: number) => void;
};

/**
 * Prefer `[context]="{ openSaleDetail: (id) => component.openSaleDetail(id) }"` on `ag-grid-angular`
 * so the icon uses the same method instance as `(rowDoubleClicked)`.
 */
export function saleDetailsIconCellRenderer(params: IconParams): HTMLElement {
  const btn = document.createElement('button');
  btn.type = 'button';
  btn.className = 'ag-grid-icon-action-btn';
  btn.title = 'View details';
  btn.setAttribute('aria-label', 'View sale details');
  btn.innerHTML = '<span class="material-icons" aria-hidden="true">visibility</span>';

  btn.addEventListener('click', (ev) => {
    ev.preventDefault();
    ev.stopPropagation();
    const row = (params.node?.data ?? params.data) as SaleRowWithId | undefined;
    const raw = row?.id;
    if (raw === undefined || raw === null) {
      return;
    }
    const saleId = typeof raw === 'number' ? raw : Number(raw);
    if (!Number.isFinite(saleId)) {
      return;
    }

    const ctx = params.context as SaleDetailIconGridContext | undefined;
    const handler = ctx?.openSaleDetail ?? params.onOpen;
    if (!handler) {
      return;
    }
    handler(saleId);
  });

  return btn;
}

/** @deprecated Prefer {@link saleDetailsIconCellRenderer} + grid `context.openSaleDetail`. */
export function createSaleDetailsIconRenderer(onOpen: (saleId: number) => void) {
  return (params: ICellRendererParams<SaleRowWithId>): HTMLElement =>
    saleDetailsIconCellRenderer({ ...params, onOpen });
}
