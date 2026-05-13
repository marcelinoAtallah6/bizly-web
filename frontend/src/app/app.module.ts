import { APP_INITIALIZER, NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';

// icons
import { TablerIconsModule } from 'angular-tabler-icons';
import * as TablerIcons from 'angular-tabler-icons/icons';

//Import all material modules
import { MaterialModule } from './material.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { DragDropModule } from '@angular/cdk/drag-drop';

//Import Layouts
import { FullComponent } from './layouts/full/full.component';
import { BlankComponent } from './layouts/blank/blank.component';

// Vertical Layout
import { SidebarComponent } from './layouts/full/sidebar/sidebar.component';
import { HeaderComponent } from './layouts/full/header/header.component';
import { GlobalContextSwitcherComponent } from './layouts/full/header/global-context-switcher/global-context-switcher.component';
import { CustomizerComponent } from './layouts/full/customizer/customizer.component';
import { TopSideBarComponent } from './layouts/full/topSideBar/top-side-bar/top-side-bar.component';
import { BrandingComponent } from './layouts/full/sidebar/branding.component';
import { AppNavItemComponent } from './layouts/full/sidebar/nav-item/nav-item.component';
import { BreadcrumbComponent } from './pages/ui-components/breadcrumb/breadcrumb.component';
import { CustomHTTPInterceptor } from './common/CustomHTTPInterceptor';
import { DeviceIdService } from './services/device-id.service';
import { PermissionsModule } from './shared/permissions/permissions.module';

@NgModule({
  declarations: [
    AppComponent,
    FullComponent,
    BlankComponent,
    SidebarComponent,
    HeaderComponent,
    GlobalContextSwitcherComponent,
    CustomizerComponent,
    BrandingComponent,
    AppNavItemComponent,
    TopSideBarComponent,
    BreadcrumbComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    BrowserAnimationsModule,
    FormsModule,
    ReactiveFormsModule,
    MaterialModule,
    TablerIconsModule.pick(TablerIcons),
    DragDropModule,
    PermissionsModule,
  ],
  exports: [TablerIconsModule],
  providers: [
    {
      provide: APP_INITIALIZER,
      multi: true,
      useFactory: (deviceId: DeviceIdService) => () => deviceId.init(),
      deps: [DeviceIdService],
    },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: CustomHTTPInterceptor,
      multi: true,
    },
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}
