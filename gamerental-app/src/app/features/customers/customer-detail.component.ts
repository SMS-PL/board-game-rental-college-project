import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { NavbarComponent } from '../../shared/navbar/navbar.component';
import { CustomerService } from '../../core/services/customer.service';
import { CustomerDetail } from '../../core/models/customer.model';
import { Rental } from '../../core/models/rental.model';

@Component({
  selector: 'app-customer-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, NavbarComponent],
  templateUrl: './customer-detail.component.html'
})
export class CustomerDetailComponent implements OnInit {
  customer: CustomerDetail | null = null;
  rentals: Rental[] = [];
  loadingRentals = true;

  constructor(private route: ActivatedRoute, private customerService: CustomerService) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id')!; // FIXED: string UUID
    // ClientDetailDto includes rentals — one call instead of two
    this.customerService.getCustomer(id).subscribe(c => {
      this.customer = c;
      this.rentals = c.rentals;
      this.loadingRentals = false;
    });
  }

  statusLabel(s: string): string {
    return ({ ACTIVE: 'Aktywne', RETURNED: 'Zwrócone', OVERDUE: 'Zaległe' } as any)[s] ?? s;
  }
}
