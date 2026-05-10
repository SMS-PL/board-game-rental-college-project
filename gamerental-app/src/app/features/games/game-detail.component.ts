import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { NavbarComponent } from '../../shared/navbar/navbar.component';
import { GameService } from '../../core/services/game.service';
import { RentalService } from '../../core/services/rental.service';
import { GameDetail, GameCopy, CONDITION_LABELS, GAME_TAG_LABELS, CopyCondition } from '../../core/models/game.model';
import { Rental, ReturnRequest } from '../../core/models/rental.model';

@Component({
  selector: 'app-game-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, NavbarComponent],
  templateUrl: './game-detail.component.html'
})
export class GameDetailComponent implements OnInit {
  game: GameDetail | null = null;
  rentals: Rental[] = [];
  loadingRentals = true;
  expandedCopy: string | null = null;
  successMsg = '';
  errorMsg = '';

  // Copies count editor
  newTotalCopies = 0;
  editingCopies = false;

  // Return modal
  returningRentalId: string | null = null;
  returnCondition: CopyCondition = 'GOOD';

  readonly conditionLabels = CONDITION_LABELS;
  readonly tagLabels = GAME_TAG_LABELS;
  readonly conditions: CopyCondition[] = ['NEW', 'GOOD', 'WORN', 'DAMAGED'];

  constructor(
    private route: ActivatedRoute,
    private gameService: GameService,
    private rentalService: RentalService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.loadGame(id);
    this.loadRentals();
  }

  loadGame(id: string): void {
    this.gameService.getGame(id).subscribe(g => {
      this.game = g;
      this.newTotalCopies = g.totalCopies;
    });
  }

  loadRentals(): void {
    this.loadingRentals = true;
    if (!this.game) {
      // Wait for game to load first, then use gameId from URL
      const id = this.route.snapshot.paramMap.get('id')!;
      this.rentalService.getRentals(undefined, undefined, id).subscribe({
        next: r => { this.rentals = r; this.loadingRentals = false; },
        error: () => { this.loadingRentals = false; }
      });
    } else {
      this.rentalService.getRentals(undefined, undefined, this.game.id).subscribe({
        next: r => { this.rentals = r; this.loadingRentals = false; },
        error: () => { this.loadingRentals = false; }
      });
    }
  }

  getRentalsForCopy(copyId: string): Rental[] {
    return this.rentals
      .filter(r => r.gameCopyId === copyId)
      .sort((a, b) => new Date(b.rentedFrom).getTime() - new Date(a.rentedFrom).getTime());
  }

  getActiveRental(copyId: string): Rental | null {
    return this.getRentalsForCopy(copyId).find(r => r.status === 'ACTIVE' || r.status === 'OVERDUE') ?? null;
  }

  availableCount(): number {
    return this.game?.copies.filter(c => c.isAvailable).length ?? 0;
  }

  toggleCopy(id: string): void {
    this.expandedCopy = this.expandedCopy === id ? null : id;
  }

  conditionLabel(c: string): string {
    return (this.conditionLabels as any)[c] ?? c;
  }

  tagLabel(tag: string): string {
    return (this.tagLabels as any)[tag] ?? tag;
  }

  statusLabel(s: string): string {
    return ({ ACTIVE: 'Aktywne', RETURNED: 'Zwrócone', OVERDUE: 'Zaległe' } as any)[s] ?? s;
  }

  // Copies management
  openSetCopies(): void {
    this.editingCopies = true;
    this.newTotalCopies = this.game?.totalCopies ?? 0;
  }

  saveCopies(): void {
    if (!this.game) return;
    this.gameService.updateCopies(this.game.id, this.newTotalCopies).subscribe({
      next: updated => {
        this.game = updated;
        this.editingCopies = false;
        this.successMsg = 'Liczba egzemplarzy zaktualizowana.';
        setTimeout(() => this.successMsg = '', 3000);
      },
      error: () => this.errorMsg = 'Błąd podczas aktualizacji egzemplarzy.'
    });
  }

  cancelEditCopies(): void { this.editingCopies = false; }

  // Return rental
  openReturnModal(rentalId: string): void {
    this.returningRentalId = rentalId;
    this.returnCondition = 'GOOD';
    (document.getElementById('returnModal') as HTMLDialogElement).showModal();
  }

  closeReturnModal(): void {
    (document.getElementById('returnModal') as HTMLDialogElement).close();
    this.returningRentalId = null;
  }

  confirmReturn(): void {
    if (!this.returningRentalId || !this.game) return;
    const req: ReturnRequest = { conditionOnReturn: this.returnCondition };
    this.rentalService.returnRental(this.returningRentalId, req).subscribe({
      next: (updated) => {
        this.rentals = this.rentals.map(r => r.id === updated.id ? updated : r);
        // Refresh game to update copy availability
        this.loadGame(this.game!.id);
        this.closeReturnModal();
        this.successMsg = 'Wypożyczenie zwrócone.';
        setTimeout(() => this.successMsg = '', 3000);
      },
      error: () => this.errorMsg = 'Błąd podczas zwrotu.'
    });
  }
}
