import { Component, Input } from '@angular/core';

@Component({
  selector: 'custom-fieldset',
  templateUrl: './fieldset.component.html',
  styleUrl: './fieldset.component.scss'
})
export class FieldsetComponent {

  @Input() name : String = '';

}
