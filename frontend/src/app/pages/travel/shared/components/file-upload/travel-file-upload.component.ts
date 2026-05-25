import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-travel-file-upload',
  templateUrl: './travel-file-upload.component.html',
  styleUrl: './travel-file-upload.component.scss',
})
export class TravelFileUploadComponent {
  @Input() label = 'Browse';
  @Input() accept = '*/*';
  @Input() fileName = '';
  @Input() hint = '';
  /** Inline thumbnail for images; PDFs show icon chip. */
  @Input() showPreview = true;
  @Output() fileSelected = new EventEmitter<File | null>();

  selectedFile: File | null = null;
  previewUrl: string | null = null;
  isImage = false;

  get displayName(): string {
    return this.selectedFile?.name || this.fileName || '';
  }

  onFile(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) {
      return;
    }
    this.setFile(file);
  }

  setFile(file: File): void {
    this.revokePreview();
    this.selectedFile = file;
    this.fileName = file.name;
    this.isImage = file.type.startsWith('image/');
    if (this.showPreview && this.isImage) {
      this.previewUrl = URL.createObjectURL(file);
    } else {
      this.previewUrl = null;
    }
    this.fileSelected.emit(file);
  }

  clear(): void {
    this.revokePreview();
    this.selectedFile = null;
    this.fileName = '';
    this.previewUrl = null;
    this.isImage = false;
    this.fileSelected.emit(null);
  }

  private revokePreview(): void {
    if (this.previewUrl?.startsWith('blob:')) {
      URL.revokeObjectURL(this.previewUrl);
    }
  }
}
