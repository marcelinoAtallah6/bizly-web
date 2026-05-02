import { Component, EventEmitter, inject, Input, OnInit, Output, TemplateRef, ViewChild } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { debounceTime, distinctUntilChanged, map, startWith, Subject } from 'rxjs';
import { MatDialog } from '@angular/material/dialog';
import { ProgressBarMode } from '@angular/material/progress-bar';
import { ProgressSpinnerMode } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { NestedTreeControl } from '@angular/cdk/tree';
import { MatTreeNestedDataSource } from '@angular/material/tree';
import { LiveAnnouncer } from '@angular/cdk/a11y';
import { MatTableDataSource } from '@angular/material/table';
import { MatSort, Sort } from '@angular/material/sort';
import { MatPaginator } from '@angular/material/paginator';
import { AlertComponent, AlertDialogData } from '../../ui-components/alert/alert.component';
import { DialogService } from '../../ui-components/dialog/service/dialog.service';
import { ColDef, ISelectCellEditorParams } from '@ag-grid-community/core';
import { ButtonConfig } from '../../ui-components/switch/switch.component';
import { CardConfig } from '../../ui-components/Custom/services/cardConfig.service';
import { ToolbarButton } from '../../ui-components/button/toolbar/toolbar.component';
import { DetailScreenComponent } from '../../ui-components/Custom/detail-screen/detail-screen.component';
import { ButtonComponent } from '../../ui-components/button/button.component';

///tree enode ///
interface TreeNode {
  name: string;
  id: number;
  children?: TreeNode[];
}



@Component({
  selector: 'app-sample-page',
  templateUrl: './sample-page.component.html',
  styleUrls: ['./sample-page.component.css'],
})

export class AppSamplePageComponent implements OnInit {

  public selectedValue: any;
  password: string = ''; // Declare the password property
////stock items /////
  public displayMode2: 'grid' | 'table' = 'grid';
  searchTerm = '';
  private searchSubject = new Subject<string>();
  
  public dataSource2 = new MatTableDataSource<InventoryItem>();
  public displayedColumns2 = ['name', 'quantity', 'price', 'category', 'lastUpdated', 'actions'];
  
  public items2: InventoryItem[] = [];
  pageSize = 20;
  currentPage = 0;
  loading = false;

  ///////////////////// cards //////////

//end stock items////
//card//

public switchOption : boolean = true;
currentLayout = 'grid'; // Initial selected layout
  layoutButtons: ButtonConfig[] = [
    { value: 'grid', label: 'Grid', icon: 'grid_view' },
    { value: 'card', label: 'Card', icon: 'view_module' },
    { value: 'list', label: 'List', icon: 'view_list' } // Example additional option
  ];

  handleLayoutChange(newLayout: string): void {
    console.log('Selected layout:', newLayout);
    this.currentLayout = newLayout;
  }

/////




////autocomplete/////////

  myControl = new FormControl();
  public options2: string[] = ['Option 1', 'Option 2', 'Option 3', 'Option 4'];
  filteredOptions: string[] = [];
  hint: string = 'Select a number from the options';
  //// end atocomplete //

  //header title///
  @Input() title: string = 'Form Layouts';


  //todolidt///


  todolist = [
    {
      id: 'todo',
      title: 'To Do',
      items: [
        { 
          id: 1, 
          title: 'Research competitors', 
          description: 'Analyze top 5 competitors',
          priority: 'high',
          type: 'Research'
        },
        { 
          id: 2, 
          title: 'Design mockups',
          description: 'Create initial wireframes',
          priority: 'medium',
          type: 'Design'
        }
      ]
    },
    {
      id: 'inProgress',
      title: 'In Progress',
      items: [
        {
          id: 3,
          title: 'Frontend development',
          description: 'Implement new features',
          priority: 'high',
          type: 'Development'
        }
      ]
    },
    {
      id: 'done',
      title: 'Done',
      items: [
        {
          id: 4,
          title: 'Project setup',
          description: 'Initial repository setup',
          priority: 'low',
          type: 'Setup'
        }
      ]
    },
    {
      id: 'nathalie',
      title: 'Done',
      items: [
        {
          id: 5,
          title: 'Project setup',
          description: 'Initial repository setup',
          priority: 'low',
          type: 'Setup'
        },
        {
          id: 6,
          title: 'Project setup',
          description: 'Initial repository setup',
          priority: 'low',
          type: 'Setup'
        },
        {
          id: 7,
          title: 'Project setup',
          description: 'Initial repository setup',
          priority: 'low',
          type: 'Setup'
        }
      ]
    },
    {
      id: 'marcelino',
      title: 'Done',
      items: [
        {
          id: 5,
          title: 'Project setup',
          description: 'Initial repository setup',
          priority: 'low',
          type: 'Setup'
        },
        {
          id: 6,
          title: 'Project setup',
          description: 'Initial repository setup',
          priority: 'low',
          type: 'Setup'
        },
        {
          id: 7,
          title: 'Project setup',
          description: 'Initial repository setup',
          priority: 'low',
          type: 'Setup'
        }
      ]
    }
  ];

