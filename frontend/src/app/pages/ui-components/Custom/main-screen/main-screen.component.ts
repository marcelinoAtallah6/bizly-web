import { Component, HostListener, Input, TemplateRef, ViewChild } from '@angular/core';
import { ColDef, ISelectCellEditorParams } from '@ag-grid-community/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { data } from 'jquery';
import { ImagePreviewDialogComponent } from '../image-preview/image-preview.component';
import { Card } from '../services/card.service';
import { CardConfig } from '../services/cardConfig.service';
import { GridConfig } from '../services/aggridConfig.service';
import { DetailScreenComponent } from '../detail-screen/detail-screen.component';
import { DialogService } from '../../dialog/service/dialog.service';
import { ToolbarButton } from '../../button/toolbar/toolbar.component';
import { MatSnackBar } from '@angular/material/snack-bar';

export interface productsData {
  id: number;
  imagePath: string;
  uname: string;
  position: string;
  productName: string;
  budget: number;
  priority: string;
}


@Component({
  selector: 'custom-main-screen',
  templateUrl: './main-screen.component.html',
  styleUrl: './main-screen.component.scss'
})
export class MainScreenComponent {
  @Input() data: Card[] = []; // Holds all data
  usedData: Card[] = [];
  @Input() cardTitle: string = "Title";
  @Input()
  cardConfig!: CardConfig;
  @Input()
  gridConfig!: ColDef[];
  itemsPerPage = 10;
  currentPage = 1;
  @Input() detailConfig: any;
  displayedColumns: string[] = ['assigned', 'name', 'priority', 'budget'];

  searchTerms: string = '';
  sortBy: string = 'title';
  @Input() switchOption: boolean = false;
  mainScreenForm: UntypedFormGroup;

  constructor(private fb: UntypedFormBuilder, public dialog: MatDialog, private dialogService: DialogService, private snackBar: MatSnackBar) {

    // this.gridConfig = generateGridConfig(this.rowData);
    this.mainScreenForm = this.fb.group({
      search: [], sortBy: []
    });
  }
  //////////// Toolbar ///////////////////

  @Input() toolbarAction: any[] = ["add", "edit", "delete", "sort", "filter", "more", "view"];
  gridToolbar: any[] = ["add", "edit", "delete", "view"];
  cardToolbar: any[] = ["add", "sort", "filter", "more", "view"];

  toolbar: ToolbarButton[] = [

    {
      id: 'delete',
      icon: 'delete',
      tooltip: 'Delete',
      action: () => this.onDelete()
    },
    {
      id: 'edit',
      icon: 'editOutlined',
      tooltip: 'Edit',
      action: () => this.openMenuDialog("edit")
    },
    {
      id: 'add',
      icon: 'addCircleOutlineOutlined',
      tooltip: 'Add',
      action: () => this.openMenuDialog("add")
    },
    {
      id: 'sort',
      icon: 'sort',
      tooltip: 'Sort',
      hasSubmenu: true,
      submenuItems: [
        { id: 'important', label: 'By Date', icon: 'label_important', action: () => this.handleFilter() },
        { id: 'star', label: 'By Price', icon: 'star', action: () => this.handleFilter() },
        { id: 'filter', label: 'By Title', icon: 'filter', action: () => this.handleFilter() }
      ]
    },
    {
      id: 'filter',
      icon: 'tuneRounded',
      tooltip: 'Filter',
      hasSubmenu: true,
      submenuItems: [
        { id: 'important', label: 'Mark important', icon: 'label_important', action: () => this.handleFilter() },
        { id: 'star', label: 'Add star', icon: 'star', action: () => this.handleFilter() },
        { id: 'filter', label: 'Filter messages like these', icon: 'filter', action: () => this.handleFilter() }
      ]
    },
    {
      id: 'view',
      icon: 'grid_view',
      tooltip: 'View',
      hasSubmenu: true,
      submenuItems: [
        { id: 'grid', label: 'Grid View', icon: 'tableRowsOutlined', action: () => this.handleMainScreenView('grid') },
        { id: 'landscape', label: 'Landscape View', icon: 'cropLandscapeOutlined', action: () => this.handleMainScreenView('card') },
      ]
    }
    // {
    //   id: 'more',
    //   icon: 'more_vert',
    //   tooltip: 'More',
    //   hasSubmenu: true,
    //   submenuItems: [
    //     { id: 'important', label: 'Mark important', icon: 'label_important', action: () => this.handleMarkImportant() },
    //     { id: 'star', label: 'Add star', icon: 'star', action: () => this.handleAddStar() },
    //     { id: 'filter', label: 'Filter messages like these', icon: 'filter_list', action: () => this.handleFilter() }
    //   ]
    // }
  ];

