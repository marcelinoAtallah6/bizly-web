import { BreakpointObserver } from '@angular/cdk/layout';
import { Component, OnInit, ViewChild } from '@angular/core';
import { Subscription } from 'rxjs';
import { MatSidenav } from '@angular/material/sidenav';

const MOBILE_VIEW = 'screen and (max-width: 768px)';
const TABLET_VIEW = 'screen and (min-width: 769px) and (max-width: 1024px)';
const MONITOR_VIEW = 'screen and (min-width: 1024px)';

@Component({
  selector: 'app-full',
  templateUrl: './full.component.html',
  styleUrls: [],
})
export class FullComponent implements OnInit {

  @ViewChild('leftsidenav')
  public sidenav: MatSidenav | any;
  @ViewChild('sidenav') customizeSidenav!: MatSidenav;

  //get options from service
  private layoutChangesSubscription = Subscription.EMPTY;
  isMobileScreen = false;
  private isContentWidthFixed = true;
  private isCollapsedWidthFixed = false;
  private htmlElement!: HTMLHtmlElement;
  public isOpened = false;
  public profileBoxOnHover = false;
  public profileBoxOnClick = false;
  isMini: boolean = true;  // Tracks if the sidenav is in mini (collapsed) state
  sCustomizeSidenav: boolean = true;  
  public isVertical: boolean = true;
  /** When true on mobile, the drawer overlay is open (hamburger menu). */
  mobileNavOpen = false;
  get isOver(): boolean {
    return this.isMobileScreen;
  }

  constructor(private breakpointObserver: BreakpointObserver) {
    this.htmlElement = document.querySelector('html')!;
    this.layoutChangesSubscription = this.breakpointObserver
      .observe([MOBILE_VIEW, TABLET_VIEW, MONITOR_VIEW])
      .subscribe((state) => {
        // SidenavOpened must be reset true when layout changes

        const wasMobile = this.isMobileScreen;
        this.isMobileScreen = state.breakpoints[MOBILE_VIEW];
        if (this.isMobileScreen && !wasMobile) {
          this.mobileNavOpen = false;
          this.isMini = true;
        }
        if (!this.isMobileScreen && wasMobile) {
          this.mobileNavOpen = false;
        }

        this.isContentWidthFixed = state.breakpoints[MONITOR_VIEW];
      });
  }

  ngOnInit(): void {}

  ngOnDestroy() {
    this.layoutChangesSubscription.unsubscribe();
  }
  onSidenavOpenedHoverEnter(){  
          
    // Select the element with the class 'profile-box'
    const profileBox = document.querySelector('.profile-box') as HTMLElement;
    // Check if the element exists and then apply styles
    if (this.profileBoxOnHover && !this.profileBoxOnClick) {
      // Set the display property to 'block'
      this.profileBoxOnHover = false;
      profileBox.style.display = '';
    } else if( !this.profileBoxOnClick){
      this.profileBoxOnHover = true;
      profileBox.style.display = 'block';
    }
  }

  onSidenavOpenedHoverLeave(){  
          
    // Select the element with the class 'profile-box'
    const profileBox = document.querySelector('.profile-box') as HTMLElement;

    // Check if the element exists and then apply styles
    if (this.profileBoxOnHover && !this.profileBoxOnClick) {
      // Set the display property to 'block'
      this.profileBoxOnHover = false;
      profileBox.style.display = '';
    } else if( !this.profileBoxOnClick){
      this.profileBoxOnHover = true;
      profileBox.style.display = 'block';
    }
  }


  toggleCollapsed() {
    this.isContentWidthFixed = false;
  }

  onSidenavOpenedChange(isOpened: boolean) {
    this.isCollapsedWidthFixed = !this.isOver;
    this.isOpened = isOpened;
    if (this.isOver) {
      this.mobileNavOpen = isOpened;
      this.isMini = !isOpened;
    }
  }
  onSidenavClosedStart() {
    this.isContentWidthFixed = false;
    if (this.isOver) {
      this.mobileNavOpen = false;
      this.isMini = true;
    } else if (!this.isOver) {
      this.isMini = false;
    }
  }

  toggleSidenav() {
    if (this.isOver) {
      this.mobileNavOpen = !this.mobileNavOpen;
      this.isMini = !this.mobileNavOpen;
      return;
    }

    const profileBox = document.querySelector('.profile-box') as HTMLElement;
    if (this.profileBoxOnClick) {
      this.profileBoxOnClick = false;
      if (profileBox) {
        profileBox.style.display = '';
      }
    } else {
      this.profileBoxOnClick = true;
      if (profileBox) {
        profileBox.style.display = 'block';
      }
    }

    if (this.isMini || !this.sidenav?.opened) {
      this.isMini = false;
    } else {
      this.isMini = true;
    }
  }
  // @ViewChild('sidenav') sidenav!: MatSidenav;

  toggleCustomizeSidenav(sidenav:any) {

    if (this.sCustomizeSidenav || !sidenav.opened) {
      // this.isMini = false;  // Set to full state
      // sidenav.open();       // Open the sidenav
      // sidenav.isOver = true;
      this.customizeSidenav.toggle();


    } else {
      // If it's fully opened, collapse to mini state
      this.sCustomizeSidenav = true;


    }

  }
  
  handleAction(eventData: any): void {
    this.isVertical = eventData;
    this.sidenav.isOver = true;

  }

}