  ///

  ///form group for vertical form //////////////////////////////////
  accountForm: UntypedFormGroup = this.fb.group({
    name: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]]
  });


  //////dropdown
  public options = [
    { value: 'option1', viewValue: 'Option 1' },
    { value: 'option2', viewValue: 'Option 2' },
    { value: 'option3', viewValue: 'Option 3' }
  ];
  

  public autocomplete=['One', 'Two', 'Three', 'Four'];
  fields = [
    { label: 'Name', icon: 'person', placeholder: 'John Doe', type: 'text', value: '' },
    { label: 'Company', icon: 'business', placeholder: 'ACME Inc.', type: 'text', value: '' },
    { label: 'Email', icon: 'mail', placeholder: 'john.doe@example.com', type: 'email', value: '' },
    { label: 'Phone No', icon: 'phone', placeholder: '123 4561 123', type: 'tel', value: '' },
    { label: 'password', icon: 'password', placeholder: 'Passwords', type: 'password', value: '' },

    { label: 'Message', icon: 'message', placeholder: 'Hi, do you have a moment to talk?', type: 'textarea', value: '' }
  ];

  public fullName: string | undefined;
  public country: string | undefined;
  public birthDate: Date | undefined;
  public deliveryOption: string | undefined; // Holds the selected delivery option
  public phone: string | undefined;

  ///radio//
  radioptions = [
    { label: 'Standard 3-5 Days', value: 1 },
    { label: 'Express', value: 2 },
    { label: 'Overnight', value: 3 }
  ];
  //dropdown values
  countriesList = [
    { id: 1, name: 'USA' },
    { id: 2, name: 'Canada' },
    { id: 3, name: 'Germany' },
    { id: 4, name: 'France' },
    { id: 5, name: 'Australia' },
  ];
  selected = 1;


  nameControl = new FormControl('', Validators.required);



  /////treee view /////////

  treeControl = new NestedTreeControl<TreeNode>(node => node.children);
  dataSource = new MatTreeNestedDataSource<TreeNode>();

  hasChild = (_: number, node: TreeNode) => !!node.children && node.children.length > 0;


  toggleTreeNode(item: TreeNode) {


    if (this.treeControl.isExpanded(item)) {



      this.treeControl.collapse(item);
    } else {

      this.treeControl.expand(item);
    }
  }
  ///end tree viuew///

  fieldsForm: UntypedFormGroup;

  constructor(private fb: UntypedFormBuilder, public dialog: MatDialog, private snackBar: MatSnackBar,private dialogService: DialogService) {


    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged()
    ).subscribe(term => {
      this.filterItems(term);
    });
    
    this.fieldsForm = this.fb.group({
      first: ['', Validators.required],
      country: ['',Validators.required], birthDate: [], selectedOption: [],
      acceptTerms: [],slideToggleChecked:[],numberField:[]

    });
    



    /////treee view /////////
    const treeData: TreeNode[] = [
      {
        name: 'Groceries',
        id: 1,
        children: [
          { name: 'Fruits', id: 3, children: [] },
          { name: 'Vegetables', id: 4, children: [] }
        ]
      },
      {
        name: 'Reminders',
        id: 2,
        children: [
          {
            name: 'Work',
            id: 5,
            children: [
              { name: 'Emails', id: 7, children: [] },
              { name: 'Meetings', id: 8, children: [] }
            ]
          },
          { name: 'Personal', id: 6, children: [] }
        ]
      }
    ];
    this.dataSource.data = treeData;


    ///end tree viuew///


    ////autocomplete/////////
    this.myControl.valueChanges.pipe(
      startWith(''),
      map(value => this._filter(value))
    ).subscribe(filtered => this.filteredOptions = filtered);

    ////end autocomplete ////////////
  }

  ngOnInit(): void {
    this.loadItems();

  }

  public selectedOption: string | undefined; // Holds the selected option

  onPrevious() {
    // Logic for previous action
  }

  onNext() {
    // Logic for next action
  }


  ////autocomplete/////////

  private _filter(value: string): string[] {
    const filterValue = value.toLowerCase();
    return this.options2.filter((option: any) => option.toLowerCase().includes(filterValue));
  }
  onInput(event: any) {
    // Handle input change if needed
  }

  ////end autocomplete ////////////


  //open  dialog ///


  openDialog() {
    const dialogRef = this.dialog.open(AppSamplePageComponent);

    dialogRef.afterClosed().subscribe((result: any) => {
      if (result) {
        // Add your delete logic here
      } else {
      }
    });
  }

  onInputChange(x: any) {
    alert(x)
  }


  ///dialog ///

  ////divider////


  items = ['Item 1', 'Item 2', 'Item 3'];

  isLastItem(item: string, index: number): boolean {
    return index === this.items.length - 1;
  }


  ///devider////






  ///chat /////



  messages = [
    { user: 'James Johnson', text: 'Hi Luke.', date: 'Jan 5, 2016' },
    { user: 'James Johnson', text: 'How are you my friend?', date: 'Jan 6, 2016' },
    { user: 'You', text: 'I am good and what about you?', date: 'Jan 7, 2016' },
    { user: 'James Johnson', text: 'Lorem Ipsum is simply dummy text.', date: 'Jan 8, 2016' },
    { user: 'You', text: 'I would love to join the team.', date: 'Jan 9, 2016' }
  ];
  messageInput: string = '';

  sendMessage() {
    if (this.messageInput.trim()) {
      this.messages.push({ user: 'You', text: this.messageInput, date: new Date().toLocaleDateString() });
      this.messageInput = '';
    }
  }


  ///end chat ////

  ///Progress Bar //////////////////////////////////


  readonly availableModes: ProgressBarMode[] = ['determinate', 'indeterminate', 'buffer', 'query'];

  mode: ProgressBarMode = 'determinate'; // Default mode
  color: string = 'primary'; // Default color
  progressValue: number = 50; // Default progress value
  updateProgress(mode: ProgressBarMode, color: string) {
    // Update logic can be implemented here if needed
  }



  ///end progress bar ///////////////////////////////////



  ////////////////////// Define available modes as ProgressSpinnerMode type/////////////////////////


  readonly availableSpinnerModes: ProgressSpinnerMode[] = ['determinate', 'indeterminate'];
  spinnerMode: ProgressSpinnerMode = 'indeterminate'; // Default mode
  spinnerColor: string = 'primary'; // Default color
  spinnerValue: number = 50; // Default progress value

  updateSpinner(mode: ProgressSpinnerMode, color: string) {
    // Logic for updating spinner can be implemented here
  }


  ////////////////////// end available modes as ProgressSpinnerMode type/////////////////////////

  ///Toggle Componwnt ///



  slideToggleChecked: boolean = false; // Default state of the slide toggle

  onSlideToggleChange(event: any) {
    this.slideToggleChecked = event.checked; // Update state on toggle change
  }


  ///end toggle compoenntt ///


  //slider ////



  // sliderFields = [
  //   { label: 'Value', type: 'number', value: null },
  //   { label: 'Min value', type: 'number', value: null },
  //   { label: 'Max value', type: 'number', value: null },
  //   { label: 'Step size', type: 'number', value: null },
  // ];

  // checkboxes = [
  //   { label: 'Show ticks', checked: false },
  //   { label: 'Show thumb label', checked: false },
  //   { label: 'Disabled', checked: false },
  // ];

  // sliderValue = 50;


  /////////////

  ///snack bar / ALERT//////////////////////////////////

  openSnackBar() {
    this.snackBar.open('This is a Snackbar message!', 'Close', {
      duration: 3000, // Duration in milliseconds
    });
  }

  openAlertDialog(): void {
    const dialogData: AlertDialogData = {
      title: 'Delete Confirmation',
      message: 'Are you sure you want to delete this item?',
      alertType: 'warn',
      confirmButtonText: 'Confirm',
      cancelButtonText: 'Cancel',
      showCancelButton: true,
    };

    const dialogRef = this.dialog.open(AlertComponent, {
      width: '400px',
      data: dialogData,
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        console.log('Confirmed!');
      } else {
        console.log('Cancelled!');
      }
    });
  }

