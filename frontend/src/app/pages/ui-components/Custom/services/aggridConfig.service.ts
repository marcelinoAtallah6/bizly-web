export interface GridConfig {
  columnDefs: {
    headerName: string;
    field: string;
    sortable?: boolean;
    filter?: boolean;
    editable?: boolean;
    cellRenderer?: string; // Optional for custom cell rendering
    cellFormatter?: string; // Optional custom formatting
  }[];
  pagination?: boolean;
  rowSelection?: 'single' | 'multiple';
}
