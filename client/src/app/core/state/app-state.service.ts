import { Injectable, computed, signal } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

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
  providedIn: 'root'
})
export class AppStateService {
  // Private state signals
  private readonly _loading = signal<boolean>(initialState.loading);
  private readonly _error = signal<string | null>(initialState.error);
  private readonly _user = signal<any | null>(initialState.user);
  private readonly _theme = signal<'light' | 'dark'>(initialState.theme);
  private readonly _sidebarCollapsed = signal<boolean>(initialState.sidebarCollapsed);

  // Public readonly computed signals
  readonly loading = this._loading.asReadonly();
  readonly error = this._error.asReadonly();
  readonly user = this._user.asReadonly();
  readonly theme = this._theme.asReadonly();
  readonly sidebarCollapsed = this._sidebarCollapsed.asReadonly();

  // Computed state properties
  readonly isAuthenticated = computed(() => !!this._user());
  readonly isDarkTheme = computed(() => this._theme() === 'dark');
  readonly hasError = computed(() => !!this._error());

  // Combined state computed signal
  readonly state = computed<AppState>(() => ({
    loading: this._loading(),
    error: this._error(),
    user: this._user(),
    theme: this._theme(),
    sidebarCollapsed: this._sidebarCollapsed(),
  }));

  /**
   * Set loading state
   */
  setLoading(loading: boolean): void {
    this._loading.set(loading);
  }

  /**
   * Set error state
   */
  setError(error: string | null): void {
    this._error.set(error);
  }

  /**
   * Clear error state
   */
  clearError(): void {
    this._error.set(null);
  }

  /**
   * Set user state
   */
  setUser(user: any | null): void {
    this._user.set(user);
  }

  /**
   * Set theme
   */
  setTheme(theme: 'light' | 'dark'): void {
    this._theme.set(theme);
    // Persist theme preference
    localStorage.setItem('app-theme', theme);
  }

  /**
   * Toggle theme
   */
  toggleTheme(): void {
    const newTheme = this._theme() === 'light' ? 'dark' : 'light';
    this.setTheme(newTheme);
  }

  /**
   * Set sidebar collapsed state
   */
  setSidebarCollapsed(collapsed: boolean): void {
    this._sidebarCollapsed.set(collapsed);
    // Persist sidebar state
    localStorage.setItem('sidebar-collapsed', collapsed.toString());
  }

  /**
   * Toggle sidebar collapsed state
   */
  toggleSidebar(): void {
    this.setSidebarCollapsed(!this._sidebarCollapsed());
  }

  /**
   * Reset state to initial values
   */
  reset(): void {
    this._loading.set(initialState.loading);
    this._error.set(initialState.error);
    this._user.set(initialState.user);
    this._theme.set(initialState.theme);
    this._sidebarCollapsed.set(initialState.sidebarCollapsed);
  }

  /**
   * Initialize state from localStorage
   */
  initializeFromStorage(): void {
    // Load theme preference
    const savedTheme = localStorage.getItem('app-theme') as 'light' | 'dark';
    if (savedTheme) {
      this._theme.set(savedTheme);
    }

    // Load sidebar state
    const savedSidebarState = localStorage.getItem('sidebar-collapsed');
    if (savedSidebarState) {
      this._sidebarCollapsed.set(savedSidebarState === 'true');
    }
  }

  /**
   * Update multiple state properties at once
   */
  updateState(partialState: Partial<AppState>): void {
    if (partialState.loading !== undefined) {
      this._loading.set(partialState.loading);
    }
    if (partialState.error !== undefined) {
      this._error.set(partialState.error);
    }
    if (partialState.user !== undefined) {
      this._user.set(partialState.user);
    }
    if (partialState.theme !== undefined) {
      this._theme.set(partialState.theme);
    }
    if (partialState.sidebarCollapsed !== undefined) {
      this._sidebarCollapsed.set(partialState.sidebarCollapsed);
    }
  }
}
