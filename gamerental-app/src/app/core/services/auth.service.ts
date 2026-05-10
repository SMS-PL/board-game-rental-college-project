import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, switchMap, tap } from 'rxjs';
import { AuthResponse, LoginRequest, RegisterRequest } from '../models/auth.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = '/api/auth'; // FIXED: was /api/v1/auth

  constructor(private http: HttpClient) {}

  login(req: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, req).pipe(
      tap(res => localStorage.setItem('jwt_token', res.token))
    );
  }

  register(req: RegisterRequest): Observable<AuthResponse> {
    // Backend returns void on register — auto-login afterwards to get token
    return this.http.post<void>(`${this.apiUrl}/register`, req).pipe(
      switchMap(() => this.login(req))
    );
  }

  logout(): void {
    localStorage.removeItem('jwt_token');
  }

  getToken(): string | null {
    return localStorage.getItem('jwt_token');
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }
}
