import { Injectable, TemplateRef, Type } from '@angular/core';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { EnhancedDialogConfig, DialogComponent } from '../dialog.component';
@Injectable({ providedIn: 'root' })
export class DialogService {
  constructor(private dialog: MatDialog) {}

  open(config: Partial<EnhancedDialogConfig>) {
    const dialogConfig: MatDialogConfig<EnhancedDialogConfig> = {
      width: config.width || '1200px',
      height: config.height || 'auto',
      disableClose: config.disableClose || false,
      hasBackdrop: config.hasBackdrop || true,
      panelClass: config.panelClass || '',
      data: {
        header: config.header,
        body: config.body as TemplateRef<any> | Type<any>,
        data:config.data,
         // Ensure valid body
        footer: config.footer as TemplateRef<any> | Type<any>, // Ensure valid body,
        type: config.type ||'dialog',
      },
    };

    return this.dialog.open(DialogComponent, dialogConfig);
  }
}
