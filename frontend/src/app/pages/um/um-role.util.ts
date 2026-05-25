import { AuthService } from 'src/app/services/auth.service';

export function normalizeRoleAuthority(name: string): string {
  const t = name.trim().toUpperCase();
  return t.startsWith('ROLE_') ? t : `ROLE_${t}`;
}

/** Team role id matching the JWT active (session) role, if any. */
export function resolveActiveTeamRoleId(
  auth: AuthService,
  teamRoles: { id: number; name: string }[]
): number | null {
  const active = auth.getJwtActiveRole() ?? auth.getJwtRoleNames()[0];
  if (!active) {
    return null;
  }
  const norm = normalizeRoleAuthority(active);
  const hit = teamRoles.find((r) => normalizeRoleAuthority(r.name) === norm);
  return hit?.id ?? null;
}

export function isOwnActiveTeamRole(
  auth: AuthService,
  teamRoles: { id: number; name: string }[],
  roleId: number | null | undefined
): boolean {
  if (roleId == null) {
    return false;
  }
  const activeId = resolveActiveTeamRoleId(auth, teamRoles);
  return activeId != null && activeId === roleId;
}
