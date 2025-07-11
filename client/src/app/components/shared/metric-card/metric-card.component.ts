import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

export interface MetricCardData {
  title: string;
  value: string | number;
  icon: string;
  iconClass: string;
  subtitle?: string;
  subtitleIcon?: string;
  subtitleClass?: string;
  trend?: {
    value: number;
    isPositive: boolean;
    label: string;
  };
  clickable?: boolean;
}

/**
 * Reusable Metric Card Component
 * Displays key metrics with icons, trends, and optional click actions
 */
@Component({
  selector: 'app-metric-card',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div
      class="card h-100 metric-card"
      [class.clickable]="clickable"
      [class.shadow-sm]="!clickable"
      [class.shadow]="clickable"
      (click)="onCardClick()"
      [attr.role]="clickable ? 'button' : null"
      [attr.tabindex]="clickable ? '0' : null"
      [attr.aria-label]="clickable ? 'Click to view ' + title : null"
      (keydown.enter)="onCardClick()"
      (keydown.space)="onCardClick()"
    >
      <div class="card-body d-flex align-items-center">
        <!-- Icon Section -->
        <div class="flex-shrink-0 me-3">
          <div class="metric-icon-wrapper p-3 rounded-circle bg-light">
            <i [class]="icon + ' ' + iconClass" aria-hidden="true"></i>
          </div>
        </div>

        <!-- Content Section -->
        <div class="flex-grow-1">
          <h6 class="card-title text-muted mb-1 small text-uppercase fw-bold">
            {{ title }}
          </h6>

          <div class="metric-value mb-1">
            <span class="h4 mb-0 fw-bold text-dark">{{ value }}</span>
          </div>

          <!-- Subtitle -->
          <div *ngIf="subtitle" class="d-flex align-items-center">
            <i
              *ngIf="subtitleIcon"
              [class]="subtitleIcon + ' ' + (subtitleClass || 'text-muted')"
              class="me-1 small"
              aria-hidden="true"
            ></i>
            <small [class]="subtitleClass || 'text-muted'">{{ subtitle }}</small>
          </div>

          <!-- Trend Indicator -->
          <div *ngIf="trend" class="mt-2">
            <span
              class="badge"
              [class.bg-success]="trend.isPositive"
              [class.bg-danger]="!trend.isPositive"
            >
              <i
                [class]="trend.isPositive ? 'fas fa-arrow-up' : 'fas fa-arrow-down'"
                class="me-1"
                aria-hidden="true"
              ></i>
              {{ trend.value }}% {{ trend.label }}
            </span>
          </div>
        </div>

        <!-- Click Indicator -->
        <div *ngIf="clickable" class="flex-shrink-0 ms-2">
          <i class="fas fa-chevron-right text-muted" aria-hidden="true"></i>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .metric-card {
      transition: all 0.2s ease-in-out;
      border: 1px solid rgba(0,0,0,.125);
    }

    .metric-card.clickable {
      cursor: pointer;
    }

    .metric-card.clickable:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 8px rgba(0,0,0,.15) !important;
      border-color: var(--bs-primary);
    }

    .metric-card.clickable:focus {
      outline: 2px solid var(--bs-primary);
      outline-offset: 2px;
    }

    .metric-icon-wrapper {
      width: 60px;
      height: 60px;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .metric-value {
      line-height: 1.2;
    }

    @media (max-width: 768px) {
      .metric-icon-wrapper {
        width: 50px;
        height: 50px;
      }

      .card-body {
        padding: 1rem 0.75rem;
      }
    }
  `]
})
export class MetricCardComponent {
  @Input() title!: string;
  @Input() value!: string | number;
  @Input() icon!: string;
  @Input() iconClass: string = 'text-primary';
  @Input() subtitle?: string;
  @Input() subtitleIcon?: string;
  @Input() subtitleClass?: string;
  @Input() trend?: {
    value: number;
    isPositive: boolean;
    label: string;
  };
  @Input() clickable: boolean = false;

  onCardClick(): void {
    if (this.clickable) {
      // Emit click event or handle navigation
      // This can be extended with @Output() events as needed
    }
  }
}
