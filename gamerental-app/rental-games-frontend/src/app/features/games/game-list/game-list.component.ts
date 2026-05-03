import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Game, GameTag } from '../../../core/models/game.model';
import { GamesService } from '../games.service';

@Component({
  selector: 'app-game-list',
  standalone: false,
  templateUrl: './game-list.component.html',
  styleUrl: './game-list.component.scss'
})
export class GameListComponent implements OnInit {
  protected games: Game[] = [];
  protected isLoading = false;
  protected deleteModalOpen = false;
  protected selectedGameId: string | null = null;
  protected selectedTag: GameTag | '' = '';
  protected sortField = 'title';

  protected readonly gameTags = Object.values(GameTag);

  constructor(
    private readonly gamesService: GamesService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.loadGames();
  }

  protected loadGames(): void {
    this.isLoading = true;
    this.gamesService.getGames(this.selectedTag || undefined, this.sortField).subscribe({
      next: games => {
        this.games = games;
        this.isLoading = false;
      },
      error: () => { this.isLoading = false; }
    });
  }

  protected onTagChange(tag: GameTag | ''): void {
    this.selectedTag = tag;
    this.loadGames();
  }

  protected onSortChange(field: string): void {
    this.sortField = field;
    this.loadGames();
  }

  protected openDeleteModal(id: string): void {
    this.selectedGameId = id;
    this.deleteModalOpen = true;
  }

  protected confirmDelete(): void {
    if (!this.selectedGameId) return;
    this.gamesService.deleteGame(this.selectedGameId).subscribe({
      next: () => {
        this.deleteModalOpen = false;
        this.selectedGameId = null;
        this.loadGames();
      }
    });
  }

  protected cancelDelete(): void {
    this.deleteModalOpen = false;
    this.selectedGameId = null;
  }

  protected navigateToDetail(id: string): void {
    this.router.navigate(['/games', id]);
  }

  protected navigateToEdit(id: string, event: Event): void {
    event.stopPropagation();
    this.router.navigate(['/games', id, 'edit']);
  }
}

