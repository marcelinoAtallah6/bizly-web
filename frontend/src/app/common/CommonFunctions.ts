import { Injectable } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class CommonFunctions {

  constructor(private router: Router, private route: ActivatedRoute) {}

  public reloadPage(value: any) {
    // Global function to reload a given page
    this.router.routeReuseStrategy.shouldReuseRoute = () => false;
    this.router.onSameUrlNavigation = 'reload';
    this.router.navigate(['.' + value], {
      relativeTo: this.route,
    });
  }

  public navigateToPage(value: any) {
    // Routing navigator to any given page
    if(typeof(value) == 'undefined') {
      value = '';
    }

    if(value != '') {
      this.router.navigate([value]);
    }
  }

  public customAlertMsg(message: any, typeOfMsg: any) {
    if(typeOfMsg == 'success') {
      return "<i class='fa fa-check-circle'></i>" + " " + "<span>" + message + "</span>";
    }
    if(typeOfMsg == 'error') {
      return "<i class='fa fa-times-circle'></i>" + " " + "<span>" + message + "</span>";
    }
    if(typeOfMsg == 'info') {
      return "<i class='fa fa-info-circle'></i>" + " " + "<span>" + message + "</span>";
    }
    if(typeOfMsg == '') {
      return "<span>" + message + "</span>";
    }
  }
}