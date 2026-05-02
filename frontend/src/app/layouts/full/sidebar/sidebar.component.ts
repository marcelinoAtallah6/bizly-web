import { Component, Input, OnInit } from '@angular/core';
import { NavService } from '../../../services/nav.service';
import axios from 'axios';
import { GlobalConstants } from 'src/app/common/GlobalConstants';
import { NavGroupItem, NavItem } from './nav-item/nav-item';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
})
export class SidebarComponent implements OnInit {
  navItems: NavGroupItem[] = []; // Initialize as an empty array
  @Input() isVertical: boolean | undefined;
  public filteredMenus: NavItem[] = [];

  constructor(public navService: NavService) { }

  async ngOnInit(): Promise<void> {
    await this.getNavItems();

    // this.navService.currentUrl.subscribe((url: any) => {
    //   this.filterMenusByRoute(url);
    // });
  }

  private apiUrl = GlobalConstants.API_ENDPOINTS.application.getAll; // Update the URL if necessary

  async getNavItems(): Promise<void> {
    try {
      const response = await axios.get<NavGroupItem[]>(this.apiUrl);
      this.navItems = response.data; // Assign the resolved data to navItems
    } catch (error) {
      console.error('Error fetching nav items:', error);
    }
  }

  // filterMenusByRoute(route: string): void {

  //   if (!this.navItems || this.navItems.length === 0) {
  //     console.log('navItems is empty or undefined.');
  //     return;
  //   }

    // const filteredMenus: NavItem[] = [];
    // this.navItems.forEach((group) => {
    //   group.applications?.forEach((application) => {
    //     if (application.route === route) {
    //       filteredMenus.push(application);
    //     }

    //     application.menus?.forEach((menu) => {
    //       if (menu.route === route) {
    //         filteredMenus.push(menu);
    //       }
    //     });
    //   });
    // });

    // this.filteredMenus = filteredMenus;
    // this.sharedService.setMenus(this.filteredMenus[0].menus);
  // }



}


