import { Component, Input } from '@angular/core';
import { ToolbarButton } from '../button/toolbar/toolbar.component';

/** Standard page header: title left, icon actions via custom-toolbar right. Parent supplies `p-24`. */
@Component({
  selector: 'app-page-action-bar',
  templateUrl: './page-action-bar.component.html',
  styleUrl: './page-action-bar.component.scss',
})
export class PageActionBarComponent {
  @Input() title = '';
  /** Passed through to `custom-toolbar` (add, refresh, edit, …). */
  @Input() toolbar: ToolbarButton[] = [];
  /** When true, adds flex-wrap and gap for detail screens with many actions. */
  @Input() wrap = false;
}
