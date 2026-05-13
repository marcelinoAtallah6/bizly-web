# Business Registration & Multi-Tenant Rollout

## What was delivered in this pass

### Database
1. `backend/bizly-api/um/src/main/resources/db/oracle/patch-business-registration-oracle.sql`
   - Creates `UM.UM_ROLE_LEVEL` (ADMIN / BUSINESS classification)
   - Adds `ROLE_LEVEL_ID`, `IS_DEFAULT_FOR_REGISTRATION`, `IS_SYSTEM_RESTRICTED` to `UM.UM_ROLE`
   - Creates `UM.UM_BUSINESS` (tenant table)
   - Adds `BUSINESS_ID`, `FIRST_LOGIN`, `WELCOME_COMPLETED_AT`, `AUTH_PROVIDER`, `PROVIDER_USER_ID` to `UM.UM_USER`
   - Seeds `BUSINESS_ADMIN` role and classifies existing admin roles as system-restricted

2. `backend/bizly-api/um/src/main/resources/db/oracle/patch-business-scope-pm-product-oracle.sql`
   - Single-table example: adds `BUSINESS_ID` column + FK + index to `UM.PM_PRODUCT`
   - Superseded by the comprehensive patch below (kept for reference).

3. `backend/bizly-api/settings/src/main/resources/db/oracle/patch-settings-builder-visibility-oracle.sql`
   - Creates per-builder grant tables mirroring `SETTINGS_DASH_*_GRANT`:
     `SETTINGS_REPORT_ROLE_GRANT`, `SETTINGS_REPORT_USER_GRANT`,
     `SETTINGS_QDEF_ROLE_GRANT`, `SETTINGS_QDEF_USER_GRANT`.
   - Composite PKs with `ON DELETE CASCADE` from the parent report / query def.
   - Idempotent — re-running is safe.

4. `backend/bizly-api/um/src/main/resources/db/oracle/patch-business-scope-all-tenant-tables-oracle.sql`
   - **Comprehensive** patch — adds `BUSINESS_ID` + FK + index to **every** tenant-owned table:
     `PM_PRODUCT`, `PM_PRODUCT_ITEMS`, `PM_CUSTOMER_SALE`, `PM_CUSTOMER_SALE_LINE`,
     `BM_SERVICE_ITEM`, `BM_APPOINTMENT`,
     `KYC_CUSTOMER`, `KYC_CUSTOMER_ORDER`,
     `SETTINGS_DASHBOARD`, `SETTINGS_WIDGET`, `SETTINGS_REPORT`, `SETTINGS_QUERY_DEF`,
     `SETTINGS_DASH_ROLE_GRANT`, `SETTINGS_DASH_USER_GRANT`,
     `NOTIF_INBOX`, `NOTIF_BROADCAST_MESSAGE`, `NOTIF_EMAIL_TEMPLATE`,
     `UM_AUDIT_LOG`.
   - Built-in / admin-defined rows (`IS_BUILTIN=1`, query templates, system email templates)
     are reverted to `BUSINESS_ID = NULL` so they stay globally available.
   - Idempotent — re-running is safe.

### Backend — auth service
- New entities: `BusinessEntity`, `RoleLevelEntity` (+ repos)
- Extended: `UserEntity`, `RoleEntity`, `LoginResponse`
- New service: `RegisterBusinessServiceImpl` (server-decides role; refuses restricted roles; logs security events)
- New service: `SocialLoginServiceImpl` (Google + Facebook verified; Apple stubbed pending JWKS)
- Updated: `LoginServiceImpl.generateAccessToken` — emits `businessId`, `firstLogin`, `roleLevel` claims
- New endpoints:
  - `POST /auth/register-business`
  - `POST /auth/welcome-complete`
  - `POST /auth/me`
  - `POST /auth/roles/assignable`
  - `POST /auth/social/{provider}` (`google` / `facebook` / `apple`)

### Backend — gateway
- `AuthenticationPreFilter` forwards `X-Business-Id`, `X-Role-Level`, `X-First-Login` headers after JWT verification. Strips any inbound copy of those headers first to prevent smuggling.

