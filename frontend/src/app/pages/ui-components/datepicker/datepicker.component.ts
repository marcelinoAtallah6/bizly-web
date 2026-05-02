import { Component, Input } from '@angular/core';
import { UntypedFormGroup, UntypedFormControl } from '@angular/forms';

@Component({
  selector: 'custom-datepicker',
  templateUrl: './datepicker.component.html',
  styleUrl: './datepicker.component.scss'
})
export class DatepickerComponent {
  @Input() label: string = 'Birth Date';
  @Input() placeholder: string = 'Choose a date';
  @Input() isRequired: boolean = false;
  @Input() public parentForm?: UntypedFormGroup;
  @Input() public fieldName: string = ''; // used to set formControl field name

  value: Date | null = null;

  get formField(): UntypedFormControl {
    if (this.fieldName !== '' && this.parentForm) {
      return this.parentForm.get(this.fieldName) as UntypedFormControl;
    }
    throw new Error(`FormControl not found for field name: ${this.fieldName}`);
  }

  onChange = (value: Date | null) => {};
  onTouched = () => {};
  
  updateValue(value: Date | null): void {
    this.value = value;
    this.onChange(value);
    this.onTouched();
  }

}
