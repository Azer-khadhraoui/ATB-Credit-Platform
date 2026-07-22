import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { AuthResponse, AuthService } from './auth.service';

const SESSION: AuthResponse = {
  token: 'jwt-token',
  id: 'u-1',
  matricule: '123456',
  fullName: 'Azer Khadhraoui',
  role: 'ADMIN'
};

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [AuthService, provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('stores the session and exposes the token after login', () => {
    let emitted: AuthResponse | undefined;
    service.login('123456', 'secret').subscribe((r) => (emitted = r));

    const req = httpMock.expectOne(`${environment.apiUrl}/auth/login`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ matricule: '123456', password: 'secret' });
    req.flush(SESSION);

    expect(emitted).toEqual(SESSION);
    expect(service.token).toBe('jwt-token');
    expect(service.isAuthenticated()).toBeTrue();
    expect(service.currentUser()?.matricule).toBe('123456');
  });

  it('clears the session on logout', () => {
    service.login('123456', 'secret').subscribe();
    httpMock.expectOne(`${environment.apiUrl}/auth/login`).flush(SESSION);

    service.logout();

    expect(service.token).toBeNull();
    expect(service.isAuthenticated()).toBeFalse();
    expect(service.currentUser()).toBeNull();
  });

  it('patches the in-session profile only for the logged-in user', () => {
    service.login('123456', 'secret').subscribe();
    httpMock.expectOne(`${environment.apiUrl}/auth/login`).flush(SESSION);

    service.updateCurrentSession('u-1', { fullName: 'Nouveau Nom' });
    expect(service.currentUser()?.fullName).toBe('Nouveau Nom');

    // A patch aimed at a different user id is ignored.
    service.updateCurrentSession('someone-else', { fullName: 'Ignoré' });
    expect(service.currentUser()?.fullName).toBe('Nouveau Nom');
  });
});
