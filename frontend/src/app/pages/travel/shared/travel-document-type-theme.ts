/** Document card header colors — used on the Documents screen only. */
export interface TravelDocumentTypeTheme {
  label: string;
  icon: string;
  headerFrom: string;
  headerTo: string;
  accent: string;
}

const THEMES: Record<string, TravelDocumentTypeTheme> = {
  PASSPORT: {
    label: 'Passport',
    icon: 'badge',
    headerFrom: '#5e35b1',
    headerTo: '#7e57c2',
    accent: '#6a1b9a',
  },
  VISA: {
    label: 'Visa',
    icon: 'card_travel',
    headerFrom: '#2e7d32',
    headerTo: '#43a047',
    accent: '#2e7d32',
  },
  TICKET: {
    label: 'Ticket',
    icon: 'flight',
    headerFrom: '#ef6c00',
    headerTo: '#fb8c00',
    accent: '#ef6c00',
  },
  INSURANCE: {
    label: 'Insurance',
    icon: 'health_and_safety',
    headerFrom: '#00838f',
    headerTo: '#26a69a',
    accent: '#00838f',
  },
  INVOICE: {
    label: 'Invoice',
    icon: 'receipt_long',
    headerFrom: '#1565c0',
    headerTo: '#42a5f5',
    accent: '#1565c0',
  },
  CONTRACT: {
    label: 'Contract',
    icon: 'gavel',
    headerFrom: '#c62828',
    headerTo: '#e53935',
    accent: '#c62828',
  },
  ID: {
    label: 'ID',
    icon: 'fingerprint',
    headerFrom: '#546e7a',
    headerTo: '#78909c',
    accent: '#546e7a',
  },
  OTHER: {
    label: 'Other',
    icon: 'description',
    headerFrom: '#455a64',
    headerTo: '#607d8b',
    accent: '#546e7a',
  },
};

export function travelDocumentTypeTheme(docType?: string | null): TravelDocumentTypeTheme {
  const key = (docType ?? 'OTHER').toUpperCase().replace(/\s+/g, '_');
  for (const code of Object.keys(THEMES)) {
    if (key.includes(code)) {
      return THEMES[code];
    }
  }
  return THEMES['OTHER'];
}
