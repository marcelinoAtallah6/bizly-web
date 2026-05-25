package com.travel.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireMenuPermission {

	long menuId() default 0L;

	String menuRoute() default "";

	MenuPermissionAction action();
}
