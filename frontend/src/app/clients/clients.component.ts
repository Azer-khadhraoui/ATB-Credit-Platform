import { CommonModule } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ClientService } from './client.service';
import { Client, genderLabel, employmentTypeLabel } from './client.model';

@Component({
  selector: 'app-clients',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './clients.component.html',
  styleUrl: './clients.component.scss'
})
export class ClientsComponent {
  private readonly clientService = inject(ClientService);
  private readonly router = inject(Router);

  readonly clients = signal<Client[]>([]);
  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly searchTerm = signal('');

  readonly filteredClients = computed(() => {
    const term = this.searchTerm().trim().toLowerCase();
    if (!term) {
      return this.clients();
    }
    return this.clients().filter((client) => {
      const haystack = `${client.firstName} ${client.lastName} ${client.cin} ${client.email} ${client.city}`.toLowerCase();
      return haystack.includes(term);
    });
  });

  constructor() {
    this.loadClients();
  }

  loadClients(): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    this.clientService.getAll().subscribe({
      next: (clients) => {
        this.clients.set(clients);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(err?.error?.message ?? 'Impossible de charger les clients.');
      }
    });
  }

  editClient(client: Client): void {
    this.router.navigate(['/app/clients', client.id, 'edit']);
  }

  deleteClient(client: Client): void {
    if (!confirm(`Supprimer le client ${client.firstName} ${client.lastName} ?`)) {
      return;
    }

    this.clientService.delete(client.id).subscribe({
      next: () => this.clients.update((list) => list.filter((c) => c.id !== client.id)),
      error: (err) => this.errorMessage.set(err?.error?.message ?? 'La suppression a échoué.')
    });
  }

  initials(client: Client): string {
    return (client.firstName.charAt(0) + client.lastName.charAt(0)).toUpperCase();
  }

  genderLabel(value: Client['gender']): string {
    return genderLabel(value);
  }

  employmentTypeLabel(value: Client['employmentType']): string {
    return employmentTypeLabel(value);
  }
}
