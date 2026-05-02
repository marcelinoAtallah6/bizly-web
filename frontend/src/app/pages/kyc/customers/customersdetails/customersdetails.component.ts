import { Component, OnInit, Inject, Optional } from '@angular/core';
import { FormBuilder, FormGroup, UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Config } from 'src/app/pages/ui-components/Custom/detail-screen/detail-screen.component';
@Component({
  selector: 'app-customersdetails',
  templateUrl: './customersdetails.component.html',
  styleUrl: './customersdetails.component.scss'
})
export class CustomersdetailsComponent {
  
  public config: Config;
  customerForm: UntypedFormGroup;
  
  constructor(private fb: UntypedFormBuilder,@Inject(MAT_DIALOG_DATA) public data: any) {
        // Initialize config from dialog data
        this.config = data.data;
        console.log("config = ",this.config)

        this.customerForm= this.fb.group({
          firstName: ['', Validators.required],
          lastName: ['', Validators.required],
          gender: [''],
          birthDate: [],
          phone: [''],
          email: ['', [ Validators.email]],
          street: [''],
          city: [''],
          country: [''],
        });
  }

   

    genderOption = [
      { id: 1, name: 'Male' },
      { id: 2, name: 'Female' },
    ];

    onSubmit(){}
}
