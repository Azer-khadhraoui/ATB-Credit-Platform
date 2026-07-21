import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CreditFileService } from '../credit-file.service';
import {
  CreditFile,
  DecisionFactor,
  aiDecisionLabel,
  creditHistoryLabel,
  decisionFactorLabel,
  riskLevelLabel,
  statusLabel
} from '../credit-file.model';

/** A factor plus the bar width used to draw it, relative to the strongest one. */
interface WeightedFactor extends DecisionFactor {
  label: string;
  width: number;
}

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
  readonly analyzing = signal(false);
  readonly analyzeError = signal<string | null>(null);

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

  aiDecisionLabel(value: CreditFile['aiDecision']): string | null {
    return aiDecisionLabel(value);
  }

  creditHistoryLabel(value: CreditFile['creditHistory']): string | null {
    return creditHistoryLabel(value);
  }

  /**
   * Scales each bar against the strongest factor, so the dominant driver fills the
   * width and the rest read as fractions of it. Absolute log-odds mean nothing to an
   * agent; relative weight does.
   */
  weightedFactors(cf: CreditFile): WeightedFactor[] {
    const factors = cf.decisionFactors ?? [];
    if (factors.length === 0) {
      return [];
    }
    const strongest = Math.max(...factors.map((f) => Math.abs(f.impact)));
    return factors.map((f) => ({
      ...f,
      label: decisionFactorLabel(f.feature),
      width: strongest === 0 ? 0 : Math.round((Math.abs(f.impact) / strongest) * 100)
    }));
  }

  hasAnalysis(cf: CreditFile): boolean {
    return cf.riskScore != null || cf.riskLevel != null || cf.aiDecision != null;
  }

  runAnalysis(): void {
    this.analyzing.set(true);
    this.analyzeError.set(null);

    this.creditFileService.analyze(this.creditFileId).subscribe({
      next: (updated) => {
        this.creditFile.set(updated);
        this.analyzing.set(false);
      },
      error: (err) => {
        this.analyzing.set(false);
        this.analyzeError.set(err?.error?.message ?? "L'analyse a échoué.");
      }
    });
  }
}
