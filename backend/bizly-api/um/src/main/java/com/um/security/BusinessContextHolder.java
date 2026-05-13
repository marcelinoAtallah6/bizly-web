package com.um.security;

/**
 * See {@code com.bm.security.BusinessContextHolder} for full semantics.
 * Duplicated per module to avoid a shared bizly-common dependency for now.
 */
public final class BusinessContextHolder {
	private static final ThreadLocal<Context> TL = new ThreadLocal<>();
	private BusinessContextHolder() {}

	public static void set(Long businessId, String roleLevel, Long overrideBusinessId) {
		TL.set(new Context(businessId, roleLevel, overrideBusinessId));
	}
	public static void clear() { TL.remove(); }
	public static Context current() { return TL.get(); }

	public static Long currentBusinessId() {
		Context c = TL.get();
		if (c == null) return null;
		return c.overrideBusinessId != null ? c.overrideBusinessId : c.businessId;
	}
	public static Long requireBusinessId() {
		Long id = currentBusinessId();
		if (id == null) {
			throw new IllegalStateException("No business context — caller has not completed business registration");
		}
		return id;
	}
	public static boolean canBypassTenant() {
		Context c = TL.get();
		return c != null && "ADMIN".equalsIgnoreCase(c.roleLevel);
	}

	public static final class Context {
		public final Long businessId;
		public final String roleLevel;
		public final Long overrideBusinessId;
		public Context(Long businessId, String roleLevel, Long overrideBusinessId) {
			this.businessId = businessId;
			this.roleLevel = roleLevel;
			this.overrideBusinessId = overrideBusinessId;
		}
	}
}
