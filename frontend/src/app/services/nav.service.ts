import { Injectable } from '@angular/core';
import { Event, NavigationEnd, Router } from '@angular/router';
import { BehaviorSubject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class NavService {

    public currentUrl = new BehaviorSubject<string | undefined>(undefined);

    constructor(private router: Router) {
        // Capture the initial route explicitly
        const initialNavigation = this.router.getCurrentNavigation();
        if (initialNavigation && initialNavigation.finalUrl) {
            this.currentUrl.next(initialNavigation.finalUrl.toString());
        } else {
            // Fallback to the current URL
            this.currentUrl.next(this.router.url);
        }

        // Subscribe to future navigation events
        this.router.events.subscribe((event: Event) => {
            if (event instanceof NavigationEnd) {
                this.currentUrl.next(event.urlAfterRedirects);
            }
        });
    }
}