///end alert///




///start dialog ///'
@ViewChild('myTemplate') myTemplate!: TemplateRef<any>;
@ViewChild('footerTemplate') footerTemplate!: TemplateRef<any>;

openTemplateDialog() {
  this.dialogService.open({
    header: 'Template Dialog',
    body: this.myTemplate, // Ensure this is a TemplateRef
    footer: this.footerTemplate, // Ensure this is also a TemplateRef
    type:'dialog'
    // data: { message: 'This is a template dialog' },
    // Additional properties if needed
  });
}

openComponentDialog() {
  this.dialogService.open({
    header: 'Component Dialog',
    body: AlertComponent, // Ensure this is a component class (Type<any>)
    // data: { title: 'Component Dialog' },
    // Additional properties if needed
  });
}
closeDialog() {
  // Logic to close dialog if needed
}

confirm() {
  // Logic to confirm action
  console.log('Confirmed');
}

///end ///



////menu items//


menuItems = [
  {
    name: 'Animals',
    subItems: [
      {
        name: 'Vertebrates',
        subItems: [
          { name: 'Fishes' },
          { name: 'Amphibians' },
          { name: 'Reptiles' },
          { name: 'Birds' },
          { name: 'Mammals' },
        ]
      },
      {
        name: 'Invertebrates',
        subItems: [
          { name: 'Insects' },
          { name: 'Molluscs' },
          { name: 'Crustaceans' },
          { name: 'Corals' },
          { name: 'Arachnids' },
          { name: 'Velvet worms' },
          { name: 'Horseshoe crabs' },
        ]
      },
    ]
  }
];

