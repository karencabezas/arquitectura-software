import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { JwtPayload, LoginResponse, TokenResponse, UserRole } from '../../shared/models/models';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AuthService {

  private readonly ACCESS_TOKEN_KEY  = 'ar_access_token';
  private readonly REFRESH_TOKEN_KEY = 'ar_refresh_token';
  private readonly TEMP_TOKEN_KEY    = 'ar_temp_token';

  // Signals — reactive state
  private _user = signal<JwtPayload | null>(this.parseStoredToken());
  readonly user   = this._user.asReadonly();
  readonly role   = computed(() => this._user()?.role ?? null);
  readonly region = computed(() => this._user()?.region ?? null);
  readonly clearanceLevel = computed(() => this._user()?.clearance_level ?? 0);
  readonly isLoggedIn = computed(() => !!this._user());

  constructor(private http: HttpClient, private router: Router) {}

  login(username: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${environment.apiUrl}/auth/login`, { username, password })
      .pipe(tap(res => {
        if (res.status === 'MFA_REQUIRED' && res.tempToken) {
          sessionStorage.setItem(this.TEMP_TOKEN_KEY, res.tempToken);
          this.router.navigate(['/mfa']);
        } else if (res.status === 'SUCCESS' && res.accessToken) {
          this.storeTokens(res.accessToken, res.refreshToken!);
          this.router.navigate(['/dashboard']);
        }
      }));
  }

  verifyMfa(totpCode: number): Observable<TokenResponse> {
    const tempToken = sessionStorage.getItem(this.TEMP_TOKEN_KEY);
    return this.http.post<TokenResponse>(`${environment.apiUrl}/auth/verify-mfa`, { tempToken, totpCode })
      .pipe(tap(res => {
        sessionStorage.removeItem(this.TEMP_TOKEN_KEY);
        this.storeTokens(res.accessToken, res.refreshToken);
      }));
  }

  refresh(): Observable<TokenResponse> {
    const refreshToken = localStorage.getItem(this.REFRESH_TOKEN_KEY);
    return this.http.post<TokenResponse>(`${environment.apiUrl}/auth/refresh`, { refreshToken })
      .pipe(tap(res => this.storeTokens(res.accessToken, res.refreshToken)));
  }

  logout(): void {
    localStorage.removeItem(this.ACCESS_TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_TOKEN_KEY);
    sessionStorage.removeItem(this.TEMP_TOKEN_KEY);
    this._user.set(null);
    this.router.navigate(['/login']);
  }

  getAccessToken(): string | null {
    return localStorage.getItem(this.ACCESS_TOKEN_KEY);
  }

  hasRole(...roles: UserRole[]): boolean {
    const current = this.role();
    return current ? roles.includes(current) : false;
  }

  isTokenExpired(): boolean {
    const payload = this._user();
    if (!payload) return true;
    return Date.now() >= payload.exp * 1000;
  }

  private storeTokens(accessToken: string, refreshToken: string): void {
    localStorage.setItem(this.ACCESS_TOKEN_KEY, accessToken);
    localStorage.setItem(this.REFRESH_TOKEN_KEY, refreshToken);
    this._user.set(this.decodeJwt(accessToken));
  }

  private parseStoredToken(): JwtPayload | null {
    const token = localStorage.getItem(this.ACCESS_TOKEN_KEY);
    if (!token) return null;
    const payload = this.decodeJwt(token);
    if (!payload || Date.now() >= payload.exp * 1000) return null;
    return payload;
  }

  private decodeJwt(token: string): JwtPayload | null {
    try {
      const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
      return JSON.parse(atob(base64));
    } catch {
      return null;
    }
  }
}
