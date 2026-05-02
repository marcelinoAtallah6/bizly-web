import { Component } from '@angular/core';
import { CategorydetailsComponent } from './categorydetails/categorydetails.component';
import { CardConfig } from '../../ui-components/Custom/services/cardConfig.service';
import { ColDef, ISelectCellEditorParams } from '@ag-grid-community/core';

@Component({
  selector: 'app-category',
  templateUrl: './category.component.html',
  styleUrl: './category.component.scss'
})
export class CategoryComponent {


////////////////// main screen /////////////////

  public switchOption: boolean = true;


  mainScreenData = [
    {
      id: "1",
      title: "Premium Coffee Maker",
      description: "Professional-grade coffee maker with built-in grinder and programmable settings. Perfect for home or small office use.",
      imageUrl: "https://picsum.photos/seed/coffee/400/300",
      stock: 15,
      lastUpdated: new Date("2024-01-01"),
      category: "Gel",
      price: 300,
      currency: 'USD',
      stockValue: 'in stock'
    },
    {
      id: "2",
      title: "Wireless Gaming Mouse",
      description: "High-precision gaming mouse with RGB lighting and customizable buttons. Ultra-low latency for competitive gaming.",
      imageUrl: "https://picsum.photos/seed/mouse/400/300",
      stock: 8,
      lastUpdated: new Date("2024-01-02"),
      category: "Gel",
      price: 300,
      currency: 'USD',
      stockValue: 'in stock'
    },
    {
      id: "3",
      title: "Smart Watch Pro",
      description: "Advanced fitness tracking, heart rate monitoring, and smartphone notifications. Water-resistant up to 50m.",
      imageUrl: "https://picsum.photos/seed/watch/400/300",
      stock: 3,
      lastUpdated: new Date("2024-01-03"),
      category: "Gel",
      price: 300,
      currency: 'USD',
      stockValue: 'in stock'
    },
    {
      id: "4",
      title: "Noise-Canceling Headphones",
      description: "Premium wireless headphones with active noise cancellation. 30-hour battery life and comfortable fit.",
      imageUrl: "https://picsum.photos/seed/headphones/400/300",
      stock: 22,
      lastUpdated: new Date("2024-01-02"),
      category: "Box",
      price: 300,
      currency: 'USD',
      stockValue: 'in stock'
    },
    {
      id: "5",
      title: "4K Ultrawide Monitor",
      description: "34-inch curved display with HDR support. Perfect for productivity and immersive gaming experience.",
      imageUrl: "https://picsum.photos/seed/monitor/400/300",
      stock: 6,
      lastUpdated: new Date("2024-01-03"),
      category: "Gel",
      price: 300,
      currency: 'USD',
      stockValue: 'in stock'
    },
    {
      id: "6",
      title: "Mechanical Keyboard",
      description: "RGB backlit mechanical keyboard with Cherry MX xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx xxxxxxxxxxxxxxxxxxxxxx xxxxxxxxxxxxxxxxxx xxxxxxxxxxxxxxxxxxxxxxxxxxsswitches. Includes wrist rest and multimedia controls.",
      imageUrl: "https://picsum.photos/seed/keyboard/400/300",
      stock: 12,
      lastUpdated: new Date("2024-01-01"),
      category: "Items",
      price: 300,
      currency: 'USD',
      stockValue: 'in stock'
    },
    {
      id: "7",
      title: "Portable Power Bank",
      description: "20000mAh capacity with fast charging support. Charges up to 3 devices simultaneously.",
      imageUrl: "https://picsum.photos/seed/powerbank/400/300",
      stock: 45,
      lastUpdated: new Date("2024-01-02"),
      category: "Gel",
      price: 300,
      currency: 'USD',
      stockValue: 'in stock'
    },
    {
      id: "8",
      title: "Smart Home Hub",
      description: "Central control for all your smart home devices. Voice control and automation support.",
      imageUrl: "https://picsum.photos/seed/smarthome/400/300",
      stock: 5,
      lastUpdated: new Date("2024-01-03"),
      category: "Gel",
      price: 300,
      currency: 'USD',
      stockValue: 'in stock'
    },
    {
      id: "9",
      title: "Premium Coffee Maker",
      description: "Professional-grade coffee maker with built-in grinder and programmable settings. Perfect for home or small office use.",
      imageUrl: "https://picsum.photos/seed/coffee/400/300",
      stock: 15,
      lastUpdated: new Date("2024-01-01"),
      category: "Gel",
      price: 300,
      currency: 'USD',
      stockValue: 'in stock'
    },
    {
      id: "10",
      title: "Premium Coffee Maker",
      description: "Professional-grade coffee maker with built-in grinder and programmable settings. Perfect for home or small office use.",
      imageUrl: "https://picsum.photos/seed/coffee/400/300",
      stock: 15,
      lastUpdated: new Date("2024-01-01"),
      category: "Gel",
      price: 300,
      currency: 'USD',
      stockValue: 'in stock'
    },
    {
      id: "11",
      title: "Premium Coffee Maker",
      description: "Professional-grade coffee maker with built-in grinder and programmable settings. Perfect for home or small office use.",
      imageUrl: "https://picsum.photos/seed/coffee/400/300",
      stock: 15,
      lastUpdated: new Date("2024-01-01"),
      category: "Gel",
      price: 300,
      currency: 'USD',
      stockValue: 'in stock'
    },
    {
      id: "12",
      title: "Premium Coffee Maker",
      description: "Professional-grade coffee maker with built-in grinder and programmable settings. Perfect for home or small office use.",
      imageUrl: "https://picsum.photos/seed/coffee/400/300",
      stock: 15,
      lastUpdated: new Date("2024-01-01"),
      category: "Gel",
      price: 300,
      currency: 'USD',
      stockValue: 'in stock'
    },
    {
      id: "13",
      title: "Premium Coffee Maker",
      description: "Professional-grade coffee maker with built-in grinder and programmable settings. Perfect for home or small office use.",
      imageUrl: "https://picsum.photos/seed/coffee/400/300",
      stock: 15,
      lastUpdated: new Date("2024-01-01"),
      category: "Gel",
      price: 300,
      currency: 'USD',
      stockValue: 'in stock'
    },
    {
      id: "14",
      title: "Premium Coffee Maker",
      description: "Professional-grade coffee maker with built-in grinder and programmable settings. Perfect for home or small office use.",
      imageUrl: "https://picsum.photos/seed/coffee/400/300",
      stock: 15,
      lastUpdated: new Date("2024-01-01"),
      category: "Gel",
      price: 300,
      currency: 'USD',
      stockValue: 'in stock'
    },
    {
      id: "15",
      title: "Premium Coffee Maker",
      description: "Professional-grade coffee maker with built-in grinder and programmable settings. Perfect for home or small office use.",
      imageUrl: "https://picsum.photos/seed/coffee/400/300",
      stock: 15,
      lastUpdated: new Date("2024-01-01"),
      category: "Gel",
      price: 300,
      currency: 'USD',
      stockValue: 'in stock'
    },
    {
      id: "16",
      title: "Premium Coffee Maker",
      description: "Professional-grade coffee maker with built-in grinder and programmable settings. Perfect for home or small office use.",
      imageUrl: "https://picsum.photos/seed/coffee/400/300",
      stock: 15,
      lastUpdated: new Date("2024-01-01"),
      category: "Gel",
      price: 300,
      currency: 'USD',
      stockValue: 'in stock'
    },
    {
      id: "17",
      title: "Premium Coffee Maker",
      description: "Professional-grade coffee maker with built-in grinder and programmable settings. Perfect for home or small office use.",
      imageUrl: "https://picsum.photos/seed/coffee/400/300",
      stock: 15,
      lastUpdated: new Date("2024-01-01"),
      category: "Gel",
      price: 300,
      currency: 'USD',
      stockValue: 'in stock'
    },
    {
      id: "19",
      title: "Premium Coffee Maker",
      description: "Professional-grade coffee maker with built-in grinder and programmable settings. Perfect for home or small office use.",
      imageUrl: "https://picsum.photos/seed/coffee/400/300",
      stock: 15,
      lastUpdated: new Date("2024-01-01"),
      category: "Gel",
      price: 300,
      currency: 'USD',
      stockValue: 'in stock'
    },
    {
      id: "20",
      title: "Premium Coffee Maker",
      description: "Professional-grade coffee maker with built-in grinder and programmable settings. Perfect for home or small office use.",
      imageUrl: "https://picsum.photos/seed/coffee/400/300",
      stock: 15,
      lastUpdated: new Date("2024-01-01"),
      category: "Gel",
      price: 300,
      currency: 'USD',
      stockValue: 'in stock'
    },
    {
      id: "1",
      title: "Premium Coffee Maker",
      description: "Professional-grade coffee maker with built-in grinder and programmable settings. Perfect for home or small office use.",
      imageUrl: "https://picsum.photos/seed/coffee/400/300",
      stock: 15,
      lastUpdated: new Date("2024-01-01"),
      category: "Gel",
      price: 300,
      currency: 'USD',
      stockValue: 'in stock'
    },
    {
      id: "1",
      title: "Premium Coffee Maker",
      description: "Professional-grade coffee maker with built-in grinder and programmable settings. Perfect for home or small office use.",
      imageUrl: "https://picsum.photos/seed/coffee/400/300",
      stock: 15,
      lastUpdated: new Date("2024-01-01"),
      category: "Gel",
      price: 300,
      currency: 'USD',
      stockValue: 'in stock'
    },
    {
      id: "1",
      title: "Premium Coffee Maker",
      description: "Professional-grade coffee maker with built-in grinder and programmable settings. Perfect for home or small office use.",
      imageUrl: "https://picsum.photos/seed/coffee/400/300",
      stock: 15,
      lastUpdated: new Date("2024-01-01"),
      category: "Gel",
      price: 300,
      currency: 'USD',
      stockValue: 'in stock'
    },
    {
      id: "1",
      title: "Premium Coffee Maker",
      description: "Professional-grade coffee maker with built-in grinder and programmable settings. Perfect for home or small office use.",
      imageUrl: "https://picsum.photos/seed/coffee/400/300",
      stock: 15,
      lastUpdated: new Date("2024-01-01"),
      category: "Gel",
      price: 300,
      currency: 'USD',
      stockValue: 'in stock'
    },
    {
      id: "1",
      title: "Premium Coffee Maker",
      description: "Professional-grade coffee maker with built-in grinder and programmable settings. Perfect for home or small office use.",
      imageUrl: "https://picsum.photos/seed/coffee/400/300",
      stock: 15,
      lastUpdated: new Date("2024-01-01"),
      category: "Gel",
      price: 300,
      currency: 'USD',
      stockValue: 'in stock'
    },
    {
      id: "1",
      title: "Premium Coffee Maker",
      description: "Professional-grade coffee maker with built-in grinder and programmable settings. Perfect for home or small office use.",
      imageUrl: "https://picsum.photos/seed/coffee/400/300",
      stock: 15,
      lastUpdated: new Date("2024-01-01"),
      category: "Gel",
      price: 300,
      currency: 'USD',
      stockValue: 'in stock'
    },
    {
      id: "1",
      title: "Premium Coffee Maker",
      description: "Professional-grade coffee maker with built-in grinder and programmable settings. Perfect for home or small office use.",
      imageUrl: "https://picsum.photos/seed/coffee/400/300",
      stock: 15,
      lastUpdated: new Date("2024-01-01"),
      category: "Gel",
      price: 300,
      currency: 'USD',
      stockValue: 'in stock'
    },
  ];

