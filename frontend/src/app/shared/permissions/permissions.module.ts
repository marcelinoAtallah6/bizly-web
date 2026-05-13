import { NgModule } from '@angular/core';
import { HasPermissionDirective } from './has-permission.directive';

/**
 * Shared module that exposes the JWT-driven permission directives. Import it from any feature module
 * that needs {@code *hasPermission} in its templates — keeps the directive declared in exactly one
 * place while remaining usable across lazy-loaded routes.
 */
@NgModule({
  declarations: [HasPermissionDirective],
  exports: [HasPermissionDirective],
})
export class PermissionsModule {}
