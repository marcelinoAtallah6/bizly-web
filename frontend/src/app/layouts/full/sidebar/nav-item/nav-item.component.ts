import { Component, Input, OnChanges } from '@angular/core';
import { NavItem } from './nav-item';
import { Router } from '@angular/router';
import { NavService } from '../../../../services/nav.service';

@Component({
  selector: 'app-nav-item',
  templateUrl: './nav-item.component.html',
  styleUrls: [],
})
export class AppNavItemComponent implements OnChanges {
  @Input() item: NavItem | any;
  @Input() depth: any;
  expandedItems: { [key: string]: boolean } = {};

  constructor(public navService: NavService, public router: Router) {
    if (this.depth === undefined) {
      this.depth = 0;
    }
  }

  ngOnChanges() {
  }

  onItemSelected(item: NavItem) {
    //set the menus child with there values
    // this.sharedService.setMenus(item.menus);

    //navigate
    this.router.navigate([item.route]);

    // scroll
    document.querySelector('.page-wrapper')?.scroll({
      top: 0,
      left: 0,
    });
  }

  toggleSubMenu(item: any, event: Event) {
    event.stopPropagation(); // Prevent click from propagating
    if (item.menus?.length) {
      this.expandedItems[item.name] = !this.expandedItems[item.name];
    } else {
      this.onItemSelected(item);
    }
  }
  isExpanded(item: any): boolean {
    return this.expandedItems[item.name] || false;
  }
}
