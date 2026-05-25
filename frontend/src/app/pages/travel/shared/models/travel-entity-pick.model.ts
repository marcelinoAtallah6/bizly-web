import {
  TravelBookingRow,
  TravelClientRow,
  TravelInvoiceRow,
  TravelPackageRow,
} from 'src/app/core/models/travel.models';
import { GetUserResponse } from 'src/app/core/models/um.models';

export type TravelEntityType = 'client' | 'package' | 'booking' | 'invoice' | 'user';

export interface TravelEntityPick {
  id: number;
  primaryLabel: string;
  secondaryLabel?: string;
  raw: TravelClientRow | TravelPackageRow | TravelBookingRow | TravelInvoiceRow | GetUserResponse;
}
