import { Component, Input, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { finalize, firstValueFrom } from 'rxjs';
import { NavService } from '../../../services/nav.service';
import { GlobalConstants } from 'src/app/common/GlobalConstants';
import { NavGroupItem, NavItem } from './nav-item/nav-item';
import { AuthService } from 'src/app/services/auth.service';
import { BusinessApiService } from 'src/app/services/business-api.service';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
})
export class SidebarComponent implements OnInit {
  navItems: NavGroupItem[] = []; 
  @Input() isVertical: boolean | undefined;
  public filteredMenus: NavItem[] = [];
  isSigningOut = false;

  constructor(
    public navService: NavService,
    private readonly authService: AuthService,
    private readonly router: Router,
    private readonly businessApiService: BusinessApiService
  ) {}

  async ngOnInit(): Promise<void> {
    await this.getNavItems();
  }

  private apiUrl = GlobalConstants.API_ENDPOINTS.application.getAll; 

  async getNavItems(): Promise<void> {
    try {
      this.navItems = await firstValueFrom(
        this.businessApiService.post<NavGroupItem[]>(this.apiUrl, {})
      );
    } catch (error) {
      console.error('Error fetching nav items:', error);
    }
  }

  onSignOut(): void {
    if (this.isSigningOut) {
      return;
    }

    this.isSigningOut = true;
    this.authService
      .logout()
      .pipe(finalize(() => (this.isSigningOut = false)))
      .subscribe({
        next: () => this.router.navigate(['/authentication/login']),
        error: () => {
          this.authService.clearSession();
          this.router.navigate(['/authentication/login']);
        },
      });
  }

}


