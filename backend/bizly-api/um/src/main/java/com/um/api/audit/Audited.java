package com.um.api.audit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a controller handler for automatic persistence to {@code UM_AUDIT_LOG}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Audited {

	/** Stable action identifier, e.g. {@code UM_USER_ADD}. */
	String action();

	/** Optional domain resource label for filtering in the UI. */
	String resourceType() default "";

}
