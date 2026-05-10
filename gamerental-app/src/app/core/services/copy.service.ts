import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';
import { GameCopy, GameDetail } from '../models/game.model';

// CopyService now delegates to /api/games — there is no dedicated /api/copies endpoint
@Injectable({ providedIn: 'root' })
export class CopyService {
  constructor(private http: HttpClient) {}

  /** Returns available copies for a specific game */
  getAvailableCopiesForGame(gameId: string): Observable<GameCopy[]> {
    return this.http.get<GameCopy[]>(`/api/games/${gameId}/available-copies`);
  }

  /** "Add copy" = increment totalCopies by 1 via PATCH /api/games/{id}/copies */
  addCopy(gameId: string): Observable<GameDetail> {
    return this.http.get<GameDetail>(`/api/games/${gameId}`).pipe(
      switchMap(game =>
        this.http.patch<GameDetail>(`/api/games/${gameId}/copies`, { totalCopies: game.totalCopies + 1 })
      )
    );
  }

  /** Set total copies count for a game */
  setCopies(gameId: string, totalCopies: number): Observable<GameDetail> {
    return this.http.patch<GameDetail>(`/api/games/${gameId}/copies`, { totalCopies });
  }

  /** Get all available copies across all games (requires game list first) */
  getAllAvailableCopies(): Observable<(GameCopy & { gameId: string; gameTitle: string })[]> {
    return this.http.get<any[]>('/api/games').pipe(
      map(games => games.filter(g => g.availableCopies > 0)),
      switchMap(games =>
        import('rxjs').then(({ forkJoin, of }) =>
          games.length === 0
            ? of([])
            : forkJoin(
                games.map((g: any) =>
                  this.http.get<GameCopy[]>(`/api/games/${g.id}/available-copies`).pipe(
                    map(copies => copies.map(c => ({ ...c, gameId: g.id, gameTitle: g.title })))
                  )
                )
              ).pipe(map((results: any[]) => results.flat()))
        )
      ),
      switchMap(obs => obs as Observable<any[]>)
    );
  }
}
