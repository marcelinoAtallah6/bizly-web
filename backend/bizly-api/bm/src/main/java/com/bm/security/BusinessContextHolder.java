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
		set(businessId, roleLevel, overrideBusinessId, null);
	}

	public static void set(Long businessId, String roleLevel, Long overrideBusinessId, Boolean tenantBypass) {
		TL.set(new Context(businessId, roleLevel, overrideBusinessId, tenantBypass));
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
	 * available. The error message distinguishes admins in "all businesses" mode (asks them to pick
	 * one via the header dropdown) from regular users with an incomplete profile.
	 */
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
	 * True only for the portal root admin ({@code tenantBypass} in JWT). Delegated internal admin
	 * roles keep {@code roleLevel = ADMIN} for provisioning flows but must obey the menu matrix.
	 */
	public static boolean canBypassTenant() {
		Context c = TL.get();
		if (c == null) {
			return false;
		}
		if (c.tenantBypass != null) {
			return c.tenantBypass;
		}
		return "ADMIN".equalsIgnoreCase(c.roleLevel);
	}

	public static final class Context {
		public final Long businessId;
		public final String roleLevel;
		/** Admin-set "act-as" override of the business id; null otherwise. */
		public final Long overrideBusinessId;
		/** From JWT {@code tenantBypass}; null on legacy tokens (falls back to roleLevel). */
		public final Boolean tenantBypass;

		public Context(Long businessId, String roleLevel, Long overrideBusinessId, Boolean tenantBypass) {
			this.businessId = businessId;
			this.roleLevel = roleLevel;
			this.overrideBusinessId = overrideBusinessId;
			this.tenantBypass = tenantBypass;
		}
	}
}
