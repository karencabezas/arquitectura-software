import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import {
  LoginRequest, LoginResponse, MfaValidateRequest,
  AuthResponse, MfaSetupResponse, CurrentUser
} from '../models/models';

@Injectable({ providedIn: 'root' })
export class AuthService {

  private readonly API = 'http://localhost:8080/api/auth';
  private readonly TOKEN_KEY = 'authapp_token';
  private readonly USER_KEY  = 'authapp_user';

  private _currentUser = signal<CurrentUser | null>(this.loadUser());
  currentUser = this._currentUser.asReadonly();
  isLoggedIn  = computed(() => this._currentUser() !== null);
  isAdmin     = computed(() => this._currentUser()?.roles?.includes('ADMIN') ?? false);

  constructor(private http: HttpClient, private router: Router) {}

  login(req: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.API}/login`, req);
  }

  validateMfa(req: MfaValidateRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API}/mfa/validate`, req).pipe(
      tap((res: AuthResponse) => this.saveSession(res))
    );
  }

  setupMfa(preAuthToken: string): Observable<MfaSetupResponse> {
    return this.http.post<MfaSetupResponse>(
      `${this.API}/mfa/setup`, {},
      { headers: { Authorization: `Bearer ${preAuthToken}` } }
    );
  }

  confirmMfaSetup(preAuthToken: string, totpCode: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(
      `${this.API}/mfa/setup/confirm`,
      { totpCode },
      { headers: { Authorization: `Bearer ${preAuthToken}` } }
    ).pipe(tap((res: AuthResponse) => this.saveSession(res)));
  }

  register(email: string, password: string): Observable<unknown> {
    return this.http.post(`${this.API}/register`, { email, password });
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  hasPermission(permission: string): boolean {
    return this._currentUser()?.permissions?.includes(permission) ?? false;
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this._currentUser.set(null);
    this.router.navigate(['/login']);
  }

  private saveSession(res: AuthResponse): void {
    localStorage.setItem(this.TOKEN_KEY, res.token);
    const user: CurrentUser = {
      id: this.extractUserIdFromToken(res.token),  // ← extraído del JWT real
      email: res.email,
      roles: res.roles,
      permissions: res.permissions
    };
    localStorage.setItem(this.USER_KEY, JSON.stringify(user));
    this._currentUser.set(user);
  }

  /**
   * El JWT final incluye el claim "userId" en el payload.
   * Lo decodificamos con atob() — no necesitamos librería externa
   * porque no estamos verificando la firma, solo leyendo el payload.
   */
  private extractUserIdFromToken(token: string): number {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload['userId'] ?? 0;
    } catch {
      return 0;
    }
  }

  private loadUser(): CurrentUser | null {
    const raw = localStorage.getItem(this.USER_KEY);
    return raw ? JSON.parse(raw) as CurrentUser : null;
  }
}