  mainGridConfig: ColDef[] = [
    { field: 'title', sortable: true, filter: true, editable: true, hide: true }, //group    ////   rowGroup: true, hide: true
    { headerName: 'category', field: 'category', sortable: true, filter: true },
    {
      headerName: 'description', field: 'description', sortable: true, filter: true, cellEditor: 'agLargeTextCellEditor',
      cellEditorPopup: true,
      cellEditorParams: {
        maxLength: 100
      }
    },
    { headerName: 'price', field: 'price', sortable: true, filter: true, editable: true },
    {
      headerName: 'stock', field: 'stock',
      cellEditor: "agSelectCellEditor",
      cellRenderer: 'dropdownRenderer',
      editable: true,
      cellEditorParams: {
        values: ['English', 'Spanish', 'French', 'Portuguese', '(other)'],
      } as ISelectCellEditorParams,
    },
    {
      headerName: 'lastUpdated', field: 'lastUpdated',
      cellEditor: 'agDateCellEditor',
      cellEditorParams: {
        // min: '2000-01-01',
        // min: '2019-12-31',
      },
      editable: true
    },
    {
      headerName: 'Image', field: 'imageUrl',
      cellRenderer: 'agCheckboxCellRenderer',
      cellEditor: 'agCheckboxCellEditor',
      editable: true
    },
  ];

