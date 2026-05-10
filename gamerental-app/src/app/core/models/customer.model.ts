import { Rental } from './rental.model';

export interface Customer {
  id: string;
  firstName: string;
  lastName: string;
  phone?: string;
  email?: string;
  activeRentalsCount: number;
  createdAt: string;
}

export interface CustomerDetail {
  id: string;
  firstName: string;
  lastName: string;
  phone?: string;
  email?: string;
  rentals: Rental[];
  createdAt: string;
}

export interface CustomerRequest {
  firstName: string;
  lastName: string;
  phone?: string;
  email?: string;
}
