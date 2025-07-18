import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

export interface ErrorInfo {
  title?: string;
  message: string;
  type?: 'error' | 'warning' | 'info';
  code?: string | number;
  details?: string;
  timestamp?: Date;
  recoverable?: boolean;
  actions?: ErrorAction[];
}

export interface ErrorAction {
  label: string;
  action: () => void;
  type?: 'primary' | 'secondary' | 'danger';
  icon?: string;
}

/**
 * Enhanced Error Display Component
 * Provides comprehensive error handling with recovery options
 */
@Component({
  selector: 'app-error-display',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div
      class="alert alert-dismissible fade show"
      [class.alert-danger]="error.type === 'error' || !error.type"
      [class.alert-warning]="error.type === 'warning'"
      [class.alert-info]="error.type === 'info'"
      role="alert"
      [attr.aria-live]="error.type === 'error' ? 'assertive' : 'polite'"
    >
      <!-- Header -->
      <div class="d-flex align-items-start">
        <div class="flex-shrink-0 me-3">
          <i
            [class]="getIconClass()"
            class="fa-lg"
            [attr.aria-label]="getIconLabel()"
          ></i>
        </div>

        <div class="flex-grow-1">
          <!-- Title -->
          <h6 *ngIf="error.title" class="alert-heading mb-2">
            {{ error.title }}
          </h6>

          <!-- Main Message -->
          <div class="mb-2">
            <strong>{{ error.message }}</strong>
          </div>

          <!-- Error Code -->
          <div *ngIf="error.code" class="small text-muted mb-2">
            <strong>Error Code:</strong> {{ error.code }}
          </div>

          <!-- Timestamp -->
          <div *ngIf="error.timestamp" class="small text-muted mb-2">
            <strong>Time:</strong> {{ formatTimestamp(error.timestamp) }}
          </div>

          <!-- Details (Collapsible) -->
          <div *ngIf="error.details" class="mt-2">
            <button
              class="btn btn-link btn-sm p-0 text-decoration-none"
              type="button"
              (click)="toggleDetails()"
              [attr.aria-expanded]="showDetails"
              aria-controls="error-details"
            >
              <i
                [class]="showDetails ? 'fas fa-chevron-up' : 'fas fa-chevron-down'"
                class="me-1"
              ></i>
              {{ showDetails ? 'Hide' : 'Show' }} Details
            </button>

            <div
              id="error-details"
              class="mt-2 p-2 bg-light rounded small"
              [class.d-none]="!showDetails"
            >
              <pre class="mb-0 text-wrap">{{ error.details }}</pre>
            </div>
          </div>

          <!-- Recovery Actions -->
          <div *ngIf="error.actions && error.actions.length > 0" class="mt-3">
            <div class="d-flex flex-wrap gap-2">
              <button
                *ngFor="let action of error.actions"
                type="button"
                class="btn btn-sm"
                [class.btn-primary]="action.type === 'primary' || !action.type"
                [class.btn-secondary]="action.type === 'secondary'"
                [class.btn-danger]="action.type === 'danger'"
                (click)="executeAction(action)"
              >
                <i *ngIf="action.icon" [class]="action.icon" class="me-1"></i>
                {{ action.label }}
              </button>
            </div>
          </div>

          <!-- Default Recovery Actions -->
          <div *ngIf="error.recoverable && (!error.actions || error.actions.length === 0)" class="mt-3">
            <div class="d-flex flex-wrap gap-2">
              <button
                type="button"
                class="btn btn-sm btn-primary"
                (click)="onRetry()"
              >
                <i class="fas fa-redo me-1"></i>
                Try Again
              </button>
              <button
                type="button"
                class="btn btn-sm btn-secondary"
                (click)="onRefresh()"
              >
                <i class="fas fa-sync me-1"></i>
                Refresh Page
              </button>
            </div>
          </div>
        </div>

        <!-- Dismiss Button -->
        <button
          *ngIf="dismissible"
          type="button"
          class="btn-close"
          (click)="onDismiss()"
          [attr.aria-label]="'Close ' + (error.title || 'error message')"
        ></button>
      </div>
    </div>
  `,
  styles: [`
    .alert {
      border-left: 4px solid;
    }

    .alert-danger {
      border-left-color: var(--bs-danger);
    }

    .alert-warning {
      border-left-color: var(--bs-warning);
    }

    .alert-info {
      border-left-color: var(--bs-info);
    }

    pre {
      white-space: pre-wrap;
      word-wrap: break-word;
      font-family: 'Courier New', Courier, monospace;
      font-size: 0.875rem;
      max-height: 200px;
      overflow-y: auto;
    }

    .btn-link:focus {
      box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, 0.25);
    }

    @media (max-width: 576px) {
      .d-flex.flex-wrap.gap-2 {
        flex-direction: column;
      }

      .d-flex.flex-wrap.gap-2 .btn {
        width: 100%;
      }
    }
  `]
})
export class ErrorDisplayComponent {
  @Input() error!: ErrorInfo;
  @Input() dismissible: boolean = true;
  @Output() dismissed = new EventEmitter<void>();
  @Output() retry = new EventEmitter<void>();
  @Output() refresh = new EventEmitter<void>();

  showDetails = false;

  getIconClass(): string {
    switch (this.error.type) {
      case 'warning':
        return 'fas fa-exclamation-triangle text-warning';
      case 'info':
        return 'fas fa-info-circle text-info';
      case 'error':
      default:
        return 'fas fa-exclamation-circle text-danger';
    }
  }

  getIconLabel(): string {
    switch (this.error.type) {
      case 'warning':
        return 'Warning';
      case 'info':
        return 'Information';
      case 'error':
      default:
        return 'Error';
    }
  }

  formatTimestamp(timestamp: Date): string {
    return timestamp.toLocaleString();
  }

  toggleDetails(): void {
    this.showDetails = !this.showDetails;
  }

  executeAction(action: ErrorAction): void {
    try {
      action.action();
    } catch (error) {
      console.error('Error executing recovery action:', error);
    }
  }

  onDismiss(): void {
    this.dismissed.emit();
  }

  onRetry(): void {
    this.retry.emit();
  }

  onRefresh(): void {
    this.refresh.emit();
  }
}
