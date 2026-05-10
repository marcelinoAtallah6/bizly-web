import {
  Component,
  Output,
  EventEmitter,
  Input,
  ViewEncapsulation,
  OnInit,
  OnDestroy,
} from '@angular/core';
import { Subscription } from 'rxjs';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { NavItem } from '../sidebar/nav-item/nav-item';
import { SharedService } from 'src/app/services/shared.service';
import { AuthService } from 'src/app/services/auth.service';
import { NavbarProfileView, UserProfileService } from 'src/app/services/user-profile.service';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  encapsulation: ViewEncapsulation.None,
})
export class HeaderComponent implements OnInit, OnDestroy {
  @Input() showToggle = true;
  @Input() toggleChecked = false;
  @Output() toggleMobileNav = new EventEmitter<void>();
  @Output() toggleMobileFilterNav = new EventEmitter<void>();
  @Output() toggleCollapsed = new EventEmitter<void>();

  showFiller = false;
  isSigningOut = false;
  headerProf: NavbarProfileView | null = null;
  private subs = new Subscription();

  onMenuClick(menuSelected: any) {
    this.router.navigate([menuSelected]);
  }

  roleChoices(): string[] {
    return this.authService.getJwtRoleNames();
  }

  displayRoleLabel(code: string): string {
    const u = code.toUpperCase();
    return u.startsWith('ROLE_') ? u.slice(5) : u;
  }

  activeRoleSummary(): string {
    const a = this.authService.getJwtActiveRole();
    if (!a) {
      return 'All roles';
    }
    return this.displayRoleLabel(a);
  }

  switchRole(role: string | null): void {
    this.authService.setActiveRole(role).subscribe({
      error: () => {},
    });
  }

  onSignOut() {
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

  menuItems: NavItem[] = [];

  quickLinks = [
    { label: 'Gold Page', url: '/theme-pages/pricing' },
    { label: 'Gold 2 Page', url: '/authentication/login' },
  ];

  constructor(
    public dialog: MatDialog,
    public router: Router,
    private sharedService: SharedService,
    private authService: AuthService,
    private readonly userProfile: UserProfileService
  ) {}

  ngOnInit(): void {
    this.userProfile.refresh();
    this.subs.add(
      this.userProfile.profile$.subscribe((p) => {
        this.headerProf = p;
      })
    );

    this.menuItems = [
      {
        name: 'Settings',
        icon: '/assets/images/svgs/icon-user-male.svg',
        route: '/dashboard',
        isActive: true,
      },
      {
        name: 'Settings',
        icon: '/assets/images/svgs/icon-user-male.svg',
        isActive: true,
        menus: [
          {
            name: 'General',
            icon: '/assets/images/svgs/icon-user-male.svg',
            route: '/settings/general',
          },
          {
            name: 'Users',
            icon: '/assets/images/svgs/icon-user-male.svg',
            isActive: true,
          },
        ],
      },
    ];
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }
}