### Backend — every downstream service (BM, UM, KYC, settings, PM, broadcast)
- New `BusinessContextHolder` ThreadLocal per module (`com.<module>.security.BusinessContextHolder`)
- `InternalAuthFilter` populates it from `X-Business-Id` + `X-Role-Level` and clears it in `finally`
- ADMIN-level callers can override via `X-Business-Override` (the filter intentionally rejects this header from non-ADMIN callers)

### Backend — defence-in-depth
- `UM.UserServiceImpl.add(...)` refuses to grant any role with `is_system_restricted = 1`, even when invoked by an admin

### Backend — builder visibility (Dashboard / Report / Query)
- `SettingsReportRoleGrant` / `SettingsReportUserGrant` / `SettingsQueryDefRoleGrant` / `SettingsQueryDefUserGrant`
  entities + repositories added in `settings`.
- `SettingsReportService.list(...)` and `listActiveForSidebar(...)` overloaded to accept the current
  `username` + `authorities` and filter rows the caller can't see (empty grants = global; admins via
  `BusinessContextHolder.canBypassTenant()` always see everything).
- `SettingsQueryDefService.listPage(...)` overloaded the same way so the Query Builder list,
  Dashboard widget pickers, and Report Builder query dropdowns all honour the same visibility rules.
- Save DTOs (`ReportBuilderSaveRequest`, `QueryDefSaveRequest`) accept `grantRoles` (role names) and
  `grantUsernames`; the service replaces all grants on update.

### Frontend
- `AuthService` exposes `fetchMe`, `registerBusiness`, `completeWelcome`, `listAssignableRoles`, `socialLogin` and persists `bizlyFirstLogin`, `bizlyBusinessId`, `bizlyBusinessName`, `bizlyRoleLevel`
- `OnboardingGuard` (new) — protects the main shell; redirects to wizard / registration as needed
- `GuestGuard` (updated) — exempts the post-login `/authentication/welcome` and `/authentication/register-business` paths
- `WelcomeWizardComponent` — full-screen, 4-step carousel; finishes with `/auth/welcome-complete`
- `RegisterBusinessComponent` — business name + type picker; role is read-only and decided server-side
- Login screen — social buttons wired to `POST /auth/social/<provider>`; post-login redirect honours `firstLogin` and `businessId`
- Report Builder + Query Builder — new "Visibility" sections mirroring the Dashboard Builder pattern:
  multi-select for roles, autocomplete + chip-set for users, "global" label on rows with no grants.

### Wired-up tenant entities (entity + repo + service)
- **BM**: `Product`, `Appointment`, `ServiceItem`, `CustomerSale` (+ `CustomerSaleLine` denormalized)
  - Repositories expose `findByIdAndBusinessId`, `findAllByBusinessId`, and scoped paged finders.
  - Services use `BusinessContextHolder.requireBusinessId()` on every `add`/`update`/`delete`/`get`/`gets`.
- **KYC**: `KycCustomer` — entity + repo (`findByIdAndBusinessId`, `findAllByBusinessId`, `searchByBusinessId`) ready; service layer still needs to be wired (see checklist).
- **BM**: `NotifInbox`, `ProductItem` — entity field present (denormalized scope); repositories/services still need wiring.
- **Settings**: `SettingsDashboard`, `SettingsReport`, `SettingsQueryDef` — entity field present, NULL = global / built-in. Service wiring TODO.
- **Broadcast**: `BroadcastMessage`, `MailTemplateEntity` — entity field present. Service wiring TODO.

The tracking table further down lists exactly which entities/repos/services are wired vs. TODO.

## Apply order

1. Run the DDL patches against your Oracle DB:
   ```sql
   @backend/bizly-api/um/src/main/resources/db/oracle/patch-business-registration-oracle.sql
   @backend/bizly-api/um/src/main/resources/db/oracle/patch-business-scope-all-tenant-tables-oracle.sql
   @backend/bizly-api/settings/src/main/resources/db/oracle/patch-settings-builder-visibility-oracle.sql
   ```
   (The standalone `patch-business-scope-pm-product-oracle.sql` is superseded by the
   second script above — running both is harmless thanks to the idempotency guards,
   but the consolidated file alone is sufficient.)