  mainScreenCardConfig: CardConfig = {
    header: {
      titleField: 'title', // Uses 'title' field for the card header
      subtitleField: 'category', // Uses 'category' field for the card subtitle
      menuOptions: [
        { icon: 'edit', label: 'Edit', action: 'edit' },
        { icon: 'delete', label: 'Delete', action: 'delete' }
      ]
    },
    content: {
      description: 'description',
      fields: [
        { label: 'Price', field: 'price', format: 'currency' }, // Maps to 'price' and 'currency'
        { label: 'Stock', field: 'stock', format: 'stockValue' }, // Maps to 'stock' with a low-stock indicator
      ]
    },
    actions: {
      buttons: [
        { icon: 'favorite', label: 'Last Updated', action: 'likeItem' },
        { icon: 'share', label: 'Share', action: 'shareItem' },
        { icon: 'shopping_cart', label: 'Add to Cart', action: 'addToCart' }
      ],
      span: [
        { label: 'Last Updated', field: 'lastUpdated', format: 'date' } // Maps to 'lastUpdated'
      ]
    },
    customDetailScreen: CategorydetailsComponent,
    generalSettings: {
      imageField: 'imageUrl', // Maps to 'imageUrl' for card images
      idField: 'id', // Maps to 'id' for unique identification
      dateField: 'lastUpdated' // Maps to 'lastUpdated' for card footer
    },
    generalConfiguration: {
      showHeader: true,
      showContent: true,
      showAction: true,
      showImage: true,
      showSubTitleField: true,
      showTitleField: true,
      showMenuOption: true,
      showActionButtons: false,
      withCustomDetailScreen: true
    }
  };

  detailScreenConfig: any = {
    cardTitle: 'Add Category',
    fields: [
    ],
    add: "",
    edit: "",
    delete: ""

  };

  //////////// Toolbar ///////////////////
  toolbarAction: any[] = ["delete", "sort", "filter", "more", "view"];



}
