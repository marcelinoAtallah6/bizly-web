export const TRAVEL_PASSPORT_TYPES = [
  { value: 'P', label: 'P — Ordinary' },
  { value: 'D', label: 'D — Diplomatic' },
  { value: 'S', label: 'S — Service' },
  { value: 'O', label: 'O — Other' },
] as const;

export const TRAVEL_LOYALTY_PROGRAMS = [
  'None',
  'SkyTeam',
  'Star Alliance',
  'Oneworld',
  'Emirates Skywards',
  'Marriott Bonvoy',
  'Hilton Honors',
  'IHG One Rewards',
  'Other',
] as const;

export const TRAVEL_LOYALTY_STATUSES = ['Active', 'Inactive', 'Silver', 'Gold', 'Platinum'] as const;

export const TRAVEL_PHONE_COUNTRY_CODES = [
  { code: '+1', label: 'US/CA +1' },
  { code: '+44', label: 'UK +44' },
  { code: '+33', label: 'FR +33' },
  { code: '+49', label: 'DE +49' },
  { code: '+971', label: 'AE +971' },
  { code: '+966', label: 'SA +966' },
  { code: '+91', label: 'IN +91' },
  { code: '+81', label: 'JP +81' },
  { code: '+61', label: 'AU +61' },
  { code: '+961', label: 'LB +961' },
] as const;

export const TRAVEL_PREFERRED_AIRLINES = [
  'Emirates',
  'Qatar Airways',
  'Turkish Airlines',
  'Air France',
  'Lufthansa',
  'British Airways',
  'Etihad',
  'Saudia',
  'Middle East Airlines',
  'Other',
] as const;

export const TRAVEL_PREFERRED_HOTELS = [
  'Marriott',
  'Hilton',
  'Hyatt',
  'IHG',
  'Accor',
  'Four Seasons',
  'Rotana',
  'Mövenpick',
  'Other',
] as const;
