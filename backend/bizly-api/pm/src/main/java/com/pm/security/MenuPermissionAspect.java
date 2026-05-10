package com.pm.security;

import java.util.Optional;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class MenuPermissionAspect {

	@Autowired
	private MenuPermissionService menuPermissionService;

	@Around("@annotation(perm)")
	public Object enforce(ProceedingJoinPoint pjp, RequireMenuPermission perm) throws Throwable {
		long mid = perm.menuId();
		String route = perm.menuRoute();
		if (mid <= 0 && (route == null || route.isBlank())) {
			throw new IllegalStateException("RequireMenuPermission requires menuId > 0 or a non-blank menuRoute");
		}
		menuPermissionService.assertAllowed(mid > 0 ? Optional.of(mid) : Optional.empty(),
				Optional.ofNullable(route).filter(s -> !s.isBlank()),
				perm.action());
		return pjp.proceed();
	}
}
