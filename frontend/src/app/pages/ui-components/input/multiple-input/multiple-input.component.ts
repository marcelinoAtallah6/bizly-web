import { ENTER, COMMA } from '@angular/cdk/keycodes';
import { Component, Input } from '@angular/core';
import { FormControl } from '@angular/forms';
import { MatChipEditedEvent, MatChipInputEvent } from '@angular/material/chips';

interface Fruit {
  name: string;
}

@Component({
  selector: 'custom-multiple-input',
  templateUrl: './multiple-input.component.html',
  styleUrl: './multiple-input.component.scss'
})
export class MultipleInputComponent {
  @Input() placeholder: string = 'New fruit...';
  @Input() label: string = 'Favorite Fruits';
  @Input() addOnBlur: boolean = true;
  @Input() separatorKeysCodes: number[] = [ENTER, COMMA];

  fruits: Fruit[] = [{ name: 'Apple' }, { name: 'Banana' }, { name: 'Mango' }];
  fruitControl = new FormControl('');

  /** Adds a new fruit */
  add(event: MatChipInputEvent): void {
    const value = (event.value || '').trim();
    if (value) {
      this.fruits.push({ name: value });
    }
    event.chipInput!.clear();
    this.fruitControl.setValue(null);
  }

   /** Removes an existing fruit */
   remove(fruit: Fruit): void {
    const index = this.fruits.indexOf(fruit);
    if (index >= 0) {
      this.fruits.splice(index, 1);
    }
  }

  /** Edits an existing fruit */
  edit(fruit: Fruit, event: MatChipEditedEvent): void {
    const value = event.value.trim();
    if (value) {
      fruit.name = value;
    }
  }
}
