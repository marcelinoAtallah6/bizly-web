import { Component, Input, ViewEncapsulation } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR, UntypedFormControl, UntypedFormGroup } from '@angular/forms';

@Component({
  selector: 'input-field',
  templateUrl: './input.component.html',
  encapsulation: ViewEncapsulation.None,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: InputComponent,
      multi: true
    }
  ]
})
export class InputComponent implements ControlValueAccessor {
  @Input() label: string = '';
  @Input() placeholder: string = '';
  @Input() type: string = 'text';
  @Input() isRequired: boolean = false;
  @Input() minLength: number | null = null;
  @Input() maxLength: number | null = null;
  @Input() public parentForm?: UntypedFormGroup;
  @Input() public fieldName: string = ''; // used to set formControl field name
  @Input() public radioData: any;
  @Input() public disable: any = false; // used to disable input element
  @Input() public readonly: any; // used to set readOnly on input element
  @Input() public icon : any; // New input to define columns per row

  value: string = '';

  get formField(): UntypedFormControl {
    if (this.fieldName !== '' && this.parentForm) {
      return this.parentForm.get(this.fieldName) as UntypedFormControl;
    }
    throw new Error(`FormControl not found for field name: ${this.fieldName}`);
  }

  // Callbacks for ControlValueAccessor
  onChange = (value: string) => { };
  onTouched = () => { };

  writeValue(value: string): void {
    this.value = value;
  }

  registerOnChange(fn: (value: string) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  updateValue(value: string): void {
    this.value = value;
    this.onChange(value);
    this.onTouched();
  }
}




// fields = [
//   { label: 'Name', icon: 'person', placeholder: 'John Doe', type: 'text', value: '' },
//   { label: 'Company', icon: 'business', placeholder: 'ACME Inc.', type: 'text', value: '' },
//   { label: 'Email', icon: 'mail', placeholder: 'john.doe@example.com', type: 'email', value: '' },
//   { label: 'Phone No', icon: 'phone', placeholder: '123 4561 123', type: 'tel', value: '' },
//   { label: 'password', icon: 'password', placeholder: 'Passwords', type: 'password', value: '' },

//   { label: 'Message', icon: 'message', placeholder: 'Hi, do you have a moment to talk?', type: 'textarea', value: '' }
// ];