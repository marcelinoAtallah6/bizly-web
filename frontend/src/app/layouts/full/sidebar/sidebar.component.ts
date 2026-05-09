import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import { Router } from '@angular/router';
import { finalize, firstValueFrom } from 'rxjs';
import { NavService } from '../../../services/nav.service';
import { GlobalConstants } from 'src/app/common/GlobalConstants';
import { NavGroupItem, NavItem } from './nav-item/nav-item';
import { AuthService } from 'src/app/services/auth.service';
import { NavbarProfileView, UserProfileService } from 'src/app/services/user-profile.service';
import { BusinessApiService } from 'src/app/services/business-api.service';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss',
})
export class SidebarComponent implements OnInit, OnDestroy {
  navItems: NavGroupItem[] = []; 
  @Input() isVertical: boolean | undefined;
  public filteredMenus: NavItem[] = [];
  isSigningOut = false;
  sidebarProf: NavbarProfileView | null = null;
  private profileSub?: Subscription;

  constructor(
    public navService: NavService,
    private readonly authService: AuthService,
    private readonly userProfile: UserProfileService,
    private readonly router: Router,
    private readonly businessApiService: BusinessApiService
  ) {}

  private roleRefreshSub?: Subscription;

  async ngOnInit(): Promise<void> {
    this.userProfile.refresh();
    this.profileSub = this.userProfile.profile$.subscribe((p) => {
      this.sidebarProf = p;
    });
    this.roleRefreshSub = this.authService.roleRefresh$.subscribe(() => {
      void this.getNavItems();
    });
    await this.getNavItems();
  }

  ngOnDestroy(): void {
    this.profileSub?.unsubscribe();
    this.roleRefreshSub?.unsubscribe();
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
        next: () => {
          this.userProfile.clear();
          this.router.navigate(['/authentication/login']);
        },
        error: () => {
          this.authService.clearSession();
          this.userProfile.clear();
          this.router.navigate(['/authentication/login']);
        },
      });
  }

}


