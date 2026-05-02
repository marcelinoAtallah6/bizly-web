// detail-screen.component.ts
import { Component, Inject, Input, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, UntypedFormControl, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';

export interface FieldConfig {
  key: string;
  type: string;
  label: string;
  placeholder?: string;
  required: boolean;
  inputType?: string;
  options?: { id: number; name: string }[] | { label: string; value: number }[];
  multiple?: boolean;
}

export interface Config {
  cardTitle: string;
  fields: FieldConfig[];
}

@Component({
  selector: 'custom-detail-screen',
  templateUrl: './detail-screen.component.html',
  styleUrl: './detail-screen.component.scss'
})
export class DetailScreenComponent implements OnInit {
  fieldsForm!: FormGroup;
  public config: Config;
  
  constructor(
    @Inject(MAT_DIALOG_DATA) public data: any,
    private fb: FormBuilder
  ) {
    // Initialize config from dialog data
    this.config = data.data;
    console.log("config = ",this.config)
  }

  ngOnInit(): void {
    this.fieldsForm = this.fb.group({});
    this.createFormControls();
  }

 
    createFormControls() {
    if (this.config && this.config.fields) {
      this.config.fields.forEach(field => {
        const validators = field.required ? [Validators.required] : [];
        this.fieldsForm.addControl(field.key, this.fb.control('', validators));
      });
    }

  }

  onSubmit() {
    console.log('Form Submitted!', this.fieldsForm);

    if (this.fieldsForm.valid) {
      console.log('Form Submitted!', this.fieldsForm.value);
    } 
  }

  transformOptions(options: { id: number; name: string; }[] | { label: string; value: number; }[] | undefined): { label: string; value: string | number; }[] {
    if (!options) {
      return []; // Return an empty array if options are undefined
    }
  
    if (options.length > 0 && 'id' in options[0]) {
      return (options as { id: number; name: string; }[]).map(option => ({
        label: option.name,
        value: option.id
      }));
    }
    return options as { label: string; value: string | number; }[];
  }
  
}

