/** Shared maker/checker tier helpers (same rules as legacy workflow-config dialog). */

export function parseStringMatrix(json: string | null | undefined): string[][] {
  if (json == null || !String(json).trim()) {
    return [];
  }
  try {
    const root = JSON.parse(json) as unknown;
    if (!Array.isArray(root) || root.length === 0) {
      return [];
    }
    if (Array.isArray(root[0])) {
      return (root as unknown[][]).map((tier) =>
        tier.filter((x): x is string => typeof x === 'string').map((s) => s.trim())
      );
    }
    const one = (root as unknown[])
      .filter((x): x is string => typeof x === 'string')
      .map((s) => s.trim());
    return one.length ? [one] : [];
  } catch {
    return [];
  }
}

export function parseFlatStrings(json: string | null | undefined): string[] {
  const m = parseStringMatrix(json);
  return m.length ? m[0] ?? [] : [];
}

export function stripTrailingEmptyTiers(tiers: string[][]): string[][] {
  const t = tiers.map((row) => row.filter((s) => !!s?.trim()));
  while (t.length > 1 && (t[t.length - 1]?.length ?? 0) === 0) {
    t.pop();
  }
  return t;
}

/** Single tier → flat JSON array; multiple tiers → nested array. */
export function serializeCheckerTiers(tiers: string[][]): string | null {
  const cleaned = stripTrailingEmptyTiers(tiers.map((r) => r.map((s) => s.trim())));
  const nonEmpty = cleaned.filter((r) => r.length > 0);
  if (nonEmpty.length === 0) {
    return null;
  }
  if (nonEmpty.length === 1) {
    return JSON.stringify(nonEmpty[0]);
  }
  return JSON.stringify(nonEmpty);
}

export function hasCheckerAssignment(userOverride: boolean, roleTiers: string[][], userTiers: string[][]): boolean {
  const effective = userOverride ? stripTrailingEmptyTiers(userTiers) : stripTrailingEmptyTiers(roleTiers);
  return effective.some((t) => t.length > 0);
}
