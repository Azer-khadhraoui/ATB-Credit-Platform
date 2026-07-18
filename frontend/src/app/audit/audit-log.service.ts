import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuditLog } from './audit-log.model';

@Injectable({ providedIn: 'root' })
export class AuditLogService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/audit-logs`;

  getAll(): Observable<AuditLog[]> {
    return this.http.get<AuditLog[]>(this.baseUrl);
  }
}
