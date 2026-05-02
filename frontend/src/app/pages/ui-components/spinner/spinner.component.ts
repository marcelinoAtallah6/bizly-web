import { Component, Input } from '@angular/core';

@Component({
  selector: 'custom-spinner',
  templateUrl: './spinner.component.html',
  styleUrl: './spinner.component.scss'
})
export class SpinnerComponent {
  // Inputs to configure the spinner
  @Input() spinnerMode: 'indeterminate' | 'determinate' = 'indeterminate';
  @Input() spinnerColor: 'primary' | 'accent' | 'warn' = 'primary';

  // Inputs to control background and header visibility
  @Input() withBackground: boolean = false;

  // Optional header text input
  @Input() label: string = 'Loading...';
}
