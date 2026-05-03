export interface Client {
  id: string;
  firstName: string;
  lastName: string;
  phone?: string;
  email?: string;
  createdAt: string;
  activeRentalsCount?: number;
}
export interface CreateClientRequest {
  firstName: string;
  lastName: string;
  phone?: string;
  email?: string;
}
export interface UpdateClientRequest {
  firstName: string;
  lastName: string;
  phone?: string;
  email?: string;
}