//end//


//aggrid///


rowData: any[] = [
  { position: 1, name: 'Hydrogens', weight: 1.0079, symbol: 'H', action: 'English' },
  { position: 2, name: 'Helium', weight: 4.0026, symbol: 'He' },
  { position: 2, name: 'Helium', weight: 4.0026, symbol: 'He' },
  { position: 2, name: 'Helium', weight: 4.0026, symbol: 'He' },
  { position: 2, name: 'Helium', weight: 4.0026, symbol: 'He' },
  { position: 2, name: 'Helium', weight: 4.0026, symbol: 'He' },
  { position: 2, name: 'Helium', weight: 4.0026, symbol: 'He' },
  { position: 2, name: 'Helium', weight: 4.0026, symbol: 'He' },
  { position: 2, name: 'Helium', weight: 4.0026, symbol: 'He' },
  { position: 2, name: 'Helium', weight: 4.0026, symbol: 'He' },
  { position: 2, name: 'Helium', weight: 4.0026, symbol: 'He' },
  { position: 2, name: 'Helium', weight: 4.0026, symbol: 'He' },
  { position: 2, name: 'Helium', weight: 4.0026, symbol: 'He' },
  { position: 2, name: 'Helium', weight: 4.0026, symbol: 'He' },
  { position: 2, name: 'Helium', weight: 4.0026, symbol: 'He' },
  { position: 2, name: 'Helium', weight: 4.0026, symbol: 'He' },
  { position: 2, name: 'Helium', weight: 4.0026, symbol: 'He' },
  { position: 2, name: 'Helium', weight: 4.0026, symbol: 'He' },
  { position: 2, name: 'Helium', weight: 4.0026, symbol: 'He' },
  { position: 2, name: 'Helium', weight: 4.0026, symbol: 'He' },
  { position: 2, name: 'Helium', weight: 4.0026, symbol: 'He' }
];

