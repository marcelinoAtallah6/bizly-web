import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { finalize, firstValueFrom } from 'rxjs';
import { GlobalConstants } from 'src/app/common/GlobalConstants';
import { NavService } from 'src/app/services/nav.service';
import { AuthService } from 'src/app/services/auth.service';
import { BusinessApiService } from 'src/app/services/business-api.service';
import { NavGroupItem } from '../../sidebar/nav-item/nav-item';

@Component({
  selector: 'app-top-side-bar',
  templateUrl: './top-side-bar.component.html',
  styleUrl: './top-side-bar.component.scss'
})
export class TopSideBarComponent implements OnInit {

  navItems: NavGroupItem[] = []; // Initialize as an empty array
  isSigningOut = false;

  constructor(
    public navService: NavService,
    private readonly authService: AuthService,
    private readonly router: Router,
    private readonly businessApiService: BusinessApiService
  ) {}

  ngOnInit(): void {
    this.getNavItems();
  }
  

  private apiUrl = GlobalConstants.API_ENDPOINTS.application.getAll; // Update the URL if necessary

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
        next: () => this.router.navigate(['/authentication/login']),
        error: () => {
          this.authService.clearSession();
          this.router.navigate(['/authentication/login']);
        },
      });
  }



}
