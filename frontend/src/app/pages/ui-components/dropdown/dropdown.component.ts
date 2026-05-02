import { Component, Input, ViewEncapsulation } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR, UntypedFormControl, UntypedFormGroup } from '@angular/forms';
import { InputComponent } from '../input/input.component';

@Component({
  selector: 'custom-dropdown',
  templateUrl: './dropdown.component.html',
  styleUrl: './dropdown.component.scss',
})
export class DropdownComponent   {

 // Input properties for flexibility
 @Input() list: any;
 @Input() icon: any;
 @Input() label: string ='';
 @Input() placeholder: string ='';
 @Input() selectedValue: any | null = null;
 @Input() public parentForm?: UntypedFormGroup;
 @Input() public fieldName: string = ''; // used to set formControl field name
 @Input() isMultiple: boolean = false; // To enable multi-selection
 value: string = '';
 @Input() isRequired: boolean = false;

 get formField(): UntypedFormControl {
  if (this.fieldName !== '' && this.parentForm) {
    return this.parentForm.get(this.fieldName) as UntypedFormControl;
  }
  throw new Error(`FormControl not found for field name: ${this.fieldName}`);
}
//  onChange(event: any) {
//  }

 
  // Callbacks for ControlValueAccessor
  onChange = (event: any) => { };
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
