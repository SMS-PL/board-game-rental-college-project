import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { NavbarComponent } from '../../shared/navbar/navbar.component';
import { RentalService } from '../../core/services/rental.service';
import { Rental, ReturnRequest, CopyCondition } from '../../core/models/rental.model';

@Component({
  selector: 'app-rentals-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, NavbarComponent],
  templateUrl: './rentals-list.component.html'
})
export class RentalsListComponent implements OnInit {
  rentals: Rental[] = [];
  statusFilter = '';
  successMsg = '';
  errorMsg = '';
  loading = true;

  // Return modal
  returningRentalId: string | null = null;
  returnCondition: CopyCondition = 'GOOD';
  readonly conditions: { value: CopyCondition; label: string }[] = [
    { value: 'NEW', label: 'Nowy' },
    { value: 'GOOD', label: 'Dobry' },
    { value: 'WORN', label: 'Zużyty' },
    { value: 'DAMAGED', label: 'Uszkodzony' },
  ];

  constructor(private rentalService: RentalService) {}

  ngOnInit(): void { this.loadRentals(); }

  loadRentals(): void {
    this.rentalService.getRentals(this.statusFilter || undefined).subscribe({
      next: r => { this.rentals = r; this.loading = false; },
      error: () => { this.rentals = []; this.loading = false; }
    });
  }

  setFilter(s: string): void { this.statusFilter = s; this.loading = true; this.loadRentals(); }

  openReturnModal(id: string): void {
    this.returningRentalId = id;
    this.returnCondition = 'GOOD';
    (document.getElementById('returnModal') as HTMLDialogElement).showModal();
  }

  closeReturnModal(): void {
    (document.getElementById('returnModal') as HTMLDialogElement).close();
    this.returningRentalId = null;
  }

  confirmReturn(): void {
    if (!this.returningRentalId) return;
    const req: ReturnRequest = { conditionOnReturn: this.returnCondition };
    this.rentalService.returnRental(this.returningRentalId, req).subscribe({
      next: (updated) => {
        this.rentals = this.rentals.map(r => r.id === updated.id ? updated : r);
        this.closeReturnModal();
        this.successMsg = 'Wypożyczenie zwrócone.';
        setTimeout(() => this.successMsg = '', 3000);
      },
      error: () => {
        this.errorMsg = 'Błąd podczas zwrotu.';
        this.closeReturnModal();
      }
    });
  }

  statusLabel(s: string): string {
    return ({ ACTIVE: 'Aktywne', RETURNED: 'Zwrócone', OVERDUE: 'Zaległe' } as any)[s] ?? s;
  }
}
