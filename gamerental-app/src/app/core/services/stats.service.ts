import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, forkJoin } from 'rxjs';
import { map } from 'rxjs/operators';
import { StatsSummary, PopularGame } from '../models/stats.model';
import { Game } from '../models/game.model';
import { Customer } from '../models/customer.model';
import { Rental } from '../models/rental.model';

// Backend has no /api/stats endpoint — compute stats from existing endpoints
@Injectable({ providedIn: 'root' })
export class StatsService {
  constructor(private http: HttpClient) {}

  getSummary(): Observable<StatsSummary> {
    return forkJoin({
      games: this.http.get<Game[]>('/api/games'),
      clients: this.http.get<Customer[]>('/api/clients'),
      rentals: this.http.get<Rental[]>('/api/rentals'),
    }).pipe(
      map(({ games, clients, rentals }) => ({
        totalGames: games.length,
        totalCopies: games.reduce((sum, g) => sum + g.totalCopies, 0),
        totalCustomers: clients.length,
        activeRentals: rentals.filter(r => r.status === 'ACTIVE' || r.status === 'OVERDUE').length,
      }))
    );
  }

  getPopularGames(): Observable<PopularGame[]> {
    return this.http.get<Rental[]>('/api/rentals').pipe(
      map(rentals => {
        const countMap: Record<string, { title: string; count: number }> = {};
        rentals.forEach(r => {
          const key = r.gameTitle;
          if (!countMap[key]) countMap[key] = { title: r.gameTitle, count: 0 };
          countMap[key].count++;
        });
        return Object.entries(countMap)
          .map(([, { title, count }]) => ({ gameId: '', title, rentalCount: count }))
          .sort((a, b) => b.rentalCount - a.rentalCount)
          .slice(0, 5);
      })
    );
  }
}
