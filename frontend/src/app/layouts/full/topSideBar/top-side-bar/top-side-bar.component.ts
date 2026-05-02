import { Component, Input } from '@angular/core';
import axios from 'axios';
import { GlobalConstants } from 'src/app/common/GlobalConstants';
import { NavService } from 'src/app/services/nav.service';
import { NavGroupItem } from '../../sidebar/nav-item/nav-item';

@Component({
  selector: 'app-top-side-bar',
  templateUrl: './top-side-bar.component.html',
  styleUrl: './top-side-bar.component.scss'
})
export class TopSideBarComponent {

  navItems: NavGroupItem[] = []; // Initialize as an empty array

  constructor(public navService: NavService) {}

  ngOnInit(): void {
    this.getNavItems();
  }
  

  private apiUrl = GlobalConstants.API_ENDPOINTS.application.getAll; // Update the URL if necessary

  async getNavItems():  Promise<void>  {
    try {
      const response = await axios.get<NavGroupItem[]>(this.apiUrl);
      this.navItems = response.data; // Assign the resolved data to navItems
    } catch (error) {
      console.error('Error fetching nav items:', error);
    }
  }



}
