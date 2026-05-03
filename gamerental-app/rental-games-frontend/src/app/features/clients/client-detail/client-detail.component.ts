import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Client } from '../../../core/models/client.model';
import { Rental, RentalStatus } from '../../../core/models/rental.model';
import { ClientsService } from '../clients.service';
import { BadgeVariant } from '../../../shared/components/badge/badge.component';

@Component({
  selector: 'app-client-detail',
  standalone: false,
  templateUrl: './client-detail.component.html',
  styleUrl: './client-detail.component.scss'
})
export class ClientDetailComponent implements OnInit {
  protected client: Client | null = null;
  protected rentals: Rental[] = [];
  protected isLoading = false;

  protected readonly RentalStatus = RentalStatus;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly clientsService: ClientsService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) this.loadClient(id);
  }

  private loadClient(id: string): void {
    this.isLoading = true;
    this.clientsService.getClient(id).subscribe({
      next: client => { this.client = client; }
    });
    this.clientsService.getClientRentals(id).subscribe({
      next: rentals => {
        this.rentals = rentals;
        this.isLoading = false;
      },
      error: () => { this.isLoading = false; }
    });
  }

  protected get activeRentals(): Rental[] {
    return this.rentals.filter(r => r.status === RentalStatus.ACTIVE || r.status === RentalStatus.OVERDUE);
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



