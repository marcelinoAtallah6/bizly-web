package com.broadcast.audit;

import java.time.LocalDateTime;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;

@Aspect
@Component
public class AuditAspect {

	@Autowired
	private UmAuditLogRepository auditLogRepository;

	@Autowired(required = false)
	private ObjectMapper objectMapper;

	@Around("@annotation(audited)")
	public Object audit(ProceedingJoinPoint pjp, Audited audited) throws Throwable {

		Object result = pjp.proceed();

		try {
			HttpServletRequest req = currentRequest();
			String username = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
					.map(Authentication::getName).orElse(null);

			UmAuditLog row = new UmAuditLog();
			row.setUsername(username);
			row.setActionCode(audited.action());
			row.setResourceType(emptyToNull(audited.resourceType()));
			row.setHttpMethod(req != null ? req.getMethod() : null);
			row.setRequestPath(req != null ? req.getRequestURI() : null);
			row.setIpAddress(req != null ? req.getRemoteAddr() : null);
			row.setSessionId(req != null ? req.getHeader("X-Session-Id") : null);
			row.setCreatedAt(LocalDateTime.now());

			MethodSignature sig = (MethodSignature) pjp.getSignature();
			Object[] args = pjp.getArgs();
			String[] names = sig.getParameterNames();
			if (args != null && names != null && args.length > 0) {
				row.setNewValues(stringify(args[0]));
				if ("id".equals(names[0]) && args[0] != null) {
					row.setResourceId(String.valueOf(args[0]));
				} else {
					tryResourceIdFromGetter(args[0], row);
				}
			}

			auditLogRepository.save(row);
		} catch (Exception ignored) {
		}

		return result;
	}

	private static HttpServletRequest currentRequest() {
		ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		return attrs != null ? attrs.getRequest() : null;
	}

	private String stringify(Object value) {
		if (value == null) {
			return null;
		}
		try {
			if (objectMapper != null) {
				return objectMapper.writeValueAsString(value);
			}
		} catch (Exception ignored) {
		}
		return String.valueOf(value);
	}

	private static String emptyToNull(String s) {
		return s == null || s.isBlank() ? null : s;
	}

	private static void tryResourceIdFromGetter(Object firstArg, UmAuditLog row) {
		if (firstArg == null) {
			return;
		}
		String[] getters = { "getId", "getDashboardId", "getRoleId", "getWidgetId" };
		for (String getter : getters) {
			try {
				Object id = firstArg.getClass().getMethod(getter).invoke(firstArg);
				if (id != null) {
					row.setResourceId(String.valueOf(id));
					return;
				}
			} catch (ReflectiveOperationException ignored) {
			}
		}
	}
}
