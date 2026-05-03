import { CopyCondition } from './game.model';

export enum RentalStatus {
  ACTIVE = 'ACTIVE',
  RETURNED = 'RETURNED',
  OVERDUE = 'OVERDUE'
}

export interface Rental {
  id: string;
  gameCopyId: string;
  gameCopyNumber: number;
  gameId: string;
  gameTitle: string;
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

export interface CreateRentalRequest {
  gameCopyId: string;
  clientId: string;
  rentedFrom: string;
  dueTo: string;
  notes?: string;
}

export interface ReturnRentalRequest {
  conditionOnReturn: CopyCondition;
}
