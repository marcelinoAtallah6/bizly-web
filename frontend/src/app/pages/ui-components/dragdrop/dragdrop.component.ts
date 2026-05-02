// dragdrop.component.ts
import { Component, Input } from '@angular/core';
import { 
  CdkDragDrop, 
  moveItemInArray, 
  transferArrayItem,
  CdkDragEnter,
  CdkDragExit 
} from '@angular/cdk/drag-drop';

interface DragItem {
  id: number;
  title: string;
  description?: string;
  priority?: 'low' | 'medium' | 'high';
  type?: string;
}

interface DragList {
  id: string;
  title: string;
  items: DragItem[];
}

@Component({
  selector: 'custom-drag-drop',
  template: `
    <div class="drag-drop-container">
      <div class="board-container">
        <div *ngFor="let list of lists" 
             class="list-container"
             [class.list-highlight]="dragListActive">
          
          <div class="list-header">
            <h3>{{ list.title }}</h3>
            <span class="item-count">{{ list.items.length }} items</span>
          </div>

          <div cdkDropList
               #dropList="cdkDropList"
               [id]="list.id"
               [cdkDropListData]="list.items"
               [cdkDropListConnectedTo]="getConnectedLists(list.id)"
               (cdkDropListDropped)="drop($event)"
               (cdkDropListEntered)="onDragEnter($event)"
               (cdkDropListExited)="onDragExit($event)"
               class="item-list">

            <div *ngFor="let item of list.items"
                 cdkDrag
                 [cdkDragData]="item"
                 class="drag-item"
                 [class.drag-item-priority-high]="item.priority === 'high'"
                 [class.drag-item-priority-medium]="item.priority === 'medium'"
                 [class.drag-item-priority-low]="item.priority === 'low'">
              
              <!-- Preview when dragging -->
              <div *cdkDragPreview class="drag-preview">
                {{ item.title }}
              </div>

              <!-- Placeholder -->
              <div *cdkDragPlaceholder class="drag-placeholder">
                <div class="placeholder-inner"></div>
              </div>

              <!-- Actual item content -->
              <div class="item-content">
                <div class="item-header">
                  <span class="item-title">{{ item.title }}</span>
                  <span class="item-type" *ngIf="item.type">{{ item.type }}</span>
                </div>
                <p class="item-description" *ngIf="item.description">
                  {{ item.description }}
                </p>
                <div class="item-footer" *ngIf="item.priority">
                  <span class="priority-badge" [attr.data-priority]="item.priority">
                    {{ item.priority }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .drag-drop-container {
      padding: 20px;
      background-color: #f5f6f8;
      min-height: 100vh;
    }

    .board-container {
      display: flex;
      gap: 20px;
      overflow-x: auto;
      padding-bottom: 20px;
      flex-wrap: wrap;//added
    }

    .list-container {
      flex: 0 0 266px;//300px
      background: white;
      border-radius: 8px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.05);
      padding: 16px;
      max-height: calc(100vh - 238px);
      display: flex;
      flex-direction: column;
    }

    .list-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      padding-bottom: 8px;
      border-bottom: 1px solid #eee;
    }

    .list-header h3 {
      margin: 0;
      font-size: 16px;
      font-weight: 600;
      color: #333;
    }

    .item-count {
      font-size: 12px;
      color: #666;
      background: #f0f0f0;
      padding: 2px 8px;
      border-radius: 12px;
    }

    .item-list {
      flex-grow: 1;
      overflow-y: auto;
      min-height: 100px;
    }

    .drag-item {
      background: white;
      border: 1px solid #eee;
      border-radius: 6px;
      margin-bottom: 8px;
      cursor: move;
      transition: all 0.2s ease;
    }

    .drag-item:hover {
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    }

    .item-content {
      padding: 12px;
    }

    .item-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 8px;
    }

    .item-title {
      font-weight: 500;
      color: #333;
    }

    .item-type {
      font-size: 12px;
      color: #666;
      background: #f5f5f5;
      padding: 2px 6px;
      border-radius: 4px;
    }

    .item-description {
      font-size: 13px;
      color: #666;
      margin: 8px 0;
    }

    .priority-badge {
      font-size: 11px;
      padding: 2px 8px;
      border-radius: 12px;
      text-transform: uppercase;
    }

    .priority-badge[data-priority="high"] {
      background: #ffe4e4;
      color: #d63031;
    }

    .priority-badge[data-priority="medium"] {
      background: #fff3e4;
      color: #fd9644;
    }

    .priority-badge[data-priority="low"] {
      background: #e4ffe4;
      color: #27ae60;
    }

    .drag-preview {
      padding: 10px;
      background: white;
      border-radius: 6px;
      box-shadow: 0 5px 15px rgba(0,0,0,0.15);
    }

    .drag-placeholder {
      min-height: 60px;
      background: #f8f9fa;
      border: 2px dashed #dee2e6;
      border-radius: 6px;
      margin: 8px 0;
    }

    .list-highlight {
      background: #f8f9fa;
      transition: background-color 0.2s ease;
    }

    .cdk-drag-animating {
      transition: transform 250ms cubic-bezier(0, 0, 0.2, 1);
    }

    .item-list.cdk-drop-list-dragging .drag-item:not(.cdk-drag-placeholder) {
      transition: transform 250ms cubic-bezier(0, 0, 0.2, 1);
    }
  `]
})
export class DragDropComponent {
  dragListActive = false;

  @Input() lists: any[] = [];

  getConnectedLists(currentListId: string): string[] {
    return this.lists
      .map(list => list.id)
      .filter(id => id !== currentListId);
  }

  drop(event: CdkDragDrop<DragItem[]>) {
    try {
      if (event.previousContainer === event.container) {
        moveItemInArray(
          event.container.data,
          event.previousIndex,
          event.currentIndex
        );
      } else {
        transferArrayItem(
          event.previousContainer.data,
          event.container.data,
          event.previousIndex,
          event.currentIndex
        );
      }
      
      this.dragListActive = false;
      this.onListsUpdated();
    } catch (error) {
      console.error('Error during drag and drop:', error);
    }
  }

  onDragEnter(event: CdkDragEnter) {
    this.dragListActive = true;
  }

  onDragExit(event: CdkDragExit) {
    this.dragListActive = false;
  }

  onListsUpdated() {
    // Implement your save logic here
    console.log('Lists updated:', this.lists);
  }
}