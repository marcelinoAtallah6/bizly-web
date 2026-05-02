import { Component, Input } from '@angular/core';
import { UntypedFormGroup, UntypedFormControl } from '@angular/forms';

@Component({
  selector: 'custom-radio',
  templateUrl: './radio.component.html',
  styleUrl: './radio.component.scss'
})
export class RadioComponent {

  @Input() label: string = 'Select an option';
  @Input() public parentForm?: UntypedFormGroup;
  @Input() public fieldName: string = ''; // formControl field name
  @Input() options: { label: string, value: string | number }[] = [];

  value: string | number | null = null;

  get formField(): UntypedFormControl {
    if (this.fieldName !== '' && this.parentForm) {
      return this.parentForm.get(this.fieldName) as UntypedFormControl;
    }
    throw new Error(`FormControl not found for field name: ${this.fieldName}`);
  }

  // ControlValueAccessor methods
  onChange = (value: string | number | null) => {};
  onTouched = () => {};

  updateValue(value: string | number | null): void {
    this.value = value;
    this.onChange(value);
    this.onTouched();
  }

}
