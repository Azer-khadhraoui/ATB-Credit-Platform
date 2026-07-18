import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { CreditFile } from './credit-file.model';

export interface CreditFileCreatePayload {
  clientId: string;
  creditType: string;
  loanAmount: number;
  loanDurationMonths: number;
  loanPurpose: string;
  interestRate?: number | null;
  downPayment?: number | null;
  existingCredits?: number | null;
  monthlyInstallment?: number | null;
  creditHistory?: string | null;
  guaranteeType?: string | null;
  status?: string | null;
  agentDecision?: string | null;
  comments?: string | null;
}

export type CreditFileUpdatePayload = Omit<CreditFileCreatePayload, 'clientId'>;

@Injectable({ providedIn: 'root' })
export class CreditFileService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/credit-files`;

  getAll(): Observable<CreditFile[]> {
    return this.http.get<CreditFile[]>(this.baseUrl);
  }

  getById(id: string): Observable<CreditFile> {
    return this.http.get<CreditFile>(`${this.baseUrl}/${id}`);
  }

  create(payload: CreditFileCreatePayload): Observable<CreditFile> {
    return this.http.post<CreditFile>(this.baseUrl, payload);
  }

  update(id: string, payload: CreditFileUpdatePayload): Observable<CreditFile> {
    return this.http.put<CreditFile>(`${this.baseUrl}/${id}`, payload);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
