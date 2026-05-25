import { Component, EventEmitter, Input, Output } from '@angular/core';
import { TravelDocumentRow } from 'src/app/core/models/travel.models';

@Component({
  selector: 'app-travel-document-row-card',
  templateUrl: './travel-document-row-card.component.html',
  styleUrl: './travel-document-row-card.component.scss',
})
export class TravelDocumentRowCardComponent {
  @Input() doc!: TravelDocumentRow;
  @Input() clientLabel = '—';
  @Input() bookingLabel = '—';
  @Input() canEdit = false;
  @Input() canDelete = false;

  @Output() view = new EventEmitter<TravelDocumentRow>();
  @Output() download = new EventEmitter<TravelDocumentRow>();
  @Output() edit = new EventEmitter<TravelDocumentRow>();
  @Output() remove = new EventEmitter<TravelDocumentRow>();

  get fileExt(): string {
    const name = this.doc?.fileName ?? '';
    const i = name.lastIndexOf('.');
    return i > 0 ? name.substring(i + 1).toUpperCase() : this.doc?.docType ?? 'DOC';
  }

  get iconForType(): string {
    const t = (this.doc?.docType ?? '').toUpperCase();
    if (t.includes('PASSPORT') || t.includes('ID')) {
      return 'badge';
    }
    if (t.includes('VISA')) {
      return 'card_travel';
    }
    if (t.includes('INVOICE') || t.includes('TICKET')) {
      return 'receipt_long';
    }
    return 'description';
  }
}
