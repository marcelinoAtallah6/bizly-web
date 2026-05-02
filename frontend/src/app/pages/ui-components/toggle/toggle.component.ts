import { Component, Input } from '@angular/core';
import { FormGroup, FormControl } from '@angular/forms';

@Component({
  selector: 'custom-toggle',
  templateUrl: './toggle.component.html',
  styleUrl: './toggle.component.scss'
})
export class ToggleComponent {
  @Input() label: string = 'Slide me!'; // Label for the toggle
  @Input() fieldName: string = ''; // FormControl name
  @Input() parentForm!: FormGroup; // Parent FormGroup
  @Input() isRequired: boolean = false; // Parent FormGroup

  formField!: FormControl; // FormControl instance

  ngOnInit(): void {
    if (this.parentForm && this.fieldName) {
      this.formField = this.parentForm.get(this.fieldName) as FormControl;
    } else {
      throw new Error(`FormControl not found for field: ${this.fieldName}`);
    }
  }

  // Show error only when touched or dirty
  get showError(): boolean {
    return this.formField.invalid && (this.formField.touched || this.formField.dirty);
  }
}
