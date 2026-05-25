import { Routes } from '@angular/router';

import { AppSideLoginComponent } from './login/login.component';
import { AppSideRegisterComponent } from './register/register.component';
import { AppForgotPasswordComponent } from './forgot-password/forgot-password.component';
import { WelcomeWizardComponent } from './welcome/welcome-wizard.component';
import { RegisterBusinessComponent } from './register-business/register-business.component';
import { PendingBusinessApprovalComponent } from './pending-business-approval/pending-business-approval.component';
import { VerifyEmailComponent } from './verify-email/verify-email.component';
import { AuthGuard } from 'src/app/guards/auth.guard';

export const AuthenticationRoutes: Routes = [
  {
    path: '',
    children: [
      {
        path: 'login',
        component: AppSideLoginComponent,
      },
      {
        path: 'register',
        component: AppSideRegisterComponent,
      },
      {
        path: 'forgot-password',
        component: AppForgotPasswordComponent,
      },
      {
        path: 'verify-email',
        component: VerifyEmailComponent,
      },
      /* Onboarding screens — require an authenticated session. AuthGuard is enough; the actual
         redirect logic is enforced by the components/login flow themselves to keep this list small. */
      {
        path: 'welcome',
        component: WelcomeWizardComponent,
        canActivate: [AuthGuard],
      },
      {
        path: 'register-business',
        component: RegisterBusinessComponent,
        canActivate: [AuthGuard],
      },
      {
        path: 'pending-business-approval',
        component: PendingBusinessApprovalComponent,
        canActivate: [AuthGuard],
      },
    ],
  },
];
