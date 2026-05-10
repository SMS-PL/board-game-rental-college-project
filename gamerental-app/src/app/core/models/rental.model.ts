export type CopyCondition = 'NEW' | 'GOOD' | 'WORN' | 'DAMAGED';
export type RentalStatus = 'ACTIVE' | 'RETURNED' | 'OVERDUE';

export interface Rental {
  id: string;
  gameCopyId: string;
  gameTitle: string;
  copyNumber: number;
  clientId: string;
  clientName: string;
  rentedFrom: string;
  dueTo: string;
  returnedAt?: string;
  conditionOnReturn?: CopyCondition;
  status: RentalStatus;
  notes?: string;
  createdAt: string;
}

export interface RentalRequest {
  gameCopyId: string;
  clientId: string;
  rentedFrom: string;
  dueTo: string;
  notes?: string;
}

export interface ReturnRequest {
  conditionOnReturn: CopyCondition;
  returnedAt?: string;
}
