package com.um.api.service.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enforces the role×menu permission matrix. When {@link #menuId()} is 0, resolves the menu via
 * {@link #menuRoute()} against {@code UM_MENUS.route}. Valid route strings are those returned by
 * {@code POST /um/menu/permission-metadata} — keep controllers aligned with DB menu rows only.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireMenuPermission {

	/** PK of {@code UM_MENUS}; takes precedence over {@link #menuRoute()}. */
	long menuId() default 0L;

	/**
	 * Angular route for this screen (e.g. {@code /um/user}); normalized with/without leading slash.
	 */
	String menuRoute() default "";

	MenuPermissionAction action();
}
