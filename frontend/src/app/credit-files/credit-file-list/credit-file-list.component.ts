import { CommonModule } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CreditFileService } from '../credit-file.service';
import { CreditFile, statusLabel } from '../credit-file.model';

@Component({
  selector: 'app-credit-file-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './credit-file-list.component.html',
  styleUrl: './credit-file-list.component.scss'
})
export class CreditFileListComponent {
  private readonly creditFileService = inject(CreditFileService);
  private readonly router = inject(Router);

  readonly creditFiles = signal<CreditFile[]>([]);
  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly searchTerm = signal('');

  readonly filteredCreditFiles = computed(() => {
    const term = this.searchTerm().trim().toLowerCase();
    if (!term) {
      return this.creditFiles();
    }
    return this.creditFiles().filter((cf) => {
      const haystack = `${cf.clientFullName ?? ''} ${cf.clientCin ?? ''} ${cf.creditType} ${cf.loanPurpose}`.toLowerCase();
      return haystack.includes(term);
    });
  });

  constructor() {
    this.loadCreditFiles();
  }

  loadCreditFiles(): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    this.creditFileService.getAll().subscribe({
      next: (creditFiles) => {
        this.creditFiles.set(creditFiles);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(err?.error?.message ?? 'Impossible de charger les dossiers de crédit.');
      }
    });
  }

  editCreditFile(creditFile: CreditFile): void {
    this.router.navigate(['/app/credit-files', creditFile.id, 'edit']);
  }

  deleteCreditFile(creditFile: CreditFile): void {
    if (!confirm(`Supprimer le dossier de ${creditFile.clientFullName ?? 'ce client'} ?`)) {
      return;
    }

    this.creditFileService.delete(creditFile.id).subscribe({
      next: () => this.creditFiles.update((list) => list.filter((c) => c.id !== creditFile.id)),
      error: (err) => this.errorMessage.set(err?.error?.message ?? 'La suppression a échoué.')
    });
  }

  statusLabel(value: CreditFile['status']): string {
    return statusLabel(value);
  }
}
