import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { ExtraRoutes } from './extra.routing';
import { AppIconsComponent } from './icons/icons.component';
import { AppSamplePageComponent } from './sample-page/sample-page.component';
import { CommonMaterialModule } from 'src/app/common/MaterialModule';

@NgModule({
  schemas: [CUSTOM_ELEMENTS_SCHEMA],

  imports: [
    RouterModule.forChild(ExtraRoutes),
    CommonMaterialModule,

  ],
  exports:[],
  declarations: [
    AppIconsComponent,
    AppSamplePageComponent // Remove MatTab, MatTabHeader, MatTabBody, MatTabGroup
  ],
  bootstrap: [],
})
export class ExtraModule { }
