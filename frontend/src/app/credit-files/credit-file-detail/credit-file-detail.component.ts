import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CreditFileService } from '../credit-file.service';
import { CreditFile, riskLevelLabel, statusLabel } from '../credit-file.model';

@Component({
  selector: 'app-credit-file-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './credit-file-detail.component.html',
  styleUrl: './credit-file-detail.component.scss'
})
export class CreditFileDetailComponent {
  private readonly creditFileService = inject(CreditFileService);
  private readonly route = inject(ActivatedRoute);

  private readonly creditFileId = this.route.snapshot.paramMap.get('id')!;

  readonly creditFile = signal<CreditFile | null>(null);
  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);

  constructor() {
    this.creditFileService.getById(this.creditFileId).subscribe({
      next: (creditFile) => {
        this.creditFile.set(creditFile);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(err?.error?.message ?? 'Impossible de charger le dossier.');
      }
    });
  }

  statusLabel(value: CreditFile['status']): string {
    return statusLabel(value);
  }

  riskLevelLabel(value: CreditFile['riskLevel']): string | null {
    return riskLevelLabel(value);
  }

  hasAnalysis(cf: CreditFile): boolean {
    return cf.riskScore != null || cf.riskLevel != null || cf.aiDecision != null;
  }
}
