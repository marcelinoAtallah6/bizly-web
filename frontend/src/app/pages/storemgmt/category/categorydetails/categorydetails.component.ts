import { Component, Inject } from '@angular/core';
import { UntypedFormGroup, UntypedFormBuilder, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Config } from 'src/app/pages/ui-components/Custom/detail-screen/detail-screen.component';

@Component({
  selector: 'app-categorydetails',
  templateUrl: './categorydetails.component.html',
  styleUrl: './categorydetails.component.scss'
})
export class CategorydetailsComponent {


  public config: Config;
  categoryForm: UntypedFormGroup;
  
  constructor(private fb: UntypedFormBuilder,@Inject(MAT_DIALOG_DATA) public data: any) {
        // Initialize config from dialog data
        this.config = data.data;
        console.log("config = ",this.config)

        this.categoryForm= this.fb.group({
          categoryName: ['', Validators.required],
          description: [''],
        });
  }

  categoryOption = [
    { id: 1, name: 'Ge;' },
    { id: 2, name: 'Brush' },
  ];

    onSubmit(){}


}
