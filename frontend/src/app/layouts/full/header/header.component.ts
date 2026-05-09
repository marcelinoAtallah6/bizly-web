import {
  Component,
  Output,
  EventEmitter,
  Input,
  ViewEncapsulation,
  OnInit,
} from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { NavItem } from '../sidebar/nav-item/nav-item';
import { SharedService } from 'src/app/services/shared.service';
import { AuthService } from 'src/app/services/auth.service';


@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  encapsulation: ViewEncapsulation.None,
})
export class HeaderComponent implements OnInit {
  @Input() showToggle = true;
  @Input() toggleChecked = false;
  @Output() toggleMobileNav = new EventEmitter<void>();
  @Output() toggleMobileFilterNav = new EventEmitter<void>();
  @Output() toggleCollapsed = new EventEmitter<void>();

  showFiller = false;
  isSigningOut = false;

  onMenuClick(menuSelected: any) {
    this.router.navigate([menuSelected]);

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
        next: () => this.router.navigate(['/authentication/login']),
        error: () => {
          this.authService.clearSession();
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
    private authService: AuthService
  ) {}
  ngOnInit(): void {
    // this.sharedService.variable$.subscribe((value: any) => {
    //   this.menuItems = value;
    //   console.log("menuItem======",this.menuItems)
    // });
    this.menuItems = [
      {
        "name": "Settings",
        "icon": "/assets/images/svgs/icon-user-male.svg",
        "route": "/dashboard",
        "isActive": true
      },
      {
        "name": "Settings",
        "icon": "/assets/images/svgs/icon-user-male.svg",
        "isActive": true,
        "menus": [
          {
            "name": "General",
            "icon": "/assets/images/svgs/icon-user-male.svg",
            "route": "/settings/general"
          },
          {
            "name": "Users",
            "icon": "/assets/images/svgs/icon-user-male.svg",
            "isActive": true,
          }
        ]
      }
    ]
    ;
  }

  
}
