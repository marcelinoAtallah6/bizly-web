package com.bm.security;

/**
 * Request-scoped holder for the caller's tenant identifiers, populated by
 * {@code InternalAuthFilter} from the gateway-injected {@code X-Business-Id} /
 * {@code X-Role-Level} headers and cleared at the end of the request.
 *
 * <p>Every repository / service in this module MUST filter writes and reads by
 * {@link #requireBusinessId()} unless {@link #canBypassTenant()} returns true
 * (system admin). The {@link Context} record exists so we can extend the
 * scope (e.g. impersonation, business chooser) without breaking callers.</p>
 *
 * <p>Usage:</p>
 * <pre>
 *   Long bid = BusinessContextHolder.requireBusinessId();
 *   productRepository.findByBusinessId(bid, ...);
 * </pre>
 */
public final class BusinessContextHolder {

	private static final ThreadLocal<Context> TL = new ThreadLocal<>();

	private BusinessContextHolder() {}

	public static void set(Long businessId, String roleLevel, Long overrideBusinessId) {
		TL.set(new Context(businessId, roleLevel, overrideBusinessId));
	}

	public static void clear() {
		TL.remove();
	}

	public static Context current() {
		return TL.get();
	}

	public static Long currentBusinessId() {
		Context c = TL.get();
		if (c == null) return null;
		return c.overrideBusinessId != null ? c.overrideBusinessId : c.businessId;
	}

	/**
	 * Resolves the business id to use for queries — never returns {@code null}. For non-admin users
	 * this is always the user's own {@code business_id}; for admin users that have explicitly chosen
	 * a target business via the admin business-picker, it's that chosen id. Throws when neither is
	 * available.
	 */
	public static Long requireBusinessId() {
		Long id = currentBusinessId();
		if (id == null) {
			throw new IllegalStateException("No business context — caller has not completed business registration");
		}
		return id;
	}

	/**
	 * True when the caller is on an ADMIN-level role and can therefore read/write across all
	 * businesses (subject to the explicit business chooser). Use with caution — every admin call
	 * must still log who and what.
	 */
	public static boolean canBypassTenant() {
		Context c = TL.get();
		return c != null && "ADMIN".equalsIgnoreCase(c.roleLevel);
	}

	public static final class Context {
		public final Long businessId;
		public final String roleLevel;
		/** Admin-set "act-as" override of the business id; null otherwise. */
		public final Long overrideBusinessId;

		public Context(Long businessId, String roleLevel, Long overrideBusinessId) {
			this.businessId = businessId;
			this.roleLevel = roleLevel;
			this.overrideBusinessId = overrideBusinessId;
		}
	}
}