columnDefs: ColDef[] = [
  { field: 'position', sortable: true, filter: true, editable: true, hide: true }, //group    ////   rowGroup: true, hide: true
  { headerName: 'Name', field: 'name', sortable: true, filter: true },
  {
    headerName: 'Weight', field: 'weight', sortable: true, filter: true, cellEditor: 'agLargeTextCellEditor',
    cellEditorPopup: true,
    cellEditorParams: {
      maxLength: 100
    }
  },
  { headerName: 'Symbol', field: 'symbol', sortable: true, filter: true, editable: true },
  {
    headerName: 'Action', field: 'action',
    cellEditor: "agSelectCellEditor",
    cellRenderer: 'dropdownRenderer',
    editable: true,
    cellEditorParams: {
      values: ['English', 'Spanish', 'French', 'Portuguese', '(other)'],
    } as ISelectCellEditorParams,
  },
  {
    headerName: 'Date', field: 'date',
    cellEditor: 'agDateCellEditor',
    cellEditorParams: {
      // min: '2000-01-01',
      // min: '2019-12-31',
    },
    editable: true
  },
  {
    headerName: 'is Main', field: 'ismain',
    cellRenderer: 'agCheckboxCellRenderer',
    cellEditor: 'agCheckboxCellEditor',
    editable: true
  },
];


//end///

  ///profile component //////////////////////////////////


  currentPassword: string = '';
  newPassword: string = '';
  confirmPassword: string = '';
  userName: string = 'Mathew Anderson';
  email: string = 'info@modernize.com';
  storeName: string = 'Maxima Studio';
  location: string = 'India';
  tel: string = '+91 12345 65478';
  currency: string = 'INR';


  notifications = [
    {
      title: 'Our Newsletter',
      description: "We'll always let you know about important changes",
      icon: 'article',
      enabled: true
    },
    {
      title: 'Order Confirmation',
      description: 'You will be notified when a customer orders any product',
      icon: 'check_box',
      enabled: false
    },
    {
      title: 'Order Status Changed',
      description: 'You will be notified when a customer makes changes to the order',
      icon: 'schedule',
      enabled: true
    },
    {
      title: 'Order Delivered',
      description: 'You will be notified once the order is delivered',
      icon: 'local_shipping',
      enabled: false
    },
    {
      title: 'Email Notification',
      description: 'Turn on email notification to get updates through email',
      icon: 'mail',
      enabled: true
    }
  ];

  ///bills//

  billingForm: FormGroup = this.fb.group({
    businessName: ['', Validators.required],
    businessAddress: ['', Validators.required],
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    businessSector: ['', Validators.required],
    country: ['', Validators.required],
  });

  billsFields = [
    { label: 'Business Name', name: 'businessName', placeholder: 'Enter your business name' },
    { label: 'Business Address', name: 'businessAddress', placeholder: 'Enter your business address' },
    { label: 'First Name', name: 'firstName', placeholder: 'Enter your first name' },
    { label: 'Last Name', name: 'lastName', placeholder: 'Enter your last name' },
    { label: 'Business Sector', name: 'businessSector', placeholder: 'Enter your business sector' },
    { label: 'Country', name: 'country', placeholder: 'Enter your country' },
  ];

  onSubmit() {
    if (this.fieldsForm.valid) {
      console.log(this.fieldsForm)
    }
  }

  ////end bills


  // table //

  ELEMENT_DATA: PeriodicElement[] = [
    { position: 1, name: 'Hydrogen', weight: 1.0079, symbol: 'H', selectedOption: 'Option2' },
    { position: 2, name: 'Helium', weight: 4.0026, symbol: 'He', selectedOption: 'Option3' },
    { position: 3, name: 'Lithium', weight: 6.941, symbol: 'Li', selectedOption: 'Option2' },
    { position: 4, name: 'Beryllium', weight: 9.0122, symbol: 'Be', selectedOption: 'Option2' },
    { position: 5, name: 'Boron', weight: 10.811, symbol: 'B', selectedOption: 'Option2', },
    { position: 6, name: 'Carbon', weight: 12.0107, symbol: 'C', selectedOption: 'Option2' },
    { position: 7, name: 'Nitrogen', weight: 14.0067, symbol: 'N', selectedOption: 'Option2' },
    { position: 8, name: 'Oxygen', weight: 15.9994, symbol: 'O', selectedOption: 'Option2', },
    { position: 9, name: 'Fluorine', weight: 18.9984, symbol: 'F', selectedOption: 'Option2', },
    { position: 10, name: 'Neon', weight: 20.1797, symbol: 'Ne', selectedOption: 'Option2', },
    { position: 4, name: 'Beryllium', weight: 9.0122, symbol: 'Be', selectedOption: 'Option2', },
    { position: 5, name: 'Boron', weight: 10.811, symbol: 'B', selectedOption: 'Option2' },
    { position: 6, name: 'Carbon', weight: 12.0107, symbol: 'C', selectedOption: 'Option2' },
    { position: 7, name: 'Nitrogen', weight: 14.0067, symbol: 'N', selectedOption: 'Option2' },
    { position: 8, name: 'Oxygen', weight: 15.9994, symbol: 'O', selectedOption: 'Option2' },
    { position: 9, name: 'Fluorine', weight: 18.9984, symbol: 'F', selectedOption: 'Option2' },
    { position: 10, name: 'Neon', weight: 20.1797, symbol: 'Ne', selectedOption: 'Option2' },
  ];

  displayedColumns: string[] = ['position', 'name', 'weight', 'symbol'];


  private _liveAnnouncer = inject(LiveAnnouncer);

  tabledataSource = new MatTableDataSource(this.ELEMENT_DATA);


  @ViewChild(MatSort)
  sort!: MatSort;
  @ViewChild(MatPaginator)
  paginator!: MatPaginator;

  ngAfterViewInit() {
    this.tabledataSource.sort = this.sort;
    this.tabledataSource.paginator = this.paginator;

  }


  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.tabledataSource.filter = filterValue.trim().toLowerCase();
  }

  /** Announce the change in sort state for assistive technology. */
  announceSortChange(sortState: Sort) {
    // This example uses English messages. If your application supports
    // multiple language, you would internationalize these strings.
    // Furthermore, you can customize the message to add additional
    // details about the values being sorted.
    if (sortState.direction) {
      this._liveAnnouncer.announce(`Sorted ${sortState.direction}ending`);
    } else {
      this._liveAnnouncer.announce('Sorting cleared');
    }
  }
  ///end table ///

