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
import { NavItem } from '../sidebar/nav-item/nav-item';
import { SharedService } from 'src/app/services/shared.service';


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

  onMenuClick(menuSelected: any) {
    this.router.navigate([menuSelected]);

  }
  onSignOut() {
    // Perform logout logic here (e.g., clear tokens, navigate to login page)
    console.log("Signing out...");
  }
  
  menuItems: NavItem[] = [];
  quickLinks = [
    { label: 'Gold Page', url: '/theme-pages/pricing' },
    { label: 'Gold 2 Page', url: '/authentication/login' },
  ];

  constructor(public dialog: MatDialog, public router: Router, private sharedService: SharedService) { }
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
