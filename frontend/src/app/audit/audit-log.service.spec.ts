import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../environments/environment';
import { AuditLogService } from './audit-log.service';

describe('AuditLogService', () => {
  let service: AuditLogService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [AuditLogService, provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(AuditLogService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('GETs the audit log list', () => {
    service.getAll().subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/audit-logs`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });
});
