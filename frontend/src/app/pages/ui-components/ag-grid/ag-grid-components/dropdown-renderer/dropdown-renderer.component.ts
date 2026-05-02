import { Component } from '@angular/core';
import { ICellRendererAngularComp } from 'ag-grid-angular';

@Component({
  selector: 'dropdown-renderer',
  template: `
    <select (change)="onChange($event)">
      <option *ngFor="let option of options" [value]="option">{{ option }}</option>
    </select>
  `,
})
export class DropdownRendererComponent implements ICellRendererAngularComp {
  public options: string[] = [];
  private params: any;

  agInit(params: any): void {
    this.params = params;
    this.options = this.params.options || [];
  }

  onChange(event: Event): void {
    const input = event.target as HTMLInputElement;
      this.params.setValue(input.value);
  }

  refresh(params: any): boolean {
    this.params = params;
    this.options = this.params.options || [];
    return true;
  }
}
