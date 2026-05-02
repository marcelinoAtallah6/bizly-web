import { Component, Input } from '@angular/core';

@Component({
  selector: 'custom-list',
  templateUrl: './list.component.html',
  styleUrl: './list.component.scss'
})
export class ListComponent {
  @Input() items: string[] = []; // List of items to display
}
