import { Component, Input } from '@angular/core';
import { FormControl, UntypedFormControl, UntypedFormGroup } from '@angular/forms';
import { startWith, map } from 'rxjs';

@Component({
  selector: 'custom-autocomplete',
  templateUrl: './autocomplete.component.html',
  styleUrl: './autocomplete.component.scss'
})
export class AutocompleteComponent {
  @Input() options: string[] = []; // List of options for autocomplete
  @Input() placeholder: string = ''; // Placeholder text
  @Input() label: string = ''; // Label for the field
  @Input() type: string = 'text'; // Input type (e.g., text, number)
  @Input() isRequired: boolean = false; // Whether the field is required
  @Input() public parentForm?: UntypedFormGroup;
  @Input() public fieldName: string = ''; // used to set formControl field name  @Input() hint: string = ''; // Hint message
  @Input() hint: string = ''; // Hint message
  @Input() errorMessage: string = 'This field is required.'; // Error message

  get formField(): UntypedFormControl {
    if (this.fieldName !== '' && this.parentForm) {
      return this.parentForm.get(this.fieldName) as UntypedFormControl;
    }
    throw new Error(`FormControl not found for field name: ${this.fieldName}`);
  }



  filteredOptions: string[] = []; // Filtered options for autocomplete

  ngOnInit() {
    this.filteredOptions = this.options;

    // Listen to changes and filter options based on input
    this.formField.valueChanges
      .pipe(
        startWith(''),
        map(value => this.filter(value || ''))
      )
      .subscribe(filtered => {
        this.filteredOptions = filtered;
      });
  }

  private filter(value: string): string[] {
    const filterValue = value.toLowerCase();
    return this.options.filter(option =>
      option.toLowerCase().includes(filterValue)
    );
  }

  filterOptions(): void {
    const inputValue = this.formField.value || '';
    this.filteredOptions = this.filter(inputValue);
  }
}
