import { CommonModule } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DashboardService, DashboardStats } from './dashboard.service';
import { AuthService } from '../core/auth/auth.service';
import { statusLabel, riskLevelLabel, CreditStatus, RiskLevel } from '../credit-files/credit-file.model';

interface BarItem {
  key: string;
  label: string;
  count: number;
  percent: number;
  colorClass: string;
  width: number;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent {
  private readonly dashboardService = inject(DashboardService);
  private readonly authService = inject(AuthService);

  readonly stats = signal<DashboardStats | null>(null);
  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);

  readonly firstName = computed(() => this.authService.currentUser()?.fullName?.trim().split(/\s+/)[0] ?? '');

  private readonly STATUS_COLORS: Record<string, string> = {
    DRAFT: 'bar--slate',
    IN_REVIEW: 'bar--amber',
    ANALYZED: 'bar--blue',
    APPROVED: 'bar--green',
    REJECTED: 'bar--red'
  };

  private readonly RISK_COLORS: Record<string, string> = {
    LOW: 'bar--green',
    MEDIUM: 'bar--amber',
    HIGH: 'bar--red'
  };

  readonly statusBars = computed<BarItem[]>(() => this.buildBars(this.stats()?.statusDistribution, this.STATUS_COLORS, (k) => statusLabel(k as CreditStatus)));
  readonly riskBars = computed<BarItem[]>(() => this.buildBars(this.stats()?.riskLevelDistribution, this.RISK_COLORS, (k) => riskLevelLabel(k as RiskLevel) ?? k));

  readonly approvalRate = computed(() => {
    const s = this.stats();
    if (!s || s.totalCreditFiles === 0) {
      return 0;
    }
    const approved = s.statusDistribution['APPROVED'] ?? 0;
    return Math.round((approved / s.totalCreditFiles) * 100);
  });

  constructor() {
    this.dashboardService.getStats().subscribe({
      next: (stats) => {
        this.stats.set(stats);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(err?.error?.message ?? 'Impossible de charger les statistiques.');
      }
    });
  }

  private buildBars(
    distribution: Record<string, number> | undefined,
    colors: Record<string, string>,
    labelFn: (key: string) => string
  ): BarItem[] {
    if (!distribution) {
      return [];
    }
    const total = Object.values(distribution).reduce((sum, n) => sum + n, 0);
    const max = Math.max(1, ...Object.values(distribution));
    return Object.entries(distribution).map(([key, count]) => ({
      key,
      label: labelFn(key),
      count,
      percent: total === 0 ? 0 : Math.round((count / total) * 100),
      colorClass: colors[key] ?? 'bar--slate',
      // Bar width is relative to the largest bar so small values stay visible.
      width: Math.round((count / max) * 100)
    }));
  }

  riskLevelLabel(value: string | null): string | null {
    return value ? riskLevelLabel(value as RiskLevel) : null;
  }

  statusLabel(value: string | null): string | null {
    return value ? statusLabel(value as CreditStatus) : null;
  }
}
