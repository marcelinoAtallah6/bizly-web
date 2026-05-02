// import { Component, Input } from '@angular/core';

// export interface Tab {
//   label: string;      // Tab label text
//   icon: string;       // Icon name for the tab
//   content: string;    // Content to display inside the tab
// }

// @Component({
//   selector: 'app-tabs',
//   standalone: true,
//   imports: [],
//   templateUrl: './tabs.component.html',
//   styleUrl: './tabs.component.scss'
// })
// export class TabsComponent {
//   @Input() tabs: Tab[] = []; // Array of tabs
//   @Input() dynamicHeight: boolean = false; // Optional feature: dynamic height adjustment

//   ngOnInit(): void {
//     if (!this.tabs || this.tabs.length === 0) {
//       console.warn('No tabs provided!');
//     }
//   }
// }
