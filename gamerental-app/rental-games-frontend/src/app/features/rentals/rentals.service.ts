import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Rental, CreateRentalRequest, ReturnRentalRequest, RentalStatus } from '../../core/models/rental.model';

@Injectable({ providedIn: 'root' })
export class RentalsService {
  private readonly apiUrl = '/api/rentals';

  constructor(private readonly http: HttpClient) {}

  getRentals(status?: RentalStatus, clientId?: string, gameId?: string): Observable<Rental[]> {
    let params = new HttpParams();
    if (status) params = params.set('status', status);
    if (clientId) params = params.set('clientId', clientId);
    if (gameId) params = params.set('gameId', gameId);
    return this.http.get<Rental[]>(this.apiUrl, { params });
  }

  getRental(id: string): Observable<Rental> {
    return this.http.get<Rental>(`${this.apiUrl}/${id}`);
  }

  createRental(request: CreateRentalRequest): Observable<Rental> {
    return this.http.post<Rental>(this.apiUrl, request);
  }

  returnRental(id: string, request: ReturnRentalRequest): Observable<Rental> {
    return this.http.patch<Rental>(`${this.apiUrl}/${id}/return`, request);
  }
}

