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
  expandedItems: { [key: number]: boolean } = {};

  constructor(public navService: NavService, public router: Router) {
    if (this.depth === undefined) {
      this.depth = 0;
    }
  }

  ngOnChanges() {
  }

  tablerIcon(icon: string | null | undefined): string | null {
    if (icon == null) {
      return null;
    }
    const t = icon.trim();
    if (t.length < 2 || !/^[a-z][a-z0-9-]*$/i.test(t)) {
      return null;
    }
    return t;
  }

  onItemSelected(item: NavItem) {
    if (item.menus?.length) {
      return;
    }
    const route = (item.route ?? '').trim();
    if (!route) {
      return;
    }
    const url = route.startsWith('/') ? route : `/${route}`;
    this.router.navigateByUrl(url).catch(() => {
      /* invalid or unknown route — avoid freezing the app with unhandled router errors */
    });

    document.querySelector('.page-wrapper')?.scroll({
      top: 0,
      left: 0,
    });
  }

  toggleSubMenu(item: any, event: Event) {
    event.stopPropagation();
    if (item.menus?.length) {
      const key = this.expandKey(item);
      this.expandedItems[key] = !this.expandedItems[key];
    } else {
      this.onItemSelected(item);
    }
  }

  isExpanded(item: any): boolean {
    return this.expandedItems[this.expandKey(item)] || false;
  }

  private expandKey(item: any): number {
    return item?.id != null ? Number(item.id) : 0;
  }
}
