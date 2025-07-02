import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';

export interface User {
  email: string;
  firstName: string;
  lastName: string;
  role: string;
}

export interface AuthResponse {
  token: string;
  user?: User;
  email?: string;
  firstName?: string;
  lastName?: string;
  role?: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private static readonly API_URL = 'http://localhost:8080/api/v1/auth';
  private static readonly TOKEN_KEY = 'auth_token';
  private static readonly USER_KEY = 'auth_user';

  private _isAuthenticatedSubject = new BehaviorSubject<boolean>(
    this.hasToken(),
  );
  readonly isAuthenticated$ = this._isAuthenticatedSubject.asObservable();

  constructor(private readonly http: HttpClient) {}

  login(email: string, password: string): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${AuthService.API_URL}/authenticate`, {
        email,
        password,
      })
      .pipe(
        tap((response) => this.setSession(response)),
        catchError((error) => {
          return throwError(
            () => new Error('Login failed. Please check your credentials.'),
          );
        }),
      );
  }

  register(data: {
    firstName: string;
    lastName: string;
    email: string;
    password: string;
  }): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${AuthService.API_URL}/register`, data)
      .pipe(
        tap((response) => this.setSession(response)),
        catchError((error) => {
          // Pass through the original error to allow components to handle specific error formats
          return throwError(() => error);
        }),
      );
  }

  logout(): void {
    this.clearSession();
    this._isAuthenticatedSubject.next(false);
  }

  getToken(): string | null {
    return localStorage.getItem(AuthService.TOKEN_KEY);
  }

  getUser(): User | null {
    const userStr = localStorage.getItem(AuthService.USER_KEY);
    if (!userStr || userStr === 'undefined') {
      return null;
    }
    try {
      return JSON.parse(userStr) as User;
    } catch {
      return null;
    }
  }

  isAuthenticated(): boolean {
    return this.hasToken();
  }

  private setSession(authResult: AuthResponse): void {
    localStorage.setItem(AuthService.TOKEN_KEY, authResult.token);
    let user: User | undefined = authResult.user;
    if (!user) {
      user = {
        email: authResult.email ?? '',
        firstName: authResult.firstName ?? '',
        lastName: authResult.lastName ?? '',
        role: authResult.role ?? '',
      };
    }
    localStorage.setItem(AuthService.USER_KEY, JSON.stringify(user));
    this._isAuthenticatedSubject.next(true);
  }

  private clearSession(): void {
    localStorage.removeItem(AuthService.TOKEN_KEY);
    localStorage.removeItem(AuthService.USER_KEY);
  }

  private hasToken(): boolean {
    return !!this.getToken();
  }
}