////stock items /////

  onSearch(event: Event): void {
    const term = (event.target as HTMLInputElement).value;
    this.searchSubject.next(term);
  }

  filterItems(term: string): void {
    this.dataSource2.filter = term.toLowerCase();
  }

  loadMore(): void {
    if (!this.loading) {
      this.currentPage++;
      this.loadItems();
    }
  }

  private loadItems(): void {
    this.loading = true;
    // Simulate API call
    setTimeout(() => {
      const newItems = this.generateMockItems();
      this.items2 = [...this.items2, ...newItems];
      this.dataSource2.data = this.items2;
      this.loading = false;
    }, 500);
  }

  getStockStatus(quantity: number, threshold: number): string {
    return quantity <= threshold ? 'warn' : 'primary';
  }

  private generateMockItems(): InventoryItem[] {
    // Mock data generation logic
    return Array(this.pageSize).fill(0).map((_, index) => ({
      id: this.items.length + index,
      name: `Item ${this.items.length + index}`,
      quantity: Math.floor(Math.random() * 100),
      price: Math.random() * 1000,
      category: ['Electronics', 'Clothing', 'Food'][Math.floor(Math.random() * 3)],
      lastUpdated: new Date(),
      threshold: 10
    }));
  }
//end stock items////
  ////

  /////////////cardholder/////////


  name = 'Angular';
  info = {
    cardNumber: null,
    cvc: null,
    month: null,
    year: null,
    cardHolder: null
  }
  
  flip = false;

  flipper() {
    this.flip = !this.flip;
  }

  /////////////cardholder/////////

  ////////////////// main screen /////////////////

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

  mainScreenCardConfig :CardConfig= {
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
  customDetailScreen: ButtonComponent,
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
    withCustomDetailScreen:false
  }
};

