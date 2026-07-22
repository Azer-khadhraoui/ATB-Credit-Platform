import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../environments/environment';
import { ClientPayload, ClientService } from './client.service';

describe('ClientService', () => {
  const baseUrl = `${environment.apiUrl}/clients`;
  let service: ClientService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ClientService, provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(ClientService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('GETs the client list', () => {
    service.getAll().subscribe();
    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('GETs a client by id', () => {
    service.getById('c-9').subscribe();
    const req = httpMock.expectOne(`${baseUrl}/c-9`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('POSTs a new client with its payload', () => {
    const payload = { cin: '12345678', firstName: 'Sana' } as unknown as ClientPayload;
    service.create(payload).subscribe();
    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);
    req.flush({});
  });

  it('PUTs an update to a client', () => {
    service.update('c-9', { firstName: 'Omar' } as never).subscribe();
    const req = httpMock.expectOne(`${baseUrl}/c-9`);
    expect(req.request.method).toBe('PUT');
    req.flush({});
  });

  it('DELETEs a client', () => {
    service.delete('c-9').subscribe();
    const req = httpMock.expectOne(`${baseUrl}/c-9`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
