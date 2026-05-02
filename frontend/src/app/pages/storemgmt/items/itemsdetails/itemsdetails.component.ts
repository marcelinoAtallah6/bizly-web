import { Component, Inject } from '@angular/core';
import { UntypedFormGroup, UntypedFormBuilder, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Config } from 'src/app/pages/ui-components/Custom/detail-screen/detail-screen.component';

@Component({
  selector: 'app-itemsdetails',
  templateUrl: './itemsdetails.component.html',
  styleUrl: './itemsdetails.component.scss'
})
export class ItemsdetailsComponent {

 
  public config: Config;
  itemForm: UntypedFormGroup;
  
  constructor(private fb: UntypedFormBuilder,@Inject(MAT_DIALOG_DATA) public data: any) {
        // Initialize config from dialog data
        this.config = data.data;
        console.log("config = ",this.config)

        this.itemForm= this.fb.group({
          itemName: ['', Validators.required],
          description: [''],
          price: [''],
          quatity: [],
          category: [''],
          productCode:[''],
          branch:['']
        });
  }

  categoryOption = [
    { id: 1, name: 'Ge;' },
    { id: 2, name: 'Brush' },
  ];

    onSubmit(){}

}
