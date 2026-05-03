import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Client } from '../../../core/models/client.model';
import { ClientsService } from '../clients.service';

@Component({
  selector: 'app-client-list',
  standalone: false,
  templateUrl: './client-list.component.html',
  styleUrl: './client-list.component.scss'
})
export class ClientListComponent implements OnInit {
  protected clients: Client[] = [];
  protected isLoading = false;
  protected searchQuery = '';
  protected sortField = 'lastName';
  protected deleteModalOpen = false;
  protected selectedClientId: string | null = null;

  constructor(
    private readonly clientsService: ClientsService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.loadClients();
  }

  protected loadClients(): void {
    this.isLoading = true;
    this.clientsService.getClients(this.searchQuery || undefined, this.sortField).subscribe({
      next: clients => {
        this.clients = clients;
        this.isLoading = false;
      },
      error: () => { this.isLoading = false; }
    });
  }

  protected openDeleteModal(id: string): void {
    this.selectedClientId = id;
    this.deleteModalOpen = true;
  }

  protected confirmDelete(): void {
    if (!this.selectedClientId) return;
    this.clientsService.deleteClient(this.selectedClientId).subscribe({
      next: () => {
        this.deleteModalOpen = false;
        this.selectedClientId = null;
        this.loadClients();
      }
    });
  }

  protected cancelDelete(): void {
    this.deleteModalOpen = false;
    this.selectedClientId = null;
  }

  protected fullName(client: Client): string {
    return `${client.firstName} ${client.lastName}`;
  }
}

