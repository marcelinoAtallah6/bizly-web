export interface ToolbarButton {
  id: string;
  icon: string;
  tooltip: string;
  hasSubmenu?: boolean;
  submenuItems?: SubmenuItem[];
  action?: () => void;
  disabled?: boolean;
  /** Material palette for icon buttons (e.g. primary add, warn delete). */
  color?: 'primary' | 'accent' | 'warn';
}

export interface SubmenuItem {
  id: string;
  label: string;
  icon: string;
  action: () => void;
}

export interface SortOption {
  id: string;
  label: string;
  icon: string;
  value: string;
}
// toolbar.component.ts
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'custom-toolbar',
  templateUrl: './toolbar.component.html',
  styleUrls: ['./toolbar.component.scss']
})
export class ToolbarComponent {
  // @Input() buttons: ToolbarButton[] = [];
  @Output() selectionChange = new EventEmitter<string>(); // Emits the selected value
  @Input() toolbar: ToolbarButton[] = []

  //filter start///
  selectedSort = 'date';
  @Output() sortChange = new EventEmitter<string>();

  sortOptions: SortOption[] = [
    { id: 'date', label: 'Sort by Date', icon: 'calendar_today', value: 'date' },
    { id: 'rating', label: 'Sort by Rating', icon: 'star', value: 'rating' },
    { id: 'price', label: 'Sort by Price', icon: 'attach_money', value: 'price' }
  ];

  onSortChange(value: string): void {
    this.selectedSort = value;
    this.sortChange.emit(value);
  }

  ///filter end ///
  constructor() {}

  /** Stable identity so *ngFor does not recreate buttons every CD when parent uses a getter for `toolbar`. */
  trackToolbarBtn(_index: number, button: ToolbarButton): string {
    return button.id;
  }

  invoke(button: ToolbarButton, event: Event): void {
    if (button.disabled) {
      return;
    }
    button.action?.();
    event.stopPropagation();
  }

  @Output() menuItemClicked = new EventEmitter<string>();
  @Output() submenuItemClicked = new EventEmitter<() => void>();

  onMenuItemClick(buttons: ToolbarButton) {
    if (buttons.hasSubmenu) {
      return; // Let the mat-menu handle the click
    }
    this.menuItemClicked.emit(buttons.id);
  }

  onSubmenuItemClick(action: () => void) {
    this.submenuItemClicked.emit(action);
  }

}

