import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { NavbarComponent } from '../../shared/navbar/navbar.component';
import { RentalService } from '../../core/services/rental.service';
import { GameService } from '../../core/services/game.service';
import { CustomerService } from '../../core/services/customer.service';
import { RentalRequest } from '../../core/models/rental.model';
import { Game, GameCopy, CONDITION_LABELS } from '../../core/models/game.model';
import { Customer } from '../../core/models/customer.model';

@Component({
  selector: 'app-create-rental',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, NavbarComponent],
  templateUrl: './create-rental.component.html'
})
export class CreateRentalComponent implements OnInit {
  games: Game[] = [];
  availableCopies: GameCopy[] = [];
  customers: Customer[] = [];

  selectedGameId = '';
  form: RentalRequest = { gameCopyId: '', clientId: '', rentedFrom: '', dueTo: '', notes: '' };

  today = new Date().toISOString().split('T')[0];
  loading = false;
  loadingCopies = false;
  errorMsg = '';

  readonly conditionLabels = CONDITION_LABELS;

  constructor(
    private rentalService: RentalService,
    private gameService: GameService,
    private customerService: CustomerService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.form.rentedFrom = this.today;
    this.gameService.getGames().subscribe(g => this.games = g);
    this.customerService.getCustomers().subscribe(c => this.customers = c);
  }

  onGameChange(): void {
    if (!this.selectedGameId) { this.availableCopies = []; return; }
    this.loadingCopies = true;
    this.form.gameCopyId = '';
    this.gameService.getAvailableCopies(this.selectedGameId).subscribe({
      next: copies => { this.availableCopies = copies; this.loadingCopies = false; },
      error: () => { this.availableCopies = []; this.loadingCopies = false; }
    });
  }

  conditionLabel(c: string): string {
    return (this.conditionLabels as any)[c] ?? c;
  }

  isFormValid(): boolean {
    return !!this.form.gameCopyId && !!this.form.clientId && !!this.form.rentedFrom && !!this.form.dueTo;
  }

  onSubmit(): void {
    this.loading = true;
    this.errorMsg = '';
    const req: RentalRequest = {
      gameCopyId: this.form.gameCopyId,
      clientId: this.form.clientId,
      rentedFrom: this.form.rentedFrom,
      dueTo: this.form.dueTo,
      notes: this.form.notes || undefined
    };
    this.rentalService.createRental(req).subscribe({
      next: () => this.router.navigate(['/rentals']),
      error: (e) => {
        this.errorMsg = e.error?.detail ?? e.error?.message ?? 'Błąd podczas tworzenia wypożyczenia.';
        this.loading = false;
      }
    });
  }
}
