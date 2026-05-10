import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { NavbarComponent } from '../../shared/navbar/navbar.component';
import { GameService } from '../../core/services/game.service';
import { Game, GameRequest, UpdateGameRequest, GameTag, ALL_GAME_TAGS, GAME_TAG_LABELS } from '../../core/models/game.model';

@Component({
  selector: 'app-games-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, NavbarComponent],
  templateUrl: './games-list.component.html'
})
export class GamesListComponent implements OnInit {
  games: Game[] = [];
  loading = true;
  search = '';
  successMsg = '';
  errorMsg = '';
  editingId: string | null = null;

  readonly allTags = ALL_GAME_TAGS;
  readonly tagLabels = GAME_TAG_LABELS;

  form: GameRequest = { title: '', description: '', tags: [], totalCopies: 1 };
  editForm: UpdateGameRequest = { title: '', description: '', tags: [] };

  constructor(private gameService: GameService) {}

  ngOnInit(): void { this.loadGames(); }

  loadGames(): void {
    this.loading = true;
    this.gameService.getGames().subscribe({
      next: res => { this.games = res; this.loading = false; },
      error: () => { this.games = []; this.loading = false; }
    });
  }

  get filtered(): Game[] {
    if (!this.search) return this.games;
    const q = this.search.toLowerCase();
    return this.games.filter(g => g.title.toLowerCase().includes(q));
  }

  tagLabel(tag: GameTag): string {
    return this.tagLabels[tag];
  }

  isTagSelected(tags: GameTag[], tag: GameTag): boolean {
    return tags.includes(tag);
  }

  toggleTag(tags: GameTag[], tag: GameTag): void {
    const idx = tags.indexOf(tag);
    if (idx >= 0) tags.splice(idx, 1);
    else tags.push(tag);
  }

  toggleFormTag(tag: GameTag): void { this.toggleTag(this.form.tags, tag); }
  toggleEditTag(tag: GameTag): void { this.toggleTag(this.editForm.tags, tag); }

  openAddModal(): void {
    this.form = { title: '', description: '', tags: [], totalCopies: 1 };
    this.errorMsg = '';
    (document.getElementById('addModal') as HTMLDialogElement).showModal();
  }

  closeModal(id: string): void {
    (document.getElementById(id) as HTMLDialogElement).close();
  }

  createGame(): void {
    if (this.form.tags.length === 0) {
      this.errorMsg = 'Wybierz co najmniej jeden tag gry.';
      return;
    }
    this.gameService.createGame(this.form).subscribe({
      next: () => {
        // Reload full list to get correct Game shape (with availableCopies)
        this.loadGames();
        this.successMsg = 'Gra dodana pomyślnie.';
        this.closeModal('addModal');
        this.form = { title: '', description: '', tags: [], totalCopies: 1 };
        setTimeout(() => this.successMsg = '', 3000);
      },
      error: () => this.errorMsg = 'Błąd podczas dodawania gry.'
    });
  }

  openEdit(g: Game): void {
    this.editingId = g.id;
    this.editForm = { title: g.title, description: g.description, tags: [...g.tags] };
    this.errorMsg = '';
    (document.getElementById('editModal') as HTMLDialogElement).showModal();
  }

  updateGame(): void {
    if (!this.editingId) return;
    if (this.editForm.tags.length === 0) {
      this.errorMsg = 'Wybierz co najmniej jeden tag gry.';
      return;
    }
    this.gameService.updateGame(this.editingId, this.editForm).subscribe({
      next: (updated) => {
        // GameDetail → update matching Game in list (shared fields)
        this.games = this.games.map(g => g.id === updated.id
          ? { ...g, title: updated.title, description: updated.description, tags: updated.tags }
          : g
        );
        this.successMsg = 'Gra zaktualizowana.';
        this.closeModal('editModal');
        setTimeout(() => this.successMsg = '', 3000);
      },
      error: () => this.errorMsg = 'Błąd podczas aktualizacji gry.'
    });
  }

  deleteGame(id: string): void {
    if (!confirm('Czy na pewno chcesz usunąć tę grę?')) return;
    this.gameService.deleteGame(id).subscribe({
      next: () => {
        this.games = this.games.filter(g => g.id !== id);
        this.successMsg = 'Gra usunięta.';
        setTimeout(() => this.successMsg = '', 3000);
      },
      error: () => this.errorMsg = 'Nie można usunąć gry (być może ma aktywne wypożyczenia).'
    });
  }
}