detailScreenConfig : any ={
  cardTitle: 'Items',
  fields: [
    {
      key: 'id',
      type: 'input',
      label: 'ID',
      placeholder: 'Enter ID',
      required: true,
      inputType: 'text'
    },
    {
      key: 'id',
      type: 'input',
      label: 'ID',
      placeholder: 'Enter ID',
      required: true,
      inputType: 'text'
    },
    {
      key: 'id',
      type: 'input',
      label: 'ID',
      placeholder: 'Enter ID',
      required: true,
      inputType: 'text'
    },
    {
      key: 'id',
      type: 'input',
      label: 'ID',
      placeholder: 'Enter ID',
      required: true,
      inputType: 'text'
    },
    {
      key: 'id',
      type: 'input',
      label: 'ID',
      placeholder: 'Enter ID',
      required: true,
      inputType: 'text'
    },
    {
      key: 'id',
      type: 'input',
      label: 'ID',
      placeholder: 'Enter ID',
      required: true,
      inputType: 'text'
    },
    {
      key: 'id',
      type: 'input',
      label: 'ID',
      placeholder: 'Enter ID',
      required: true,
      inputType: 'text'
    },
    {
      key: 'id',
      type: 'input',
      label: 'ID',
      placeholder: 'Enter ID',
      required: true,
      inputType: 'text'
    },
    {
      key: 'id',
      type: 'input',
      label: 'ID',
      placeholder: 'Enter ID',
      required: true,
      inputType: 'text'
    },
    {
      key: 'id',
      type: 'input',
      label: 'ID',
      placeholder: 'Enter ID',
      required: true,
      inputType: 'text'
    },
    {
      key: 'id',
      type: 'input',
      label: 'ID',
      placeholder: 'Enter ID',
      required: true,
      inputType: 'text'
    },
    {
      key: 'title',
      type: 'input',
      label: 'Title',
      placeholder: 'Enter title',
      required: true,
      inputType: 'text'
    },
    {
      key: 'description',
      type: 'input',
      label: 'Description',
      placeholder: 'Enter description',
      required: false,
      inputType: 'text'
    },
    {
      key: 'imageUrl',
      type: 'input',
      label: 'Image URL',
      placeholder: 'Enter image URL',
      required: true,
      inputType: 'text'
    },
    {
      key: 'stock',
      type: 'input',
      label: 'Stock',
      placeholder: 'Enter stock quantity',
      required: true,
      inputType: 'number'
    },
    {
      key: 'lastUpdated',
      type: 'datepicker',
      label: 'Last Updated',
      placeholder: 'Select last updated date',
      required: true
    },
    {
      key: 'category',
      type: 'dropdown',
      label: 'Category',
      options: [
        { id: 1, name: 'USA' },
        { id: 2, name: 'Canada' },
        { id: 3, name: 'Germany' },
        { id: 4, name: 'France' },
        { id: 5, name: 'Australia' }
      ],
      required: true,
      multiple: false
    },
    {
      key: 'price',
      type: 'input',
      label: 'Price',
      placeholder: 'Enter price',
      required: true,
      inputType: 'number'
    },
    {
      key: 'currency',
      type: 'dropdown',
      label: 'Currency',
      options: [
        { id: 1, name: 'USA' },
        { id: 2, name: 'Canada' },
        { id: 3, name: 'Germany' },
        { id: 4, name: 'France' },
        { id: 5, name: 'Australia' }
      ],
      required: false,
      multiple: true
    },
    {
      key: 'stockValue',
      type: 'radio',
      label: 'Stock Status',
      options: [
        { label: 'Standard 3-5 Days', value: 1 },
        { label: 'Express', value: 2 },
        { label: 'Overnight', value: 3 }
      ],
      required: true
    }
  ],
  add:"",
  edit:"",
  delete:""

};

