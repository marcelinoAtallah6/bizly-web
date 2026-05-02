import {
  Component,
  Output,
  EventEmitter,
  Input,
  ViewEncapsulation,
  OnInit,
  Renderer2,
} from '@angular/core';
import { MatDialog } from '@angular/material/dialog';


@Component({
  selector: 'app-customizer',
  templateUrl: './customizer.component.html',
  encapsulation: ViewEncapsulation.None,
})
export class CustomizerComponent implements OnInit {
  selectedTheme = 'light'; // Default theme
  selectedColor = 'blue'; // Default color

  themeColors = [
    { name: 'blue', value: 'blue' },
    { name: 'aqua', value: 'aqua' },
    { name: 'purple', value: 'purple' },
    { name: '#5d87ff', value: '#5d87ff' }
  ];
  themeDirection: string = 'ltr'; // Default direction is LTR
  @Output() layoutType: any = new EventEmitter<string>();
  previousMode: any;

  toggleMode(mode: any) {
    this.renderer.removeClass(document.body, this.previousMode)
    this.renderer.addClass(document.body, mode);
    this.previousMode = mode;

  }

  toVerticalBar() {
    this.layoutType.emit(true);
  }

  toHorizontalBar() {
    this.layoutType.emit(false);
  }


  themes = [
    { name: 'Light Theme', class: 'light-theme', value: 'light' },
    { name: 'Dark Theme', class: 'dark-theme', value: 'dark' },
    { name: 'Blue Theme', class: 'blue-theme', value: 'blue' },
    // Add more themes as needed
  ];

  constructor(public dialog: MatDialog, private renderer: Renderer2) { }
  ngOnInit(): void {

  }

  onThemeChange(theme: string): void {
    this.selectedTheme = theme;
  }

  onColorChange(color: string): void {
    this.selectedColor = color;
    document.documentElement.style.setProperty('--primary', this.selectedColor);
  }
  onDirectionChange(direction: string): void {
    const customizerBtn = document.querySelector('.customizerBtn') as HTMLElement;
    this.themeDirection = direction;

    // Apply the direction to the <html> element
    if (this.themeDirection === 'rtl') {
      this.renderer.setAttribute(document.documentElement, 'dir', 'rtl');
      document.documentElement.style.setProperty('--direction', this.themeDirection);

      // Apply styles with !important by adding a class or using inline styles
      this.applyImportantStyles(customizerBtn, 'auto', '30px');


    } else {
      this.renderer.setAttribute(document.documentElement, 'dir', 'ltr');
      document.documentElement.style.setProperty('--direction', this.themeDirection);

      // Apply styles with !important by adding a class or using inline styles
      this.applyImportantStyles(customizerBtn, '50px', '');

    }
  }

  private applyImportantStyles(element: HTMLElement, rightValue: string, left: string): void {
    if (element) {
      element.style.setProperty('right', rightValue, 'important');
      element.style.setProperty('left', left, 'important');

    }
  }

  changeContainerFullWidth() {

    document.documentElement.style.setProperty('--boxedWidth', '1200');
  }


  changeContainerBoxedWidth() {

    document.documentElement.style.setProperty('--boxedWidth', '');


  }
}
