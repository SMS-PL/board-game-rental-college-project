import { Component, OnInit } from '@angular/core';
import { Rental, RentalStatus } from '../../../core/models/rental.model';
import { RentalsService } from '../rentals.service';
import { BadgeVariant } from '../../../shared/components/badge/badge.component';

@Component({
  selector: 'app-rental-list',
  standalone: false,
  templateUrl: './rental-list.component.html',
  styleUrl: './rental-list.component.scss'
})
export class RentalListComponent implements OnInit {
  protected rentals: Rental[] = [];
  protected isLoading = false;
  protected filterStatus: RentalStatus | '' = '';

  protected readonly RentalStatus = RentalStatus;
  protected readonly rentalStatuses = Object.values(RentalStatus);

  constructor(private readonly rentalsService: RentalsService) {}

  ngOnInit(): void {
    this.loadRentals();
  }

  protected loadRentals(): void {
    this.isLoading = true;
    this.rentalsService.getRentals(this.filterStatus || undefined).subscribe({
      next: rentals => {
        this.rentals = rentals;
        this.isLoading = false;
      },
      error: () => { this.isLoading = false; }
    });
  }

  protected statusVariant(status: RentalStatus): BadgeVariant {
    const map: Record<RentalStatus, BadgeVariant> = {
      [RentalStatus.ACTIVE]: 'success',
      [RentalStatus.RETURNED]: 'ghost',
      [RentalStatus.OVERDUE]: 'error'
    };
    return map[status];
  }

  protected statusLabel(status: RentalStatus): string {
    const map: Record<RentalStatus, string> = {
      [RentalStatus.ACTIVE]: 'Aktywne',
      [RentalStatus.RETURNED]: 'Zwrócone',
      [RentalStatus.OVERDUE]: 'Po terminie'
    };
    return map[status];
  }
}



