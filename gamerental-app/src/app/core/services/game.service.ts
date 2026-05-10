import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { Game, GameDetail, GameCopy, GameRequest, UpdateGameRequest } from '../models/game.model';

@Injectable({ providedIn: 'root' })
export class GameService {
  private apiUrl = '/api/games'; // FIXED: was /api/v1/games

  constructor(private http: HttpClient) {}

  getGames(tag?: string, sortBy = 'title'): Observable<Game[]> {
    let params = new HttpParams().set('sortBy', sortBy);
    if (tag) params = params.set('tag', tag);
    return this.http.get<Game[]>(this.apiUrl, { params });
  }

  getGame(id: string): Observable<GameDetail> {
    return this.http.get<GameDetail>(`${this.apiUrl}/${id}`);
  }

  createGame(req: GameRequest): Observable<GameDetail> {
    // Backend returns {id: UUID} — fetch full game detail afterwards
    return this.http.post<{ id: string }>(this.apiUrl, req).pipe(
      switchMap(res => this.getGame(res.id))
    );
  }

  updateGame(id: string, req: UpdateGameRequest): Observable<GameDetail> {
    return this.http.put<GameDetail>(`${this.apiUrl}/${id}`, req);
  }

  deleteGame(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  updateCopies(id: string, totalCopies: number): Observable<GameDetail> {
    return this.http.patch<GameDetail>(`${this.apiUrl}/${id}/copies`, { totalCopies });
  }

  getAvailableCopies(id: string): Observable<GameCopy[]> {
    return this.http.get<GameCopy[]>(`${this.apiUrl}/${id}/available-copies`);
  }
}
