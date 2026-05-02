import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class SharedService {
  private menus = new BehaviorSubject<any>([]);
  variable$ = this.menus.asObservable();

  setMenus(newValue: any) {
    this.menus.next(newValue);
  }
}
