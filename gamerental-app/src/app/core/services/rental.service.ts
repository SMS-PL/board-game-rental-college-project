import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Rental, RentalRequest, ReturnRequest } from '../models/rental.model';

@Injectable({ providedIn: 'root' })
export class RentalService {
  private apiUrl = '/api/rentals'; // FIXED: was /api/v1/rentals

  constructor(private http: HttpClient) {}

  getRentals(status?: string, clientId?: string, gameId?: string): Observable<Rental[]> {
    let params = new HttpParams();
    if (status) params = params.set('status', status);
    if (clientId) params = params.set('clientId', clientId); // FIXED: was customerId
    if (gameId) params = params.set('gameId', gameId);
    return this.http.get<Rental[]>(this.apiUrl, { params });
  }

  getRental(id: string): Observable<Rental> {
    return this.http.get<Rental>(`${this.apiUrl}/${id}`);
  }

  createRental(req: RentalRequest): Observable<{ id: string }> {
    return this.http.post<{ id: string }>(this.apiUrl, req);
  }

  returnRental(id: string, req: ReturnRequest): Observable<Rental> {
    // FIXED: was POST with empty body — now PATCH with ReturnGameCommand
    return this.http.patch<Rental>(`${this.apiUrl}/${id}/return`, req);
  }
}
