import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize } from 'rxjs';
import { ToolbarButton } from 'src/app/pages/ui-components/button/toolbar/toolbar.component';
import { KycCustomerService } from '../../services/kyc-customer.service';

@Component({
  selector: 'app-customer-form',
  templateUrl: './customer-form.component.html',
  styleUrl: './customer-form.component.scss',
})
export class CustomerFormComponent implements OnInit {
  form!: FormGroup;
  mode: 'create' | 'edit' = 'create';
  customerId: number | null = null;
  loading = false;
  saving = false;

  private readonly mobilePattern = '^\\+?[0-9]{7,15}$';

  readonly customerStatusOptions = ['ACTIVE', 'INACTIVE', 'PENDING', 'SUSPENDED'];

  get formToolbar(): ToolbarButton[] {
    return [{ id: 'back', icon: 'arrow_back', tooltip: 'Back to list', action: () => this.cancel() }];
  }

  constructor(
    private readonly fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly kycCustomerService: KycCustomerService
  ) {}

  ngOnInit(): void {
    this.mode = (this.route.snapshot.data['mode'] as 'create' | 'edit') ?? 'create';
    const idParam = this.route.snapshot.paramMap.get('id');
    this.customerId = idParam ? Number(idParam) : null;

    this.form = this.fb.group({
      firstName: ['', [Validators.required, Validators.maxLength(100)]],
      lastName: ['', [Validators.required, Validators.maxLength(100)]],
      dob: [null as Date | null, Validators.required],
      email: ['', [Validators.required, Validators.email, Validators.maxLength(255)]],
      mobileNumber: ['', [Validators.required, Validators.pattern(this.mobilePattern)]],
      addressLine1: ['', [Validators.maxLength(255)]],
      addressLine2: ['', [Validators.maxLength(255)]],
      city: ['', [Validators.maxLength(100)]],
      stateProvince: ['', [Validators.maxLength(100)]],
      postalCode: ['', [Validators.maxLength(20)]],
      country: ['', [Validators.maxLength(100)]],
      customerStatus: ['ACTIVE', [Validators.required, Validators.maxLength(20)]],
    });

    if (this.mode === 'edit' && this.customerId != null && !Number.isNaN(this.customerId)) {
      this.loadCustomer(this.customerId);
    }
  }

  cancel(): void {
    this.router.navigate(['/kyc/customers']);
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const v = this.form.getRawValue();
    const dobDate = v.dob instanceof Date ? v.dob : new Date(v.dob);
    const dob = `${dobDate.getFullYear()}-${String(dobDate.getMonth() + 1).padStart(2, '0')}-${String(
      dobDate.getDate()
    ).padStart(2, '0')}`;

    const trim = (s: string) => (typeof s === 'string' ? s.trim() : '');
    const opt = (s: string) => {
      const t = trim(s);
      return t.length ? t : undefined;
    };

    const base = {
      firstName: trim(v.firstName),
      lastName: trim(v.lastName),
      dob,
      email: trim(v.email),
      mobileNumber: trim(v.mobileNumber),
      addressLine1: opt(v.addressLine1),
      addressLine2: opt(v.addressLine2),
      city: opt(v.city),
      stateProvince: opt(v.stateProvince),
      postalCode: opt(v.postalCode),
      country: opt(v.country),
      customerStatus: trim(v.customerStatus),
    };

    this.saving = true;
    if (this.mode === 'create') {
      this.kycCustomerService
        .add(base)
        .pipe(finalize(() => (this.saving = false)))
        .subscribe({
          next: (res) => {
            this.router.navigate(['/kyc/customers', res.id]);
          },
          error: () => {},
        });
    } else if (this.customerId != null) {
      this.kycCustomerService
        .update({ ...base, id: this.customerId })
        .pipe(finalize(() => (this.saving = false)))
        .subscribe({
          next: () => {
            this.router.navigate(['/kyc/customers', this.customerId]);
          },
          error: () => {},
        });
    }
  }

  private loadCustomer(id: number): void {
    this.loading = true;
    this.kycCustomerService
      .get({ id })
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (row) => {
          let dob: Date | null = null;
          if (row.dob) {
            const parts = String(row.dob).split('-').map(Number);
            if (parts.length === 3) {
              dob = new Date(parts[0], parts[1] - 1, parts[2]);
            }
          }
          this.form.patchValue({
            firstName: row.firstName ?? '',
            lastName: row.lastName ?? '',
            dob,
            email: row.email ?? '',
            mobileNumber: row.mobileNumber ?? '',
            addressLine1: row.addressLine1 ?? '',
            addressLine2: row.addressLine2 ?? '',
            city: row.city ?? '',
            stateProvince: row.stateProvince ?? '',
            postalCode: row.postalCode ?? '',
            country: row.country ?? '',
            customerStatus: row.customerStatus ?? 'ACTIVE',
          });
        },
        error: () => {},
      });
  }
}
