import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ColDef, GridReadyEvent, RowDoubleClickedEvent } from 'ag-grid-community';
import { finalize } from 'rxjs';
import { GetProductResponse } from 'src/app/core/models/pm.models';
import { ToolbarButton } from 'src/app/pages/ui-components/button/toolbar/toolbar.component';
import { PmProductService } from '../../services/pm-product.service';

@Component({
  selector: 'app-product-list',
  templateUrl: './product-list.component.html',
  styleUrl: './product-list.component.scss',
})
export class ProductListComponent implements OnInit {
  rowData: GetProductResponse[] = [];
  loading = false;

  columnDefs: ColDef[] = [
    { field: 'id', headerName: 'ID', width: 90, sortable: true, filter: true },
    { field: 'name', headerName: 'Name', flex: 1, sortable: true, filter: true },
    {
      field: 'price',
      headerName: 'Price',
      width: 140,
      sortable: true,
      filter: true,
      valueFormatter: (p) => (p.value != null ? Number(p.value).toFixed(2) : ''),
    },
    {
      field: 'stockQuantity',
      headerName: 'Stock',
      width: 110,
      sortable: true,
      filter: true,
      valueFormatter: (p) => (p.value != null && p.value !== '' ? String(p.value) : '∞'),
    },
  ];

  defaultColDef: ColDef = {
    resizable: true,
    floatingFilter: true,
  };

  constructor(
    private readonly pmProductService: PmProductService,
    private readonly router: Router
  ) {}

  get listToolbar(): ToolbarButton[] {
    return [
      { id: 'refresh', icon: 'refresh', tooltip: 'Refresh', action: () => this.load(), disabled: this.loading },
      { id: 'add', icon: 'add_box', tooltip: 'New product', action: () => this.goNew(), color: 'primary' },
    ];
  }

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.pmProductService
      .gets({ pageNumber: 0, pageSize: 100 })
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (page) => {
          this.rowData = page.items ?? [];
        },
        error: () => {},
      });
  }

  onGridReady(_e: GridReadyEvent): void {}

  onRowDoubleClicked(event: RowDoubleClickedEvent<GetProductResponse>): void {
    const id = event.data?.id;
    if (id != null) {
      this.router.navigate(['/pm/products', id]);
    }
  }

  goNew(): void {
    this.router.navigate(['/pm/products/add']);
  }
}
