package com.um.security;

/**
 * See {@code com.bm.security.BusinessContextHolder} for full semantics.
 * Duplicated per module to avoid a shared bizly-common dependency for now.
 */
public final class BusinessContextHolder {
	private static final ThreadLocal<Context> TL = new ThreadLocal<>();
	private BusinessContextHolder() {}

	public static void set(Long businessId, String roleLevel, Long overrideBusinessId) {
		set(businessId, roleLevel, overrideBusinessId, null);
	}

	public static void set(Long businessId, String roleLevel, Long overrideBusinessId, Boolean tenantBypass) {
		TL.set(new Context(businessId, roleLevel, overrideBusinessId, tenantBypass));
	}
	public static void clear() { TL.remove(); }
	public static Context current() { return TL.get(); }

	public static Long currentBusinessId() {
		Context c = TL.get();
		if (c == null) return null;
		return c.overrideBusinessId != null ? c.overrideBusinessId : c.businessId;
	}
	public static Long requireBusinessId() {
		Context c = TL.get();
		Long id = currentBusinessId();
		if (id == null) {
			if (c != null && "ADMIN".equalsIgnoreCase(c.roleLevel)) {
				throw new IllegalStateException(
						"Select a specific business from the header dropdown to perform this action.");
			}
			throw new IllegalStateException(
					"Your account is not linked to a business yet — finish registration before continuing.");
		}
		return id;
	}
	/**
	 * True only for the portal root admin ({@code tenantBypass} in JWT). Cross-tenant reads/writes
	 * without selecting a business.
	 */
	public static boolean canBypassTenant() {
		Context c = TL.get();
		if (c == null) {
			return false;
		}
		return c.tenantBypass != null && c.tenantBypass;
	}

	/**
	 * True for any portal admin session ({@code roleLevel = ADMIN}), including delegated internal
	 * admin roles under Super Admin. Use for UM role/user screens; use {@link #canBypassTenant()}
	 * only for cross-tenant data access.
	 */
	public static boolean isPortalAdminRoleLevel() {
		Context c = TL.get();
		return c != null && "ADMIN".equalsIgnoreCase(c.roleLevel);
	}

	public static final class Context {
		public final Long businessId;
		public final String roleLevel;
		public final Long overrideBusinessId;
		public final Boolean tenantBypass;
		public Context(Long businessId, String roleLevel, Long overrideBusinessId, Boolean tenantBypass) {
			this.businessId = businessId;
			this.roleLevel = roleLevel;
			this.overrideBusinessId = overrideBusinessId;
			this.tenantBypass = tenantBypass;
		}
	}
}