  get filteredToolbar() {
    if (this.SwitchValue == "grid") {
      return this.toolbar.filter(button => this.gridToolbar.includes(button.id))
    }
    else {
      return this.toolbar.filter(button => this.cardToolbar.includes(button.id))
    }
  }

  onDelete() { }

  handleFilter(): void {
    this.showNotification('Filter created');
  }

  private showNotification(message: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 3000,
      horizontalPosition: 'end'
    });
  }

  SwitchValue: String = 'card';
  handleMainScreenView(newLayout: string): void {
    console.log('Selected layout:', newLayout);
    this.SwitchValue = newLayout;
  }

  //////////////////////end toolbar/////////////

  openMenuDialog(menu: any) {

    const detailScreen = this.cardConfig.customDetailScreen;
    const withCustomDetailScreen = this.cardConfig.generalConfiguration.withCustomDetailScreen;
    const dialogConfig = {
      header: this.cardTitle,
      data: this.detailConfig,
      // body: DetailScreenComponent, // Ensure this is a TemplateRef
      body: withCustomDetailScreen ? detailScreen : DetailScreenComponent, // Ensure this is a TemplateRef
      // footer: this.footerTemplate, // Ensure this is also a TemplateRef
      type: 'dialog'
    };

    const dialogRef = this.dialogService.open(dialogConfig);

    dialogRef.afterClosed().subscribe((result: any) => {
      if (result) {
        // Add your delete logic here
      } else {
      }
    });
  }
  ngOnInit(): void {
    // Listen for image preview events from AG Grid
    document.addEventListener('openImagePreview', ((event: CustomEvent) => {
      this.openImagePreview(event.detail);
    }) as EventListener);

    this.loadInitialData();

  }

  loadInitialData() {
    this.usedData = this.data.slice(0, this.itemsPerPage);
    console.log(" this.usedData =", this.usedData)
  }

  loadMoreItems() {
    const nextItems = this.data.slice(this.currentPage * this.itemsPerPage, (this.currentPage + 1) * this.itemsPerPage);

    if (nextItems.length > 0) {
      this.usedData = [...this.usedData, ...nextItems];
      this.currentPage++;
    }
  }
//Scroll option////
  @HostListener('window:scroll', [])
  onScroll() {
    if (this.isBottomReached()) {
      this.loadMoreItems();
    }
  }

  isBottomReached(): boolean {
    return (window.innerHeight + window.scrollY) >= document.body.offsetHeight;
  }

  ///end scroll///


  // on submit /////
  onSubmit() {

  }

  //dropdown values
  selected = 1;
  sortByList = [
    { id: 1, name: 'Title' },
    { id: 2, name: 'Stock' },
    { id: 3, name: 'Popular' },
    { id: 4, name: 'Recent' },
  ];
  ///end ///

  /// Image Preview /////
  openImagePreview(item: { imageUrl: string; title: string }): void {
    this.dialog.open(ImagePreviewDialogComponent, {
      panelClass: 'fullscreen-dialog',
      data: {
        imageUrl: item.imageUrl,
        title: item.title
      },
      maxWidth: '100vw',
      maxHeight: '100vh',
      height: '100%',
      width: '100%'
    });
  }
  cardAction(action: any) {

  }

}
