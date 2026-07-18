import { CommonModule } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuditLogService } from './audit-log.service';
import { AuditLog, actionLabel, entityLabel } from './audit-log.model';

@Component({
  selector: 'app-audit',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './audit.component.html',
  styleUrl: './audit.component.scss'
})
export class AuditComponent {
  private readonly auditLogService = inject(AuditLogService);

  readonly logs = signal<AuditLog[]>([]);
  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly searchTerm = signal('');

  readonly filteredLogs = computed(() => {
    const term = this.searchTerm().trim().toLowerCase();
    if (!term) {
      return this.logs();
    }
    return this.logs().filter((log) => {
      const haystack = `${log.userFullName ?? ''} ${log.userId} ${log.description} ${entityLabel(log.entity)}`.toLowerCase();
      return haystack.includes(term);
    });
  });

  constructor() {
    this.loadLogs();
  }

  loadLogs(): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    this.auditLogService.getAll().subscribe({
      next: (logs) => {
        this.logs.set(logs);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(err?.error?.message ?? "Impossible de charger le journal d'audit.");
      }
    });
  }

  actionLabel(value: AuditLog['action']): string {
    return actionLabel(value);
  }

  entityLabel(value: string): string {
    return entityLabel(value);
  }
}
