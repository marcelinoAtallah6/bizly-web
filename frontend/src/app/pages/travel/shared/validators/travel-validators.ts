import { AbstractControl, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';

const PASSPORT_PATTERN = /^[A-Za-z0-9]{5,15}$/;

export function travelOptionalEmailValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const v = (control.value ?? '').toString().trim();
    if (!v) {
      return null;
    }
    return Validators.email(control);
  };
}

export function travelOptionalPassportValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const v = (control.value ?? '').toString().trim();
    if (!v) {
      return null;
    }
    return PASSPORT_PATTERN.test(v) ? null : { passportFormat: true };
  };
}

export function travelPassportExpiryWarningValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const v = control.value;
    if (!v) {
      return null;
    }
    const d = v instanceof Date ? v : new Date(v);
    if (Number.isNaN(d.getTime())) {
      return null;
    }
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    return d < today ? { passportExpired: true } : null;
  };
}
