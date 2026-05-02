import { Component, EventEmitter, Input, Output } from '@angular/core';

export interface ButtonConfig {
  value: string; // The value emitted on selection
  label: string; // Text displayed for the button
  icon?: string; // Optional icon for the button
}

@Component({
  selector: 'custom-switch',
  templateUrl: './switch.component.html',
  styleUrls: ['./switch.component.scss']
})
export class SwitchComponent {
  @Input() buttons: ButtonConfig[] = []; // Input for dynamic button configuration
  @Input() selectedValue: string = ''; // Tracks the currently selected value

  @Output() selectionChange = new EventEmitter<string>(); // Emits the selected value

  onSelectionChange(value: string): void {
    this.selectedValue = value;
    this.selectionChange.emit(value);
  }
}