//////////// Toolbar ///////////////////
toolbarAction : any[] =["delete","sort","filter","more","view"];

  // toolbar: ToolbarButton[] = [
  //   // {
  //   //   id: 'back',
  //   //   icon: 'arrow_back',
  //   //   tooltip: 'Back to Inbox',
  //   //   action:  () => this.handleBackToInbox()
  //   // },
  //   // {
  //   //   id: 'archive',
  //   //   icon: 'archive',
  //   //   tooltip: 'Archive',
  //   //   action: () => this.handleArchive()
  //   // },
  //   // {
  //   //   id: 'report',
  //   //   icon: 'report',
  //   //   tooltip: 'Report spam',
  //   //   action: () => this.handleReportSpam()
  //   // },
  //   // {
  //   //   id: 'delete',
  //   //   icon: 'delete',
  //   //   tooltip: 'Delete',
  //   //   action: () => this.handleDelete()
  //   // },
  //   // {
  //   //   id: 'mark_email_unread',
  //   //   icon: 'mark_email_unread',
  //   //   tooltip: 'Mark as unread',
  //   //   hasSubmenu: true,
  //   //   submenuItems: [
  //   //     { id: 'unread', label: 'Mark as unread', icon: 'mark_email_unread', action: () => this.handleMarkUnread() },
  //   //     { id: 'read', label: 'Mark as read', icon: 'mark_email_read', action: () => this.handleMarkRead() }
  //   //   ]
  //   // },
  //   // {
  //   //   id: 'move_to',
  //   //   icon: 'drive_file_move',
  //   //   tooltip: 'Move to',
  //   //   hasSubmenu: true,
  //   //   submenuItems: [
  //   //     { id: 'inbox', label: 'Inbox', icon: 'inbox', action: () => this.handleMove('inbox') },
  //   //     { id: 'spam', label: 'Spam', icon: 'report', action: () => this.handleMove('spam') },
  //   //     { id: 'trash', label: 'Trash', icon: 'delete', action: () => this.handleMove('trash') }
  //   //   ]
  //   // },
  //   {
  //     id: 'delete',
  //     icon: 'delete',
  //     tooltip: 'Delete'
  //   },
  //   {
  //     id: 'edit',
  //     icon: 'editOutlined',
  //     tooltip: 'Edit',
  //   },
  //   {
  //     id: 'add',
  //     icon: 'addCircleOutlineOutlined',
  //     tooltip: 'Add',
  //   },
  //   {
  //     id: 'sort',
  //     icon: 'sort',
  //     tooltip: 'Sort',
  //     hasSubmenu: true,
  //     submenuItems: [
  //       { id: 'important', label: 'By Date', icon: 'label_important', action: () => this.handleFilter() },
  //       { id: 'star', label: 'By Price', icon: 'star', action: () => this.handleFilter() },
  //       { id: 'filter', label: 'By Title', icon: 'filter', action: () => this.handleFilter() }
  //     ]
  //   },
  //   {
  //     id: 'filter',
  //     icon: 'tuneRounded',
  //     tooltip: 'Filter',
  //     hasSubmenu: true,
  //     submenuItems: [
  //       { id: 'important', label: 'Mark important', icon: 'label_important', action: () => this.handleFilter() },
  //       { id: 'star', label: 'Add star', icon: 'star', action: () => this.handleFilter() },
  //       { id: 'filter', label: 'Filter messages like these', icon: 'filter', action: () => this.handleFilter() }
  //     ]
  //   },
  //   {
  //     id: 'view',
  //     icon: 'grid_view',
  //     tooltip: 'View',
  //     hasSubmenu: true,
  //     submenuItems: [
  //       { id: 'grid', label: 'Grid View', icon: 'GridViewOutlined', action: () => this.handleMainScreenView('grid') },
  //       { id: 'landscape', label: 'Landscape View', icon: 'CropLandscapeOutlined', action: () => this.handleMainScreenView('card') },
  //     ]
  //   }
  //   // {
  //   //   id: 'more',
  //   //   icon: 'more_vert',
  //   //   tooltip: 'More',
  //   //   hasSubmenu: true,
  //   //   submenuItems: [
  //   //     { id: 'important', label: 'Mark important', icon: 'label_important', action: () => this.handleMarkImportant() },
  //   //     { id: 'star', label: 'Add star', icon: 'star', action: () => this.handleAddStar() },
  //   //     { id: 'filter', label: 'Filter messages like these', icon: 'filter_list', action: () => this.handleFilter() }
  //   //   ]
  //   // }
  // ];


  // handleFilter(): void {
  //   this.showNotification('Filter created');
  // }

  // private showNotification(message: string): void {
  //   this.snackBar.open(message, 'Close', {
  //     duration: 3000,
  //     horizontalPosition: 'end'
  //   });
  // }

  // SwitchValue: String = 'card';
  // // @Output() variableChange: EventEmitter<string> = new EventEmitter<string>();

  // handleMainScreenView(newLayout: string): void {
  //   console.log('Selected layout:', newLayout);
  //   this.SwitchValue = newLayout;
  //   // const newChangeVariable = newLayout;
  //   // this.variableChange.emit(newChangeVariable);
  }

  //////////////////////end toolbar/////////////
  ///////////////////end main screen //////////////////////////////////////

// }


//table //////////////////////////////////

interface PeriodicElement {
  name: string;
  position: number;
  weight: number;
  symbol: string;
  selectedOption: string;
}

//table end ///

interface InventoryItem {
  id: number;
  name: string;
  quantity: number;
  price: number;
  category: string;
  lastUpdated: Date;
  threshold: number;
}