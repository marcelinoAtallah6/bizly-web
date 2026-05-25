import { Component, EventEmitter, Input, Output } from '@angular/core';
import { TravelDocumentRow } from 'src/app/core/models/travel.models';
import { travelDocumentTypeTheme, TravelDocumentTypeTheme } from '../../travel-document-type-theme';

@Component({
  selector: 'app-travel-document-grid-card',
  templateUrl: './travel-document-grid-card.component.html',
  styleUrl: './travel-document-grid-card.component.scss',
})
export class TravelDocumentGridCardComponent {
  @Input() doc!: TravelDocumentRow;
  @Input() clientLabel = '—';
  @Input() canEdit = false;
  @Input() canDelete = false;
  /** `saas` = full Documents screen mockup; `compact` = client profile tab */
  @Input() displayMode: 'saas' | 'compact' = 'compact';
  @Output() view = new EventEmitter<TravelDocumentRow>();
  @Output() download = new EventEmitter<TravelDocumentRow>();
  @Output() edit = new EventEmitter<TravelDocumentRow>();
  @Output() remove = new EventEmitter<TravelDocumentRow>();

  get theme(): TravelDocumentTypeTheme {
    return travelDocumentTypeTheme(this.doc.docType);
  }

  get headerStyle(): Record<string, string> {
    if (this.displayMode !== 'saas') {
      return {};
    }
    return {
      '--doc-header-from': this.theme.headerFrom,
      '--doc-header-to': this.theme.headerTo,
    };
  }

  get fileExt(): string {
    const name = this.doc.fileName ?? '';
    const i = name.lastIndexOf('.');
    return i >= 0 ? name.substring(i + 1).toUpperCase() : 'DOC';
  }

  get iconForType(): string {
    return this.theme.icon;
  }

  get typeLabel(): string {
    return (this.doc.docType ?? this.theme.label).toUpperCase();
  }
}
