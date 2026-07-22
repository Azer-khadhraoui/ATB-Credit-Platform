import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../environments/environment';
import { UserService } from './user.service';

describe('UserService', () => {
  const baseUrl = `${environment.apiUrl}/users`;
  let service: UserService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [UserService, provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(UserService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('GETs all users', () => {
    service.getAll().subscribe();
    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('GETs a user by id', () => {
    service.getById('u-1').subscribe();
    const req = httpMock.expectOne(`${baseUrl}/u-1`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('PUTs an update', () => {
    service.update('u-1', {} as never).subscribe();
    const req = httpMock.expectOne(`${baseUrl}/u-1`);
    expect(req.request.method).toBe('PUT');
    req.flush({});
  });

  it('POSTs a photo as multipart form data', () => {
    const file = new File(['x'], 'avatar.png', { type: 'image/png' });
    service.uploadPhoto('u-1', file).subscribe();
    const req = httpMock.expectOne(`${baseUrl}/u-1/photo`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body instanceof FormData).toBeTrue();
    req.flush({});
  });

  it('DELETEs a user', () => {
    service.delete('u-1').subscribe();
    const req = httpMock.expectOne(`${baseUrl}/u-1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
