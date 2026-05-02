import { Component } from '@angular/core';


@Component({
  selector: 'custom-image-upload',
  templateUrl: './image-upload.component.html',
  styleUrl: './image-upload.component.scss'
})
export class ImageUploadComponent {
  previewUrl: string | ArrayBuffer | null = null;

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      this.loadImage(input.files[0]);
    }
  }

  removeImage(): void {
    this.previewUrl = null; // Clear the preview URL
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault(); // Prevent default to allow drop
    const container = event.currentTarget as HTMLElement;
    container.classList.add('drag-over'); // Add class on drag over
  }

  onDragLeave(event: DragEvent): void {
    const container = event.currentTarget as HTMLElement;
    container.classList.remove('drag-over'); // Remove class on drag leave
  }

  onDrop(event: DragEvent): void {
    event.preventDefault(); // Prevent default behavior
    const container = event.currentTarget as HTMLElement;
    container.classList.remove('drag-over'); // Remove class on drop

    if (event.dataTransfer && event.dataTransfer.files.length) {
      const file = event.dataTransfer.files[0];
      this.loadImage(file);
    }
  }

  private loadImage(file: File): void {
    const reader = new FileReader();
    reader.onload = (e) => {
      this.previewUrl = e.target?.result ?? null;
    };
    reader.readAsDataURL(file);
  }


  // public previewUrl: string | ArrayBuffer | null = null;

  // onFileSelected(event: Event): void {
  //   const input = event.target as HTMLInputElement;
  //   if (input.files && input.files.length) {
  //     const file = input.files[0];
  //     const reader = new FileReader();

  //     reader.onload = (e) => {
  //       this.previewUrl = e.target?.result ?? null;
  //     };

  //     reader.readAsDataURL(file);
  //   }
  // }

  // removeImage(): void {
  //   this.previewUrl = null; // Clears the preview when "X" is clicked
  // }

}
