import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from 'src/app/services/auth.service';

interface WelcomeStep {
  icon: string;
  title: string;
  subtitle: string;
  body: string;
}

/**
 * Full-screen onboarding wizard shown to brand-new users (first_login = 1).
 * Calls {@code /auth/welcome-complete} on finish, then routes the user to
 * the business-registration screen if they don't have a business yet, or
 * to the dashboard otherwise.
 */
@Component({
  selector: 'app-welcome-wizard',
  templateUrl: './welcome-wizard.component.html',
  styleUrls: ['./welcome-wizard.component.scss'],
})
export class WelcomeWizardComponent {
  /* Each step renders full-screen; the carousel is animated via CSS transform on the wrapper. */
  readonly steps: WelcomeStep[] = [
    {
      icon: 'sparkles',
      title: 'Welcome to Bizly',
      subtitle: 'A modern operating system for your business',
      body:
        'Bizly brings together customers, sales, inventory, bookings and broadcasting in one place so you can spend more time growing your business.',
    },
    {
      icon: 'building-store',
      title: 'Set up your business',
      subtitle: 'Tell us a little about who you are',
      body:
        'After this wizard you will create your business workspace. Everything you do in Bizly is automatically scoped to your business — your data is never visible to anyone outside of it.',
    },
    {
      icon: 'shield-check',
      title: 'Security by default',
      subtitle: 'Your account is protected',
      body:
        'Roles, audit logs, session tracking and tenant isolation are turned on out of the box. You only see what you are allowed to see, and every change is logged.',
    },
    {
      icon: 'rocket',
      title: "You're ready",
      subtitle: 'Let’s get started',
      body:
        'On the next screen, create your business. After that the dashboard, calendar, inventory and everything else become available.',
    },
  ];

  currentStep = 0;
  isFinishing = false;
  errorMessage = '';

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}

  next(): void {
    if (this.currentStep < this.steps.length - 1) {
      this.currentStep += 1;
    }
  }

  previous(): void {
    if (this.currentStep > 0) {
      this.currentStep -= 1;
    }
  }

  goToStep(i: number): void {
    if (i >= 0 && i < this.steps.length) {
      this.currentStep = i;
    }
  }

  finish(): void {
    if (this.isFinishing) return;
    this.isFinishing = true;
    this.errorMessage = '';

    this.authService.completeWelcome().subscribe({
      next: () => {
        const bid = this.authService.getCachedBusinessId();
        const role = this.authService.getCachedRoleLevel();
        if (bid != null || (role && role.toUpperCase() === 'ADMIN')) {
          this.router.navigateByUrl('/dashboard');
        } else {
          this.router.navigateByUrl('/authentication/register-business');
        }
      },
      error: (err) => {
        this.isFinishing = false;
        this.errorMessage = err?.error?.message ?? 'Could not finish the welcome wizard.';
      },
    });
  }
}
