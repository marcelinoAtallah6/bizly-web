import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatStepperModule } from '@angular/material/stepper';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialogModule } from '@angular/material/dialog';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

// icons
import { TablerIconsModule } from 'angular-tabler-icons';
import * as TablerIcons from 'angular-tabler-icons/icons';

import { AuthenticationRoutes } from './authentication.routing';

import { AppSideLoginComponent } from './login/login.component';
import { AppSideRegisterComponent } from './register/register.component';
import { AppForgotPasswordComponent } from './forgot-password/forgot-password.component';
import { WelcomeWizardComponent } from './welcome/welcome-wizard.component';
import { RegisterBusinessComponent } from './register-business/register-business.component';
import { BusinessRegistrationPendingDialogComponent } from './register-business/business-registration-pending-dialog.component';
import { PendingBusinessApprovalComponent } from './pending-business-approval/pending-business-approval.component';
import { VerifyEmailComponent } from './verify-email/verify-email.component';

@NgModule({
  imports: [
    CommonModule,
    RouterModule.forChild(AuthenticationRoutes),
    MatIconModule,
    MatCardModule,
    MatInputModule,
    MatFormFieldModule,
    MatCheckboxModule,
    MatButtonModule,
    MatStepperModule,
    MatProgressSpinnerModule,
    MatDialogModule,
    FormsModule,
    ReactiveFormsModule,
    TablerIconsModule.pick(TablerIcons),
  ],
  declarations: [
    AppSideLoginComponent,
    AppSideRegisterComponent,
    AppForgotPasswordComponent,
    WelcomeWizardComponent,
    RegisterBusinessComponent,
    BusinessRegistrationPendingDialogComponent,
    PendingBusinessApprovalComponent,
    VerifyEmailComponent,
  ],
})
export class AuthenticationModule {}
