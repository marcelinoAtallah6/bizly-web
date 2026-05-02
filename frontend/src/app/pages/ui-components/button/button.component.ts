import { Component, Input } from '@angular/core';

@Component({
  selector: 'custom-button',
  templateUrl: './button.component.html',
  styleUrls: ['./button.component.scss'],
})
export class ButtonComponent {
  // Input properties to make the button configurable
  @Input() name: string = 'Click Me';
  @Input() color :any ='primary'// | 'accent' | 'warn' = 'primary';
  @Input() type: 'button' | 'submit' | 'reset' = 'button';
  @Input() disabled: boolean = false; // To support a disabled state
  @Input() isMiniFab: boolean = false; // Determine if it's a mini FAB
  @Input() ariaLabel: string = ''; // ARIA label for accessibility
  @Input() icon: string = ''; // Icon to display in the mini FAB
}
