import { Component, Input } from '@angular/core';

@Component({
  selector: 'custom-accordion',
  templateUrl: './accordion.component.html',
  styleUrl: './accordion.component.scss'
})
export class AccordionComponent {
  @Input() expanded: boolean = true;
  @Input() panelTitle: string = 'Accordion Panel';

}
