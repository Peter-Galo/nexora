import { Injectable, signal, computed } from '@angular/core';
import { BehaviorSubject, timer } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

export type NotificationType = 'success' | 'error' | 'warning' | 'info';
export type NotificationPosition = 'top-right' | 'top-left' | 'bottom-right' | 'bottom-left' | 'top-center' | 'bottom-center';

export interface Notification {
  id: string;
  type: NotificationType;
  title?: string;
  message: string;
  duration?: number;
  persistent?: boolean;
  actions?: NotificationAction[];
  timestamp: Date;
  dismissed?: boolean;
}

export interface NotificationAction {
  label: string;
  action: () => void;
  type?: 'primary' | 'secondary';
}

export interface NotificationConfig {
  position: NotificationPosition;
  maxNotifications: number;
  defaultDuration: number;
  enableSound: boolean;
  enableAnimation: boolean;
}

/**
 * Modern Notification Service using Angular Signals
 * Provides toast notifications, alerts, and user feedback
 */
@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  // Default configuration
  private readonly defaultConfig: NotificationConfig = {
    position: 'top-right',
    maxNotifications: 5,
    defaultDuration: 5000,
    enableSound: false,
    enableAnimation: true
  };

  // Service state using signals
  private readonly _notifications = signal<Notification[]>([]);
  private readonly _config = signal<NotificationConfig>(this.defaultConfig);
  private readonly _isEnabled = signal<boolean>(true);

  // Public readonly signals
  readonly notifications = this._notifications.asReadonly();
  readonly config = this._config.asReadonly();
  readonly isEnabled = this._isEnabled.asReadonly();

  // Computed signals
  readonly activeNotifications = computed(() =>
    this._notifications().filter(n => !n.dismissed)
  );

  readonly notificationCount = computed(() =>
    this.activeNotifications().length
  );

  readonly hasNotifications = computed(() =>
    this.notificationCount() > 0
  );

  // Notification counters by type
  readonly errorCount = computed(() =>
    this.activeNotifications().filter(n => n.type === 'error').length
  );

  readonly warningCount = computed(() =>
    this.activeNotifications().filter(n => n.type === 'warning').length
  );

  readonly successCount = computed(() =>
    this.activeNotifications().filter(n => n.type === 'success').length
  );

  readonly infoCount = computed(() =>
    this.activeNotifications().filter(n => n.type === 'info').length
  );

  private notificationIdCounter = 0;

  /**
   * Show a success notification
   */
  success(message: string, title?: string, options?: Partial<Notification>): string {
    return this.show({
      type: 'success',
      title,
      message,
      ...options
    });
  }

  /**
   * Show an error notification
   */
  error(message: string, title?: string, options?: Partial<Notification>): string {
    return this.show({
      type: 'error',
      title,
      message,
      persistent: true, // Errors are persistent by default
      ...options
    });
  }

  /**
   * Show a warning notification
   */
  warning(message: string, title?: string, options?: Partial<Notification>): string {
    return this.show({
      type: 'warning',
      title,
      message,
      duration: 7000, // Warnings last longer
      ...options
    });
  }

  /**
   * Show an info notification
   */
  info(message: string, title?: string, options?: Partial<Notification>): string {
    return this.show({
      type: 'info',
      title,
      message,
      ...options
    });
  }

  /**
   * Show a generic notification
   */
  show(notification: Partial<Notification>): string {
    if (!this._isEnabled()) {
      return '';
    }

    const id = this.generateId();
    const newNotification: Notification = {
      id,
      type: 'info',
      message: '',
      timestamp: new Date(),
      duration: this._config().defaultDuration,
      persistent: false,
      dismissed: false,
      ...notification
    };

    // Add notification to the list
    this._notifications.update(notifications => {
      const updated = [...notifications, newNotification];

      // Limit the number of notifications
      const maxNotifications = this._config().maxNotifications;
      if (updated.length > maxNotifications) {
        // Remove oldest notifications
        return updated.slice(-maxNotifications);
      }

      return updated;
    });

    // Auto-dismiss if not persistent
    if (!newNotification.persistent && newNotification.duration && newNotification.duration > 0) {
      timer(newNotification.duration).subscribe(() => {
        this.dismiss(id);
      });
    }

    // Play sound if enabled
    if (this._config().enableSound) {
      this.playNotificationSound(newNotification.type);
    }

    return id;
  }

  /**
   * Dismiss a specific notification
   */
  dismiss(id: string): void {
    this._notifications.update(notifications =>
      notifications.map(n =>
        n.id === id ? { ...n, dismissed: true } : n
      )
    );

    // Remove dismissed notifications after animation
    setTimeout(() => {
      this._notifications.update(notifications =>
        notifications.filter(n => n.id !== id)
      );
    }, 300); // Animation duration
  }

  /**
   * Dismiss all notifications
   */
  dismissAll(): void {
    this._notifications.update(notifications =>
      notifications.map(n => ({ ...n, dismissed: true }))
    );

    // Clear all after animation
    setTimeout(() => {
      this._notifications.set([]);
    }, 300);
  }

  /**
   * Dismiss notifications by type
   */
  dismissByType(type: NotificationType): void {
    this._notifications.update(notifications =>
      notifications.map(n =>
        n.type === type ? { ...n, dismissed: true } : n
      )
    );

    // Remove dismissed notifications after animation
    setTimeout(() => {
      this._notifications.update(notifications =>
        notifications.filter(n => n.type !== type || !n.dismissed)
      );
    }, 300);
  }

  /**
   * Update notification configuration
   */
  updateConfig(config: Partial<NotificationConfig>): void {
    this._config.update(current => ({ ...current, ...config }));
  }

  /**
   * Enable/disable notifications
   */
  setEnabled(enabled: boolean): void {
    this._isEnabled.set(enabled);

    if (!enabled) {
      this.dismissAll();
    }
  }

  /**
   * Get notification by ID
   */
  getNotification(id: string): Notification | undefined {
    return this._notifications().find(n => n.id === id);
  }

  /**
   * Clear all notifications immediately
   */
  clear(): void {
    this._notifications.set([]);
  }

  /**
   * Get notifications by type
   */
  getNotificationsByType(type: NotificationType): Notification[] {
    return this.activeNotifications().filter(n => n.type === type);
  }

  /**
   * Check if a notification exists
   */
  hasNotification(id: string): boolean {
    return this._notifications().some(n => n.id === id && !n.dismissed);
  }

  /**
   * Update an existing notification
   */
  update(id: string, updates: Partial<Notification>): void {
    this._notifications.update(notifications =>
      notifications.map(n =>
        n.id === id ? { ...n, ...updates } : n
      )
    );
  }

  /**
   * Generate unique notification ID
   */
  private generateId(): string {
    return `notification-${++this.notificationIdCounter}-${Date.now()}`;
  }

  /**
   * Play notification sound based on type
   */
  private playNotificationSound(type: NotificationType): void {
    if (!this._config().enableSound) {
      return;
    }

    try {
      // Create audio context for different notification sounds
      const audioContext = new (window.AudioContext || (window as any).webkitAudioContext)();
      const oscillator = audioContext.createOscillator();
      const gainNode = audioContext.createGain();

      oscillator.connect(gainNode);
      gainNode.connect(audioContext.destination);

      // Different frequencies for different notification types
      const frequencies = {
        success: 800,
        error: 400,
        warning: 600,
        info: 500
      };

      oscillator.frequency.setValueAtTime(frequencies[type], audioContext.currentTime);
      oscillator.type = 'sine';

      gainNode.gain.setValueAtTime(0.1, audioContext.currentTime);
      gainNode.gain.exponentialRampToValueAtTime(0.01, audioContext.currentTime + 0.2);

      oscillator.start(audioContext.currentTime);
      oscillator.stop(audioContext.currentTime + 0.2);
    } catch (error) {
      console.warn('Could not play notification sound:', error);
    }
  }

  /**
   * Batch operations for multiple notifications
   */
  showBatch(notifications: Partial<Notification>[]): string[] {
    return notifications.map(notification => this.show(notification));
  }

  /**
   * Show loading notification that can be updated
   */
  showLoading(message: string, title?: string): string {
    return this.show({
      type: 'info',
      title,
      message,
      persistent: true,
      actions: []
    });
  }

  /**
   * Update loading notification to success
   */
  updateLoadingToSuccess(id: string, message: string, title?: string): void {
    this.update(id, {
      type: 'success',
      title,
      message,
      persistent: false,
      duration: this._config().defaultDuration
    });

    // Auto-dismiss after duration
    timer(this._config().defaultDuration).subscribe(() => {
      this.dismiss(id);
    });
  }

  /**
   * Update loading notification to error
   */
  updateLoadingToError(id: string, message: string, title?: string): void {
    this.update(id, {
      type: 'error',
      title,
      message,
      persistent: true
    });
  }
}
