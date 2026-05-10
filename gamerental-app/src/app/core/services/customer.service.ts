import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';
import { Customer, CustomerDetail, CustomerRequest } from '../models/customer.model';
import { Rental } from '../models/rental.model';

@Injectable({ providedIn: 'root' })
export class CustomerService {
  private apiUrl = '/api/clients'; // FIXED: was /api/v1/customers

  constructor(private http: HttpClient) {}

  getCustomers(search?: string, sortBy = 'lastName'): Observable<Customer[]> {
    let params = new HttpParams().set('sortBy', sortBy);
    if (search) params = params.set('search', search);
    return this.http.get<Customer[]>(this.apiUrl, { params });
  }

  getCustomer(id: string): Observable<CustomerDetail> {
    return this.http.get<CustomerDetail>(`${this.apiUrl}/${id}`);
  }

  createCustomer(req: CustomerRequest): Observable<Customer> {
    // Backend returns {id: UUID} — fetch full customer afterwards
    return this.http.post<{ id: string }>(this.apiUrl, req).pipe(
      switchMap(res => this.http.get<Customer>(`${this.apiUrl}/${res.id}`))
    );
  }

  updateCustomer(id: string, req: CustomerRequest): Observable<CustomerDetail> {
    return this.http.put<CustomerDetail>(`${this.apiUrl}/${id}`, req);
  }

  deleteCustomer(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // Rentals are embedded in ClientDetailDto — extract from getCustomer
  getRentals(id: string): Observable<Rental[]> {
    return this.getCustomer(id).pipe(map(c => c.rentals));
  }
}
