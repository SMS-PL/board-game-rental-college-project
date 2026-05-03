import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Client, CreateClientRequest, UpdateClientRequest } from '../../core/models/client.model';
import { Rental } from '../../core/models/rental.model';

@Injectable({ providedIn: 'root' })
export class ClientsService {
  private readonly apiUrl = '/api/clients';

  constructor(private readonly http: HttpClient) {}

  getClients(search?: string, sort?: string): Observable<Client[]> {
    let params = new HttpParams();
    if (search) params = params.set('search', search);
    if (sort) params = params.set('sort', sort);
    return this.http.get<Client[]>(this.apiUrl, { params });
  }

  getClient(id: string): Observable<Client> {
    return this.http.get<Client>(`${this.apiUrl}/${id}`);
  }

  getClientRentals(id: string): Observable<Rental[]> {
    return this.http.get<Rental[]>(`${this.apiUrl}/${id}/rentals`);
  }

  createClient(request: CreateClientRequest): Observable<Client> {
    return this.http.post<Client>(this.apiUrl, request);
  }

  updateClient(id: string, request: UpdateClientRequest): Observable<Client> {
    return this.http.put<Client>(`${this.apiUrl}/${id}`, request);
  }

  deleteClient(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}