2. Rebuild + redeploy `api-auth` and `api-gateway` first (clients depend on the new claims/headers).
3. Rebuild + redeploy each downstream `bizly-api/*` module so they pick up `BusinessContextHolder`.
4. Rebuild the Angular frontend.
5. Verify with an end-to-end run (see below).

## End-to-end verification

| Scenario | Expected |
|---|---|
| New user logs in (first_login=1) | Redirected to `/authentication/welcome` |
| User completes wizard | `first_login` flipped to 0; user routed to `/authentication/register-business` |
| User submits business name + type | Server creates `UM_BUSINESS` row, links user, assigns `BUSINESS_ADMIN`, returns new JWT including `businessId` |
| User immediately calls `POST /pm/product/add` | Product is created with their `business_id`; their `gets` only returns rows in that business |
| Same user POSTs `/auth/register-business` again | 409 `BUSINESS_ALREADY_REGISTERED`, security event logged |
| Client tampers JSON to add `roleId: 1` | Field ignored (DTO doesn't declare it); server picks the default registration role from DB |
| Admin login (role_level=ADMIN) | `/dashboard` reachable without business; admin can call any module via `X-Business-Override: <id>` |

## Configuration to add

`backend/bizly-security/api-auth/src/main/resources/application.yaml`:

```yaml
social:
  google:
    clientId: <YOUR_GOOGLE_OAUTH_CLIENT_ID>
```

(Optional but recommended for hardening — Google audience check is enabled when the value is set.)

## Apple sign-in completion

`SocialLoginServiceImpl.verifyApple()` currently throws `501 NOT_IMPLEMENTED`. To enable:
1. Fetch and cache Apple's JWKS from `https://appleid.apple.com/auth/keys`
2. Decode the `identityToken`, verify the JWS signature against the JWK whose `kid` matches the token header
3. Validate `iss == https://appleid.apple.com`, `aud == <your services-id>`, `exp` in future
4. Extract `email` (or `sub` if the user chose to hide their email) → fill `ProviderProfile`

## Per-entity scope rollout — checklist

For every tenant-owned entity (NOT every entity — system catalogs like `UM_MENU`,
`UM_ROLE`, `SETTINGS_QUERY_DEF` stay global), follow the same pattern shown
for `PM_PRODUCT`:

1. **DDL** — add `BUSINESS_ID NUMBER`, an index on `(BUSINESS_ID, <natural sort col>)`,
   and an optional FK to `UM.UM_BUSINESS(ID)`. Backfill existing rows to the first
   ACTIVE business (or NULL if you'd rather force re-creation).
2. **Entity** — add `private Long businessId` + getter/setter; map `@Column(name = "business_id")`.
3. **Repository** — add `findByIdAndBusinessId(...)` and `findAllByBusinessId(...)`
   variants. Delete the un-scoped finders or restrict their use.
4. **Service** —
   - `add`: call `BusinessContextHolder.requireBusinessId()` and set it on the new entity.
   - `update`/`delete`: load by `(id, businessId)` so a cross-tenant id is treated as not found.
   - `get`/`gets`: use the scoped repo methods.
   - For admin bulk-read screens, gate on `BusinessContextHolder.canBypassTenant()`.

### Entity-by-entity tracking table

The script `um/src/main/resources/db/oracle/patch-business-scope-all-tenant-tables-oracle.sql`
adds `BUSINESS_ID` + FK + index to every row marked **DDL ✅** below in one idempotent run.

| Module | Table | DDL | Entity | Repo (`findByIdAndBusinessId` etc.) | Service uses `BusinessContextHolder` |
|---|---|:---:|:---:|:---:|:---:|
| BM       | `PM_PRODUCT`               | ✅ | ✅ | ✅ | ✅ |
| BM       | `PM_PRODUCT_ITEMS`         | ✅ | ✅ | (inherits via FK) | (inherits via parent save) |
| BM       | `PM_CUSTOMER_SALE`         | ✅ | ✅ | ✅ | ✅ |
| BM       | `PM_CUSTOMER_SALE_LINE`    | ✅ | ✅ | (inherits via FK) | ✅ (set on cascade save) |
| BM       | `BM_SERVICE_ITEM`          | ✅ | ✅ | ✅ | ✅ |
| BM       | `BM_APPOINTMENT`           | ✅ | ✅ | ✅ | ✅ |
| BM       | `NOTIF_INBOX`              | ✅ | ✅ | TODO — extend `NotifInboxRepository` | TODO — set in `NotifInboxServiceImpl.publish()` |
| KYC      | `KYC_CUSTOMER`             | ✅ | ✅ | ✅ | TODO — wire `KycCustomerServiceImpl` |
| KYC      | `KYC_CUSTOMER_ORDER`       | ✅ | (no JPA entity yet) | — | — |
| Settings | `SETTINGS_DASHBOARD`       | ✅ | ✅ | TODO — add `findByIdAndBusinessIdOrIsBuiltinTrue` | TODO |
| Settings | `SETTINGS_WIDGET`          | ✅ | (denormalized; inherits dashboard) | — | — |
| Settings | `SETTINGS_REPORT`          | ✅ | ✅ | TODO | TODO |
| Settings | `SETTINGS_QUERY_DEF`       | ✅ | ✅ | TODO | TODO |
| Settings | `SETTINGS_DASH_ROLE_GRANT` | ✅ | (inherits dashboard) | — | — |
| Settings | `SETTINGS_DASH_USER_GRANT` | ✅ | (inherits dashboard) | — | — |
| Settings | `SETTINGS_USER_FAVORITE`   | n/a | already user-scoped | — | — |
| Broadcast | `NOTIF_BROADCAST_MESSAGE` | ✅ | ✅ | TODO | TODO |
| Broadcast | `NOTIF_EMAIL_TEMPLATE`    | ✅ | ✅ (NULL = global) | TODO | TODO |
| UM       | `UM_USER`                  | ✅ | ✅ | ✅ | ✅ |
| UM       | `UM_AUDIT_LOG`             | ✅ | (set by `AuditAspect`) | — | TODO — stamp from `BusinessContextHolder` in `AuditAspect` |

Legend: ✅ done · TODO · — not applicable.

### Sample DDL template

```sql
DECLARE v_count NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_count FROM ALL_TAB_COLUMNS
   WHERE OWNER = 'UM' AND TABLE_NAME = '<TABLE>' AND COLUMN_NAME = 'BUSINESS_ID';
  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE UM.<TABLE> ADD (BUSINESS_ID NUMBER)';
    EXECUTE IMMEDIATE 'CREATE INDEX UM.IDX_<TABLE>_BUSINESS ON UM.<TABLE> (BUSINESS_ID)';
    EXECUTE IMMEDIATE 'ALTER TABLE UM.<TABLE> ADD CONSTRAINT FK_<TABLE>_BUSINESS '
                   || 'FOREIGN KEY (BUSINESS_ID) REFERENCES UM.UM_BUSINESS (ID)';
  END IF;
END;
/
COMMIT;
```

### Sample service hook

```java
@Override
public AddXResponse add(AddXRequest request) {
  Long businessId = BusinessContextHolder.requireBusinessId();
  X x = new X();
  x.setBusinessId(businessId);
  // …
  repository.save(x);
  return new AddXResponse(x.getId());
}

@Override
public GetXResponse get(GetXRequest request) {
  Long businessId = BusinessContextHolder.requireBusinessId();
  X x = repository.findByIdAndBusinessId(request.getId(), businessId)
    .orElseThrow(() -> new ServiceException(ApiMessages.X_NOT_FOUND, HttpStatus.NOT_FOUND));
  return mapToResponse(x);
}
```

### Admin / super-admin override

Admin screens that need to see across businesses send the chosen business id as
the `X-Business-Override` header. The downstream `InternalAuthFilter` only honors
this when the caller's `X-Role-Level` is `ADMIN`. Reads/writes then use the
override id rather than the user's own.

The frontend should expose this picker only when `getCachedRoleLevel() === 'ADMIN'`.
