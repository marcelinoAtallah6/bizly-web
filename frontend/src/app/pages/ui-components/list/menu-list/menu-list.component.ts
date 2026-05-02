import { Component, Input, QueryList, ViewChildren } from '@angular/core';
import { MatMenu, MatMenuPanel } from '@angular/material/menu';


interface MenuItem {
  name: string;
  subItems?: MenuItem[];
}
@Component({
  selector: 'custom-menu-list',
  templateUrl: './menu-list.component.html',
  styleUrl: './menu-list.component.scss'
})
export class MenuListComponent {
  @Input() menuItems: MenuItem[] = [];


  
}
