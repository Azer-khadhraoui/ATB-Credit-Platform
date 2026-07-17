import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface AuthResponse {
  token: string;
  matricule: string;
  fullName: string;
  role: string;
}

export interface SignupPayload {
  firstName: string;
  lastName: string;
  matricule: string;
  email: string;
  password: string;
  role: string;
}

const TOKEN_KEY = 'atb_token';
const USER_KEY = 'atb_user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  readonly currentUser = signal<AuthResponse | null>(this.readStoredUser());

  login(matricule: string, password: string): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.apiUrl}/auth/login`, { matricule, password })
      .pipe(tap((response) => this.storeSession(response)));
  }

  signup(payload: SignupPayload): Observable<unknown> {
    return this.http.post(`${this.apiUrl}/users`, payload);
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this.currentUser.set(null);
  }

  get token(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  isAuthenticated(): boolean {
    return !!this.token;
  }

  private storeSession(response: AuthResponse): void {
    localStorage.setItem(TOKEN_KEY, response.token);
    localStorage.setItem(USER_KEY, JSON.stringify(response));
    this.currentUser.set(response);
  }

  private readStoredUser(): AuthResponse | null {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? (JSON.parse(raw) as AuthResponse) : null;
  }
}
