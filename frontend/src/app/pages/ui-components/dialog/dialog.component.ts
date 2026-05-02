import { Component, Inject, TemplateRef, Type, ViewChild } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';

export interface EnhancedDialogConfig {
  header?: string; // Optional header
  body?: Type<any> | TemplateRef<any>; // Allow Type<any> or TemplateRef<any>
  footer?: Type<any> | TemplateRef<any>; // Optional footer
  width?: string;
  height?: string;
  data?:any | "";
  type?:String | "dialog",
  disableClose?: boolean;
  hasBackdrop?: boolean;
  panelClass?: string;
}

@Component({
  selector: 'custom-dialog',
  templateUrl: './dialog.component.html',
  styleUrl: './dialog.component.scss'
})
export class DialogComponent {
  @ViewChild(TemplateRef) templateRef!: TemplateRef<any>;

  constructor(
    public dialogRef: MatDialogRef<DialogComponent>,
    @Inject(MAT_DIALOG_DATA) public config: EnhancedDialogConfig
  ) {
    console.log("dialog config = ",config)
  }

  close(): void {
    this.dialogRef.close();
  }

  // Check if the content is a TemplateRef
  isTemplate(content: any): content is TemplateRef<any> {
    return content instanceof TemplateRef;
  }

  isComponent(content: any):  content is Type<any> {
    return typeof content === 'function' && content.prototype !== undefined;
  }
  
}
