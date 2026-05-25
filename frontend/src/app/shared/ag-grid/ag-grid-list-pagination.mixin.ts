import {
  AG_GRID_DEFAULT_PAGE_SIZE,
  AG_GRID_PAGE_SIZE_SELECTOR,
} from './ag-grid-list.defaults';

/** Bind these on list components: {@code readonly gridPagination = gridListPaginationMixin}. */
export const gridListPaginationMixin = {
  pagination: true,
  paginationPageSize: AG_GRID_DEFAULT_PAGE_SIZE,
  paginationPageSizeSelector: AG_GRID_PAGE_SIZE_SELECTOR,
} as const;
