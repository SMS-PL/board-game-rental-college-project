import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { GameDetail } from '../../../core/models/game.model';
import { GamesService } from '../games.service';

@Component({
  selector: 'app-game-detail',
  standalone: false,
  templateUrl: './game-detail.component.html',
  styleUrl: './game-detail.component.scss'
})
export class GameDetailComponent implements OnInit {
  protected game: GameDetail | null = null;
  protected isLoading = false;
  protected totalCopiesInput = 0;
  protected updateCopiesLoading = false;
  protected updateCopiesSuccess = false;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly gamesService: GamesService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) this.loadGame(id);
  }

  private loadGame(id: string): void {
    this.isLoading = true;
    this.gamesService.getGame(id).subscribe({
      next: game => {
        this.game = game;
        this.totalCopiesInput = game.totalCopies;
        this.isLoading = false;
      },
      error: () => { this.isLoading = false; }
    });
  }

  protected updateCopies(): void {
    if (!this.game) return;
    this.updateCopiesLoading = true;
    this.gamesService.updateCopies(this.game.id, this.totalCopiesInput).subscribe({
      next: () => {
        this.updateCopiesSuccess = true;
        this.updateCopiesLoading = false;
        this.loadGame(this.game!.id);
        setTimeout(() => { this.updateCopiesSuccess = false; }, 3000);
      },
      error: () => { this.updateCopiesLoading = false; }
    });
  }

  protected get availableCount(): number {
    return this.game?.copies.filter(c => c.isAvailable).length ?? 0;
  }

  protected conditionLabel(condition: string): string {
    const labels: Record<string, string> = {
      NEW: 'Nowy', GOOD: 'Dobry', WORN: 'Zużyty', DAMAGED: 'Uszkodzony'
    };
    return labels[condition] ?? condition;
  }
}

