import { Component, EventEmitter, Input, Output } from '@angular/core';
import { ButtonConfig } from '../switch/switch.component';
import { ToolbarButton } from '../button/toolbar/toolbar.component';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'custom-card',
  templateUrl: './card.component.html',
  styleUrl: './card.component.scss'
})
export class CardComponent {
  @Input() cardTitle: string = 'Card Title'; // Card title input
  @Input() size: string = '12'; // Card title input
  @Input() showFooter: boolean = false;
  @Input() switchOption: boolean = false;
  // @Output() variableChange: EventEmitter<string> = new EventEmitter<string>();

  @Input() toolbar: ToolbarButton[] = [];
  constructor(
  ) { }



}

