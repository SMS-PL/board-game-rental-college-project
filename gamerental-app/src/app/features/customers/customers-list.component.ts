import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { NavbarComponent } from '../../shared/navbar/navbar.component';
import { CustomerService } from '../../core/services/customer.service';
import { Customer, CustomerRequest } from '../../core/models/customer.model';

@Component({
  selector: 'app-customers-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, NavbarComponent],
  templateUrl: './customers-list.component.html'
})
export class CustomersListComponent implements OnInit {
  customers: Customer[] = [];
  loading = true;
  search = '';
  successMsg = '';
  errorMsg = '';
  editingId: string | null = null; // FIXED: string UUID

  form: CustomerRequest = { firstName: '', lastName: '', email: '', phone: '' };
  editForm: CustomerRequest = { firstName: '', lastName: '', email: '', phone: '' };

  get filtered(): Customer[] {
    if (!this.search) return this.customers;
    const q = this.search.toLowerCase();
    return this.customers.filter(c =>
      `${c.firstName} ${c.lastName}`.toLowerCase().includes(q) ||
      (c.email ?? '').toLowerCase().includes(q)  // FIXED: optional email
    );
  }

  constructor(private customerService: CustomerService) {}

  ngOnInit(): void { this.loadCustomers(); }

  loadCustomers(): void {
    this.loading = true;
    this.customerService.getCustomers().subscribe({
      next: cs => { this.customers = cs; this.loading = false; },
      error: () => { this.customers = []; this.loading = false; }
    });
  }

  openAddModal(): void {
    this.form = { firstName: '', lastName: '', email: '', phone: '' };
    this.errorMsg = '';
    (document.getElementById('addCustomerModal') as HTMLDialogElement).showModal();
  }

  closeModal(id: string): void {
    (document.getElementById(id) as HTMLDialogElement).close();
  }

  createCustomer(): void {
    this.errorMsg = '';
    this.customerService.createCustomer(this.form).subscribe({
      next: (created) => {
        this.customers = [created, ...this.customers];
        this.successMsg = 'Klient dodany.';
        this.closeModal('addCustomerModal');
        this.form = { firstName: '', lastName: '', email: '', phone: '' };
        setTimeout(() => this.successMsg = '', 3000);
      },
      error: (e) => {
        this.errorMsg = e.error?.detail ?? e.error?.message ?? 'Błąd podczas dodawania klienta.';
      }
    });
  }

  openEdit(c: Customer): void {
    this.editingId = c.id; // FIXED: string
    this.editForm = { firstName: c.firstName, lastName: c.lastName, email: c.email ?? '', phone: c.phone };
    this.errorMsg = '';
    (document.getElementById('editCustomerModal') as HTMLDialogElement).showModal();
  }

  updateCustomer(): void {
    if (!this.editingId) return;
    this.customerService.updateCustomer(this.editingId, this.editForm).subscribe({
      next: (updated) => {
        this.customers = this.customers.map(c => c.id === updated.id ? { ...c, ...updated } : c);
        this.successMsg = 'Klient zaktualizowany.';
        this.closeModal('editCustomerModal');
        setTimeout(() => this.successMsg = '', 3000);
      },
      error: (e) => {
        this.errorMsg = e.error?.detail ?? e.error?.message ?? 'Błąd podczas aktualizacji.';
      }
    });
  }

  deleteCustomer(id: string): void { // FIXED: string
    if (!confirm('Usunąć klienta?')) return;
    this.customerService.deleteCustomer(id).subscribe({
      next: () => {
        this.customers = this.customers.filter(c => c.id !== id);
        this.successMsg = 'Klient usunięty.';
        setTimeout(() => this.successMsg = '', 3000);
      },
      error: () => this.errorMsg = 'Nie można usunąć klienta.'
    });
  }
}
