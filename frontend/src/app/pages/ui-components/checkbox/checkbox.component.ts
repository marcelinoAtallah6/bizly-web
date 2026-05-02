import { Component, Input } from '@angular/core';
import { FormControl, UntypedFormControl, UntypedFormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'custom-checkbox',
  templateUrl: './checkbox.component.html',
  styleUrl: './checkbox.component.scss'
})
export class CheckboxComponent {

  @Input() label: string = ''; // Label for the checkbox
  @Input() fieldName: string = ''; // Used for form control field
  @Input() public parentForm?: UntypedFormGroup;

  @Input() value: boolean = false; // Default value
  @Input() disabled: boolean = false; // If the checkbox should be disabled
  @Input() isRequired: boolean = false; // If the checkbox

  get formField(): UntypedFormControl {
    if (this.fieldName !== '' && this.parentForm) {
      return this.parentForm.get(this.fieldName) as UntypedFormControl;
    }

    throw new Error(`FormControl not found for field name: ${this.fieldName}`);
  }

  get showError(): boolean {
    return this.isRequired && this.formField.invalid;
  }

  ngOnInit(): void {
  }

  // ControlValueAccessor methods
  onChange = (value: boolean) => { };
  onTouched = () => { };

  // Handle the change event
  updateValue(event: any): void {
    this.value = event.checked;
    this.onChange(this.value);
    this.onTouched();

  }

}
