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
  private isMobileScreen = false;
  private isContentWidthFixed = true;
  private isCollapsedWidthFixed = false;
  private htmlElement!: HTMLHtmlElement;
  public isOpened = false;
  public profileBoxOnHover = false;
  public profileBoxOnClick = false;
  isMini: boolean = true;  // Tracks if the sidenav is in mini (collapsed) state
  sCustomizeSidenav: boolean = true;  
  public isVertical: boolean = true;  
  get isOver(): boolean {
    return this.isMobileScreen;
  }

  constructor(private breakpointObserver: BreakpointObserver) {
    this.htmlElement = document.querySelector('html')!;
    this.layoutChangesSubscription = this.breakpointObserver
      .observe([MOBILE_VIEW, TABLET_VIEW, MONITOR_VIEW])
      .subscribe((state) => {
        // SidenavOpened must be reset true when layout changes

        this.isMobileScreen = state.breakpoints[MOBILE_VIEW];

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
    if (isOpened) {
      this.isMini = true; // Open fully
    }


  }
  onSidenavClosedStart() {
    this.isContentWidthFixed = false;
    if (!this.isOver) {
      this.isMini = false; // Collapse to mini state
    }
  }

  toggleSidenav() {

    // Select the element with the class 'profile-box'
    const profileBox = document.querySelector('.profile-box') as HTMLElement;

    // Check if the element exists and then apply styles
    if (this.profileBoxOnClick) {
      // Set the display property to 'block'
      this.profileBoxOnClick = false;
      profileBox.style.display = '';
    } else {
      this.profileBoxOnClick = true;
      profileBox.style.display = 'block';
    }


    // If the sidenav is in mini state or closed, open it fully
    if (this.isMini || !this.sidenav.opened) {
      this.isMini = false;  // Set to full state
      // sidenav.open();       // Open the sidenav
      // sidenav.isOver = true;
      

    } else {
      // If it's fully opened, collapse to mini state
      this.isMini = true;
      // sidenav.close();
      this.sidenav.isOver = false;
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
