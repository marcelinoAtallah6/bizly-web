import { Component, Inject, OnInit } from '@angular/core';
import { FormControl } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import {
  WorkflowTaskCatalogCategory,
  WorkflowTaskCatalogItem,
} from '../../services/um-workflow-engine.service';

export interface WorkflowTaskPickerDialogData {
  categories: WorkflowTaskCatalogCategory[];
}

export interface WorkflowTaskPickerDialogResult {
  item: WorkflowTaskCatalogItem;
}

@Component({
  selector: 'app-workflow-task-picker-dialog',
  templateUrl: './workflow-task-picker-dialog.component.html',
  styleUrls: ['./workflow-task-picker-dialog.component.scss'],
})
export class WorkflowTaskPickerDialogComponent implements OnInit {
  searchControl = new FormControl('');
  activeCategory = 'QUICK_ADD';
  selected: WorkflowTaskCatalogItem | null = null;

  readonly tabOrder = [
    'QUICK_ADD',
    'APPROVAL',
    'NOTIFICATION',
    'HTTP',
    'SYSTEM',
    'VARIABLE',
    'AGENTIC',
  ];

  constructor(
    private readonly ref: MatDialogRef<WorkflowTaskPickerDialogComponent, WorkflowTaskPickerDialogResult>,
    @Inject(MAT_DIALOG_DATA) public readonly data: WorkflowTaskPickerDialogData
  ) {}

  ngOnInit(): void {
    const first = this.data.categories.find((c) => c.categoryKey === 'QUICK_ADD');
    if (!first && this.data.categories.length) {
      this.activeCategory = this.data.categories[0].categoryKey;
    }
  }

  get filteredItems(): WorkflowTaskCatalogItem[] {
    const q = (this.searchControl.value ?? '').trim().toLowerCase();
    const cat = this.data.categories.find((c) => c.categoryKey === this.activeCategory);
    const items = cat?.items ?? [];
    if (!q) {
      return items;
    }
    return items.filter(
      (i) =>
        i.title.toLowerCase().includes(q) ||
        (i.description ?? '').toLowerCase().includes(q) ||
        i.taskKey.toLowerCase().includes(q)
    );
  }

  categoryLabel(key: string): string {
    return this.data.categories.find((c) => c.categoryKey === key)?.categoryLabel ?? key;
  }

  select(item: WorkflowTaskCatalogItem): void {
    this.selected = item;
  }

  confirm(): void {
    if (this.selected) {
      this.ref.close({ item: this.selected });
    }
  }

  cancel(): void {
    this.ref.close();
  }

  iconFor(item: WorkflowTaskCatalogItem): string {
    const map: Record<string, string> = {
      'shield-check': 'shield-check',
      users: 'users',
      search: 'search',
      mail: 'mail',
      inbox: 'inbox',
      bell: 'bell',
      world: 'world',
      refresh: 'refresh',
      api: 'api',
      'git-branch': 'git-branch',
      'git-merge': 'git-merge',
      repeat: 'repeat',
      variable: 'variable',
      clock: 'clock',
      message: 'message',
      vector: 'vector',
    };
    return map[item.iconKey ?? ''] ?? 'circle-plus';
  }
}
