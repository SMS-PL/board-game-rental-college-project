import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { NavbarComponent } from '../../shared/navbar/navbar.component';
import { StatsService } from '../../core/services/stats.service';
import { StatsSummary, PopularGame } from '../../core/models/stats.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, NavbarComponent],
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit {
  stats: StatsSummary | null = null;
  popularGames: PopularGame[] = [];
  loadingStats = true;
  loadingGames = true;

  constructor(private statsService: StatsService) {}

  ngOnInit(): void {
    this.statsService.getSummary().subscribe({
      next: s => { this.stats = s; this.loadingStats = false; },
      error: () => { this.loadingStats = false; }
    });
    this.statsService.getPopularGames().subscribe({
      next: g => { this.popularGames = g; this.loadingGames = false; },
      error: () => { this.popularGames = []; this.loadingGames = false; }
    });
  }
}
