import { Component, Input } from '@angular/core';
import {
  ColDef, ISelectCellEditorParams
} from "@ag-grid-community/core";
import 'ag-grid-enterprise';
import { SideBarDef } from 'ag-grid-community';


@Component({
  selector: 'custom-grid',
  templateUrl: './ag-grid.component.html',
  styleUrl: './ag-grid.component.scss'
})
export class AgGridComponent {
  @Input() pivotMode: boolean = false;
  @Input() PageSize: number = 10;
  @Input() isPagination: boolean = true;
  @Input() isAdvancedSearch: boolean = false;
  @Input() isQuickSearch: boolean = true;
  @Input() rowData: any[] = [];
  @Input() columnDefs: ColDef[] = [];

  public defaultColDef: ColDef = {
    flex: 1,
    enableValue: true,
    enableRowGroup: true,
    enablePivot: true,
  };

  public autoGroupColumnDef: ColDef = {
    minWidth: 200,
    // pinned: "left",
  };

  public sideBar: SideBarDef | string | string[] | boolean | null = "columns";
  public pivotPanelShow: "always" | "onlyWhenPivoting" | "never" = "always";

  public gridOptions: any = {
    columnDefs: this.columnDefs,
    rowData: this.rowData,
    defaultColDef: this.defaultColDef,
    autoGroupColumnDef: this.autoGroupColumnDef,
    getRowClass: (params: any) => {
      return params.data && params.data.someCondition ? 'custom-row' : 'other-row';
    },
    onGridReady: (params: any) => {
      params.api.sizeColumnsToFit();
      params.api.closeToolPanel();
      this.gridOptions.api = params.api;  // Store API reference
    },
  };

  constructor() { }

  ngOnInit(): void {
    // Set the input data to the grid options
    this.gridOptions.columnDefs = this.columnDefs;
    this.gridOptions.rowData = this.rowData;
  }

  onQuickSearch(event: Event) {
    const input = event.target as HTMLInputElement;
    if (this.gridOptions.api) {  // Check if API is defined
      this.gridOptions.api.setQuickFilter(input.value);
    }
  }


}
