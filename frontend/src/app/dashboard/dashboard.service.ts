import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface RecentCreditFile {
  id: string;
  clientFullName: string;
  creditType: string;
  loanAmount: number | null;
  status: string | null;
  riskLevel: string | null;
  createdAt: string | null;
}

export interface DashboardStats {
  totalClients: number;
  totalCreditFiles: number;
  totalUsers: number;
  analyzedCount: number;
  totalLoanAmount: number;
  averageRiskScore: number;
  pendingCount: number;
  statusDistribution: Record<string, number>;
  riskLevelDistribution: Record<string, number>;
  creditTypeDistribution: Record<string, number>;
  recentCreditFiles: RecentCreditFile[];
}

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/dashboard`;

  getStats(): Observable<DashboardStats> {
    return this.http.get<DashboardStats>(`${this.baseUrl}/stats`);
  }
}
