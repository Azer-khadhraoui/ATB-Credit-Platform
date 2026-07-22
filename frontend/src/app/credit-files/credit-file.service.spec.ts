import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../environments/environment';
import { CreditFileService } from './credit-file.service';

describe('CreditFileService', () => {
  const baseUrl = `${environment.apiUrl}/credit-files`;
  let service: CreditFileService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [CreditFileService, provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(CreditFileService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('GETs all files', () => {
    service.getAll().subscribe();
    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('GETs a file by id', () => {
    service.getById('f-1').subscribe();
    const req = httpMock.expectOne(`${baseUrl}/f-1`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('POSTs a new file', () => {
    service.create({ clientId: 'c-1' } as never).subscribe();
    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('POST');
    req.flush({});
  });

  it('PUTs an update', () => {
    service.update('f-1', {} as never).subscribe();
    const req = httpMock.expectOne(`${baseUrl}/f-1`);
    expect(req.request.method).toBe('PUT');
    req.flush({});
  });

  it('DELETEs a file', () => {
    service.delete('f-1').subscribe();
    const req = httpMock.expectOne(`${baseUrl}/f-1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('POSTs to the analyze endpoint', () => {
    service.analyze('f-1').subscribe();
    const req = httpMock.expectOne(`${baseUrl}/f-1/analyze`);
    expect(req.request.method).toBe('POST');
    req.flush({});
  });
});
