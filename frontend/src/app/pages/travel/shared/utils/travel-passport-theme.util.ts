/** Accent palette keyed by destination / nationality country code (ISO-2 or first 2 letters). */
const COUNTRY_ACCENTS: Record<string, { primary: string; secondary: string; paper: string }> = {
  US: { primary: '#1a237e', secondary: '#c62828', paper: '#f5f0e6' },
  GB: { primary: '#0d47a1', secondary: '#b71c1c', paper: '#f3efe8' },
  FR: { primary: '#1565c0', secondary: '#c62828', paper: '#f4f0ea' },
  DE: { primary: '#212121', secondary: '#ffc107', paper: '#f2efe9' },
  AE: { primary: '#00695c', secondary: '#c62828', paper: '#f0ebe3' },
  SA: { primary: '#1b5e20', secondary: '#ffffff', paper: '#f2ece4' },
  IN: { primary: '#ff6f00', secondary: '#1b5e20', paper: '#f8f2e8' },
  JP: { primary: '#b71c1c', secondary: '#ffffff', paper: '#f5f0ea' },
  AU: { primary: '#0d47a1', secondary: '#c62828', paper: '#f3efe8' },
  CA: { primary: '#c62828', secondary: '#ffffff', paper: '#f4f0ea' },
};

const DEFAULT_ACCENT = { primary: '#37474f', secondary: '#1565c0', paper: '#f3efe6' };

export function travelPassportAccent(country?: string | null): { primary: string; secondary: string; paper: string } {
  const key = (country ?? '').trim().slice(0, 2).toUpperCase();
  if (key.length === 2 && COUNTRY_ACCENTS[key]) {
    return COUNTRY_ACCENTS[key];
  }
  return DEFAULT_ACCENT;
}

export function travelPassportMrzLine(passport: {
  passportNo?: string;
  fullName?: string;
  nationality?: string;
  passportType?: string;
}): string {
  const type = (passport.passportType ?? 'P').slice(0, 1).toUpperCase();
  const name = (passport.fullName ?? 'TRAVELER').toUpperCase().replace(/\s+/g, '<');
  const no = (passport.passportNo ?? '00000000').toUpperCase();
  const nat = (passport.nationality ?? 'XXX').toUpperCase().slice(0, 3);
  return `${type}<${name}<<${no}<<${nat}`;
}
