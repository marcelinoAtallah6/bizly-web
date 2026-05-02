// import { Component } from '@angular/core';
// import { CdkDragDrop, moveItemInArray, transferArrayItem } from '@angular/cdk/drag-drop';

// interface ComponentItem {
//   id: string;
//   name: string;
//   icon: string;
//   type: string;
// }

// type CategoryType = 'inputs' | 'selection' | 'other';

// type Categories = {
//   [key in CategoryType]: boolean;
// };
// @Component({
//   selector: 'custom-component-builder',
//   templateUrl: './componentbuilder.component.html',
//   styleUrls: ['./componentbuilder.component.scss']
// })

// export class ComponentBuilderComponent {
//   categories = {
//     inputs: true,
//     selection: true,
//     other: true
//   };

//   components: { [key in CategoryType]: ComponentItem[] } = {
//     inputs: [
//       { id: 'text-input', name: 'Text Input', icon: 'text_fields', type: 'inputs' },
//       { id: 'textarea', name: 'Text Area', icon: 'notes', type: 'inputs' },
//       { id: 'number', name: 'Number Input', icon: 'numbers', type: 'inputs' },
//       { id: 'password', name: 'Password Input', icon: 'password', type: 'inputs' }
//     ],
//     selection: [
//       { id: 'dropdown', name: 'Dropdown', icon: 'arrow_drop_down_circle', type: 'selection' },
//       { id: 'checkbox', name: 'Checkbox', icon: 'check_box', type: 'selection' },
//       { id: 'radio', name: 'Radio Button', icon: 'radio_button_checked', type: 'selection' },
//       { id: 'toggle', name: 'Toggle Switch', icon: 'toggle_on', type: 'selection' }
//     ],
//     other: [
//       { id: 'datepicker', name: 'Date Picker', icon: 'calendar_today', type: 'other' },
//       { id: 'file', name: 'File Upload', icon: 'upload_file', type: 'other' },
//       { id: 'button', name: 'Button', icon: 'smart_button', type: 'other' }
//     ]
//   };

//   droppedComponents: ComponentItem[] = [];

//   toggleCategory(category: CategoryType): void {
//     this.categories[category] = !this.categories[category];
//   }

//   onDrop(event: CdkDragDrop<ComponentItem[]>) {
//     if (event.previousContainer === event.container) {
//       moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
//     } else {
//       const itemToCopy = event.previousContainer.data[event.previousIndex];
//       const newItem = { ...itemToCopy, id: `${itemToCopy.id}-${Date.now()}` };
//       this.droppedComponents.splice(event.currentIndex, 0, newItem);
//     }
//   }

//   removeComponent(index: number): void {
//     this.droppedComponents.splice(index, 1);
//   }
// }


  // builder.component.ts
  import { Component, OnInit } from '@angular/core';
  import { CdkDragDrop, moveItemInArray, transferArrayItem } from '@angular/cdk/drag-drop';
  
  interface ComponentDefinition {
    id: string;
    type: string;
    icon: string;
    properties: Record<string, any>;
  }
  
  @Component({
    selector: 'custom-component-builder>',
    template: `
      <div class="builder-container">
        <!-- Components Panel -->
        <div class="components-panel">
          <h2>Components</h2>
          <div cdkDropList
               [cdkDropListData]="availableComponents"
               (cdkDropListDropped)="drop($event)"
               class="components-list">
            <div *ngFor="let component of availableComponents"
                 cdkDrag
                 class="component-item">
              <i [class]="component.icon"></i>
              {{ component.type }}
            </div>
          </div>
        </div>
  
        <!-- Canvas -->
        <div class="canvas"
             cdkDropList
             [cdkDropListData]="canvasComponents"
             (cdkDropListDropped)="drop($event)">
          <div *ngFor="let component of canvasComponents"
               cdkDrag
               class="canvas-item"
               (click)="selectComponent(component)">
            <ng-container [ngComponentOutlet]="getComponentType(component)">
            </ng-container>
          </div>
          <div *ngIf="canvasComponents.length === 0" class="empty-canvas">
            Drag components here
          </div>
        </div>
  
        <!-- Properties Panel -->
        <div class="properties-panel">
          <h2>Properties</h2>
          <div *ngIf="selectedComponent" class="properties-form">
            <div *ngFor="let prop of getComponentProperties()">
              <label>{{ prop.label }}</label>
              <ng-container [ngSwitch]="prop.type">
                <input *ngSwitchCase="'text'"
                       type="text"
                       [(ngModel)]="selectedComponent.properties[prop.key]">
                <select *ngSwitchCase="'select'"
                        [(ngModel)]="selectedComponent.properties[prop.key]">
                  <option *ngFor="let opt of prop.options" [value]="opt.value">
                    {{ opt.label }}
                  </option>
                </select>
              </ng-container>
            </div>
          </div>
        </div>
      </div>
    `,
    styles: [`
      .builder-container {
        display: flex;
        height: 100vh;
      }
  
      .components-panel, .properties-panel {
        width: 250px;
        background: #f5f5f5;
        padding: 1rem;
        border: 1px solid #ddd;
      }
  
      .canvas {
        flex: 1;
        padding: 2rem;
        background: white;
        border: 2px dashed #ccc;
        min-height: 100%;
      }
  
      .component-item {
        padding: 0.5rem;
        margin: 0.5rem 0;
        background: white;
        border: 1px solid #ddd;
        cursor: move;
      }
  
      .empty-canvas {
        display: flex;
        justify-content: center;
        align-items: center;
        height: 100%;
        color: #666;
      }
  
      .properties-form {
        display: flex;
        flex-direction: column;
        gap: 1rem;
      }
    `]
  })
  export class ComponentBuilderComponent {
    availableComponents: ComponentDefinition[] = [
      {
        id: 'input',
        type: 'Input Field',
        icon: 'fas fa-input',
        properties: { label: '', placeholder: '', required: false }
      },
      {
        id: 'button',
        type: 'Button',
        icon: 'fas fa-square',
        properties: { text: '', style: 'primary' }
      },
      {
        id: 'table',
        type: 'Table',
        icon: 'fas fa-table',
        properties: { columns: [], dataSource: '' }
      }
    ];
  
    canvasComponents: ComponentDefinition[] = [];
    selectedComponent: ComponentDefinition | null = null;
  
    drop(event: CdkDragDrop<ComponentDefinition[]>) {
      if (event.previousContainer === event.container) {
        moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
      } else {
        transferArrayItem(
          event.previousContainer.data,
          event.container.data,
          event.previousIndex,
          event.currentIndex
        );
      }
    }
  
    selectComponent(component: ComponentDefinition) {
      this.selectedComponent = component;
    }
  
    getComponentProperties() {
      if (!this.selectedComponent) return [];
      
      // Return properties based on component type
      switch (this.selectedComponent.type) {
        case 'Input Field':
          return [
            { key: 'label', label: 'Label', type: 'text' },
            { key: 'placeholder', label: 'Placeholder', type: 'text' },
            { key: 'required', label: 'Required', type: 'checkbox' }
          ];
        case 'Button':
          return [
            { key: 'text', label: 'Button Text', type: 'text' },
            { key: 'style', label: 'Style', type: 'select', options: [
              { label: 'Primary', value: 'primary' },
              { label: 'Secondary', value: 'secondary' }
            ]}
          ];
        default:
          return [];
      }
    }
  
    getComponentType(component: ComponentDefinition) {
      // Return the actual component type based on the definition
      // This would be implemented based on your component registry
      return null;
    }
  }