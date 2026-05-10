import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { QuerybuilderRoutingModule } from './querybuilder-routing.module';
import { QuerybuilderComponent } from './querybuilder.component';
import { CommonMaterialModule } from 'src/app/common/MaterialModule';

@NgModule({
  declarations: [QuerybuilderComponent],
  imports: [CommonModule, FormsModule, CommonMaterialModule, QuerybuilderRoutingModule],
})
export class QuerybuilderModule {}
