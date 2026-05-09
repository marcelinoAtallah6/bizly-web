import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CommonMaterialModule } from 'src/app/common/MaterialModule';
import { SaleDetailDialogComponent } from './sale-detail-dialog.component';

@NgModule({
  declarations: [SaleDetailDialogComponent],
  imports: [CommonModule, CommonMaterialModule],
  exports: [SaleDetailDialogComponent],
})
export class SaleDetailDialogModule {}
