import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Game, GameCopy, GameDetail, GameTag, CreateGameRequest, UpdateGameRequest } from '../../core/models/game.model';

@Injectable({ providedIn: 'root' })
export class GamesService {
  private readonly apiUrl = '/api/games';

  constructor(private readonly http: HttpClient) {}

  getGames(tag?: GameTag, sort?: string): Observable<Game[]> {
    let params = new HttpParams();
    if (tag) params = params.set('tag', tag);
    if (sort) params = params.set('sort', sort);
    return this.http.get<Game[]>(this.apiUrl, { params });
  }

  getGame(id: string): Observable<GameDetail> {
    return this.http.get<GameDetail>(`${this.apiUrl}/${id}`);
  }

  createGame(request: CreateGameRequest): Observable<Game> {
    return this.http.post<Game>(this.apiUrl, request);
  }

  updateGame(id: string, request: UpdateGameRequest): Observable<Game> {
    return this.http.put<Game>(`${this.apiUrl}/${id}`, request);
  }

  deleteGame(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  updateCopies(id: string, totalCopies: number): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${id}/copies`, { totalCopies });
  }

  getAvailableCopies(id: string): Observable<GameCopy[]> {
    return this.http.get<GameCopy[]>(`${this.apiUrl}/${id}/available-copies`);
  }
}

