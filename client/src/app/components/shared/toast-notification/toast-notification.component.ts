import { Component, Input, Output, EventEmitter, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Notification, NotificationService } from '../../../core/services/notification.service';

/**
 * Toast Notification Component
 * Displays individual toast notifications with animations and actions
 */
@Component({
  selector: 'app-toast-notification',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div
      class="toast show"
      [class.toast-success]="notification.type === 'success'"
      [class.toast-error]="notification.type === 'error'"
      [class.toast-warning]="notification.type === 'warning'"
      [class.toast-info]="notification.type === 'info'"
      [class.toast-dismissed]="notification.dismissed"
      role="alert"
      [attr.aria-live]="notification.type === 'error' ? 'assertive' : 'polite'"
      [attr.aria-atomic]="true"
    >
      <div class="toast-header">
        <div class="toast-icon me-2">
          <i [class]="getIconClass()" [attr.aria-label]="getIconLabel()"></i>
        </div>

        <strong class="me-auto">{{ notification.title || getDefaultTitle() }}</strong>

        <small class="text-muted">{{ getTimeAgo() }}</small>

        <button
          type="button"
          class="btn-close"
          (click)="onDismiss()"
          [attr.aria-label]="'Close ' + (notification.title || getDefaultTitle())"
        ></button>
      </div>

      <div class="toast-body">
        <div class="notification-message">{{ notification.message }}</div>

        <!-- Actions -->
        <div *ngIf="notification.actions && notification.actions.length > 0" class="notification-actions mt-2">
          <div class="d-flex gap-2">
            <button
              *ngFor="let action of notification.actions"
              type="button"
              class="btn btn-sm"
              [class.btn-primary]="action.type === 'primary' || !action.type"
              [class.btn-secondary]="action.type === 'secondary'"
              (click)="executeAction(action)"
            >
              {{ action.label }}
            </button>
          </div>
        </div>

        <!-- Progress bar for timed notifications -->
        <div
          *ngIf="!notification.persistent && notification.duration && showProgress"
          class="notification-progress mt-2"
        >
          <div
            class="progress-bar"
            [style.animation-duration]="notification.duration + 'ms'"
          ></div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .toast {
      max-width: 350px;
      margin-bottom: 0.5rem;
      border: none;
      border-radius: 8px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      transition: all 0.3s ease-in-out;
      transform: translateX(0);
      opacity: 1;
    }

    .toast-dismissed {
      transform: translateX(100%);
      opacity: 0;
    }

    .toast-success {
      border-left: 4px solid var(--bs-success);
    }

    .toast-success .toast-header {
      background-color: rgba(25, 135, 84, 0.1);
    }

    .toast-error {
      border-left: 4px solid var(--bs-danger);
    }

    .toast-error .toast-header {
      background-color: rgba(220, 53, 69, 0.1);
    }

    .toast-warning {
      border-left: 4px solid var(--bs-warning);
    }

    .toast-warning .toast-header {
      background-color: rgba(255, 193, 7, 0.1);
    }

    .toast-info {
      border-left: 4px solid var(--bs-info);
    }

    .toast-info .toast-header {
      background-color: rgba(13, 202, 240, 0.1);
    }

    .toast-header {
      border-bottom: 1px solid rgba(0, 0, 0, 0.1);
      padding: 0.75rem;
    }

    .toast-body {
      padding: 0.75rem;
    }

    .toast-icon {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 20px;
      height: 20px;
    }

    .notification-message {
      word-wrap: break-word;
      line-height: 1.4;
    }

    .notification-actions .btn {
      font-size: 0.875rem;
    }

    .notification-progress {
      height: 3px;
      background-color: rgba(0, 0, 0, 0.1);
      border-radius: 2px;
      overflow: hidden;
    }

    .progress-bar {
      height: 100%;
      background-color: currentColor;
      opacity: 0.3;
      animation: progress-countdown linear;
      transform-origin: left;
    }

    @keyframes progress-countdown {
      from {
        transform: scaleX(1);
      }
      to {
        transform: scaleX(0);
      }
    }

    /* Hover effects */
    .toast:hover {
      box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
    }

    .toast:hover .progress-bar {
      animation-play-state: paused;
    }

    /* Focus styles */
    .btn-close:focus {
      box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, 0.25);
    }

    /* Responsive design */
    @media (max-width: 576px) {
      .toast {
        max-width: calc(100vw - 2rem);
        margin-left: 1rem;
        margin-right: 1rem;
      }

      .notification-actions .d-flex {
        flex-direction: column;
      }

      .notification-actions .btn {
        width: 100%;
        margin-bottom: 0.25rem;
      }

      .notification-actions .btn:last-child {
        margin-bottom: 0;
      }
    }

    /* High contrast mode */
    @media (prefers-contrast: high) {
      .toast {
        border: 2px solid;
      }

      .toast-success {
        border-color: var(--bs-success);
      }

      .toast-error {
        border-color: var(--bs-danger);
      }

      .toast-warning {
        border-color: var(--bs-warning);
      }

      .toast-info {
        border-color: var(--bs-info);
      }
    }

    /* Reduced motion */
    @media (prefers-reduced-motion: reduce) {
      .toast {
        transition: none;
      }

      .toast-dismissed {
        display: none;
      }

      .progress-bar {
        animation: none;
        display: none;
      }
    }
  `]
})
export class ToastNotificationComponent {
  @Input() notification!: Notification;
  @Input() showProgress: boolean = true;
  @Output() dismissed = new EventEmitter<string>();

  private notificationService = inject(NotificationService);

  getIconClass(): string {
    const baseClasses = 'fas';
    switch (this.notification.type) {
      case 'success':
        return `${baseClasses} fa-check-circle text-success`;
      case 'error':
        return `${baseClasses} fa-exclamation-circle text-danger`;
      case 'warning':
        return `${baseClasses} fa-exclamation-triangle text-warning`;
      case 'info':
      default:
        return `${baseClasses} fa-info-circle text-info`;
    }
  }

  getIconLabel(): string {
    switch (this.notification.type) {
      case 'success':
        return 'Success';
      case 'error':
        return 'Error';
      case 'warning':
        return 'Warning';
      case 'info':
      default:
        return 'Information';
    }
  }

  getDefaultTitle(): string {
    switch (this.notification.type) {
      case 'success':
        return 'Success';
      case 'error':
        return 'Error';
      case 'warning':
        return 'Warning';
      case 'info':
      default:
        return 'Information';
    }
  }

  getTimeAgo(): string {
    const now = new Date();
    const diff = now.getTime() - this.notification.timestamp.getTime();
    const seconds = Math.floor(diff / 1000);
    const minutes = Math.floor(seconds / 60);
    const hours = Math.floor(minutes / 60);

    if (seconds < 60) {
      return 'just now';
    } else if (minutes < 60) {
      return `${minutes}m ago`;
    } else if (hours < 24) {
      return `${hours}h ago`;
    } else {
      return this.notification.timestamp.toLocaleDateString();
    }
  }

  executeAction(action: any): void {
    try {
      action.action();
    } catch (error) {
      console.error('Error executing notification action:', error);
    }
  }

  onDismiss(): void {
    this.dismissed.emit(this.notification.id);
    this.notificationService.dismiss(this.notification.id);
  }
}
