/** Combine Material date + `HH:mm` time into backend LocalDateTime string */
export function combineDateAndTimeToApi(date: Date, timeHHmm: string): string {
  const t = timeHHmm.trim();
  const parts = t.split(':').map((x) => parseInt(x, 10));
  const hh = Number.isFinite(parts[0]) ? parts[0] : 0;
  const mm = Number.isFinite(parts[1]) ? parts[1] : 0;
  const y = date.getFullYear();
  const mo = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const h = String(hh).padStart(2, '0');
  const m = String(mm).padStart(2, '0');
  return `${y}-${mo}-${day}T${h}:${m}:00`;
}

/** Split API datetime into Date at local midnight + `HH:mm` */
export function apiDateTimeToDateAndTime(iso: string | undefined | null): {
  date: Date | null;
  timeHHmm: string;
} {
  if (!iso) {
    return { date: null, timeHHmm: '09:00' };
  }
  const s = iso.replace(' ', 'T').trim();
  const datePart = s.substring(0, 10);
  const timePart = s.length >= 16 ? s.substring(11, 16) : '09:00';
  const [y, mo, d] = datePart.split('-').map((x) => parseInt(x, 10));
  if (!y || !mo || !d) {
    return { date: null, timeHHmm: timePart };
  }
  return { date: new Date(y, mo - 1, d), timeHHmm: timePart };
}
