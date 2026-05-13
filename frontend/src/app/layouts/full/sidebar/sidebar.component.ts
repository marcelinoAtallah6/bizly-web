import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { NavService } from '../../../services/nav.service';
import { NavGroupItem, NavItem } from './nav-item/nav-item';
import { AuthService } from 'src/app/services/auth.service';
import { NavbarProfileView, UserProfileService } from 'src/app/services/user-profile.service';
import { MenuCatalogService } from 'src/app/services/menu-catalog.service';

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
    private readonly menuCatalog: MenuCatalogService
  ) {}

  private menuSub?: Subscription;

  async ngOnInit(): Promise<void> {
    this.userProfile.refresh();
    this.profileSub = this.userProfile.profile$.subscribe((p) => {
      this.sidebarProf = p;
    });
    // Subscribe to the shared catalog: the service handles role-refresh
    // reloads internally, so we only own the binding to view state.
    this.menuSub = this.menuCatalog.groups$.subscribe((groups) => {
      this.navItems = groups;
    });
    await this.menuCatalog.ensureLoaded();
  }

  ngOnDestroy(): void {
    this.profileSub?.unsubscribe();
    this.menuSub?.unsubscribe();
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


