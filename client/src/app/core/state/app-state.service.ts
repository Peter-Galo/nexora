import { computed, Injectable, signal } from '@angular/core';

/**
 * Application State Interface
 */
export interface AppState {
  loading: boolean;
  error: string | null;
  user: any | null;
  theme: 'light' | 'dark';
  sidebarCollapsed: boolean;
}

/**
 * Initial state configuration
 */
const initialState: AppState = {
  loading: false,
  error: null,
  user: null,
  theme: 'light',
  sidebarCollapsed: false,
};

/**
 * Modern State Management Service using Angular Signals
 * Implements the State Management pattern with reactive programming
 */
@Injectable({
  providedIn: 'root',
})
export class AppStateService {
  // Private state signals
  private readonly _loading = signal<boolean>(initialState.loading);
  private readonly _error = signal<string | null>(initialState.error);
  private readonly _user = signal<any | null>(initialState.user);
  private readonly _theme = signal<'light' | 'dark'>(initialState.theme);
  private readonly _sidebarCollapsed = signal<boolean>(
    initialState.sidebarCollapsed,
  );

  // Public readonly computed signals
  readonly loading = this._loading.asReadonly();
  readonly error = this._error.asReadonly();
  readonly user = this._user.asReadonly();

  // Computed state properties
  readonly isAuthenticated = computed(() => !!this._user());

  // Combined state computed signal
  readonly state = computed<AppState>(() => ({
    loading: this._loading(),
    error: this._error(),
    user: this._user(),
    theme: this._theme(),
    sidebarCollapsed: this._sidebarCollapsed(),
  }));

  setLoading(loading: boolean): void {
    this._loading.set(loading);
  }

  setError(error: string | null): void {
    this._error.set(error);
  }

  clearError(): void {
    this._error.set(null);
  }
}
