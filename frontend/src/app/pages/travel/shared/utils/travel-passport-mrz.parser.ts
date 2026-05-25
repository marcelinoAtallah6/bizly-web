export interface PassportMrzParseResult {
  fullName?: string;
  passportNo?: string;
  nationality?: string;
  passportType?: string;
  passportExpiryDate?: string;
  passportIssueDate?: string;
  sex?: string;
}

/** Parse ICAO TD3 machine-readable zone lines from OCR text. */
export function parsePassportMrzFromText(text: string): PassportMrzParseResult | null {
  const normalized = text
    .toUpperCase()
    .replace(/[^A-Z0-9<\n]/g, '')
    .split(/\n/)
    .map((l) => l.trim())
    .filter((l) => l.length >= 30 && l.includes('<'));

  if (normalized.length < 2) {
    return null;
  }

  const line1 = normalized.find((l) => l.startsWith('P<') || l.startsWith('P')) ?? normalized[normalized.length - 2];
  const line2 = normalized[normalized.length - 1];
  if (!line1 || !line2 || line2.length < 28) {
    return null;
  }

  const passportType = line1.charAt(0) === 'P' ? line1.charAt(1) || 'P' : 'P';
  const issuing = line1.substring(2, 5).replace(/</g, '');
  const namePart = line1.substring(5).replace(/<+$/, '');
  const [surname, given] = namePart.split('<<');
  const fullName = [given, surname]
    .filter(Boolean)
    .join(' ')
    .replace(/</g, ' ')
    .replace(/\s+/g, ' ')
    .trim();

  const passportNo = line2.substring(0, 9).replace(/</g, '').trim();
  const nationality = line2.substring(10, 13).replace(/</g, '') || issuing;
  const birth = yyMmDdToIso(line2.substring(13, 19));
  const sex = line2.charAt(20);
  const expiry = yyMmDdToIso(line2.substring(21, 27));

  const result: PassportMrzParseResult = {};
  if (fullName) {
    result.fullName = titleCase(fullName);
  }
  if (passportNo) {
    result.passportNo = passportNo;
  }
  if (nationality) {
    result.nationality = nationality;
  }
  if (passportType) {
    result.passportType = passportType;
  }
  if (expiry) {
    result.passportExpiryDate = expiry;
  }
  if (birth) {
    result.passportIssueDate = undefined;
  }
  if (sex && sex !== '<') {
    result.sex = sex;
  }
  return Object.keys(result).length ? result : null;
}

function yyMmDdToIso(yyMmDd: string): string | undefined {
  const raw = (yyMmDd ?? '').replace(/\D/g, '');
  if (raw.length !== 6) {
    return undefined;
  }
  const yy = parseInt(raw.substring(0, 2), 10);
  const mm = raw.substring(2, 4);
  const dd = raw.substring(4, 6);
  if (!Number.isFinite(yy)) {
    return undefined;
  }
  const year = yy >= 50 ? 1900 + yy : 2000 + yy;
  return `${year}-${mm}-${dd}`;
}

function titleCase(s: string): string {
  return s
    .toLowerCase()
    .split(' ')
    .map((w) => (w ? w.charAt(0).toUpperCase() + w.slice(1) : ''))
    .join(' ');
}
