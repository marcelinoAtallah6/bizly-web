/** Format Date for travel profile date-only fields (YYYY-MM-DD). */
export function formatTravelDateOnly(date: Date | null | undefined): string | undefined {
  if (!date || !(date instanceof Date) || Number.isNaN(date.getTime())) {
    return undefined;
  }
  const y = date.getFullYear();
  const mo = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${y}-${mo}-${day}`;
}

export function parseTravelDateOnly(iso?: string | null): Date | null {
  if (!iso?.trim()) {
    return null;
  }
  const s = iso.trim().substring(0, 10);
  const [y, mo, d] = s.split('-').map((x) => parseInt(x, 10));
  if (!y || !mo || !d) {
    return null;
  }
  return new Date(y, mo - 1, d);
}

/** Alias used by booking / trip-request dialogs. */
export const formatTravelDate = formatTravelDateOnly;
export const parseTravelDate = parseTravelDateOnly;
