import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

export type SkeletonType = 'text' | 'card' | 'table' | 'metric' | 'avatar' | 'button' | 'custom';

/**
 * Reusable Loading Skeleton Component
 * Provides various skeleton loading patterns for better UX
 */
@Component({
  selector: 'app-loading-skeleton',
  standalone: true,
  imports: [CommonModule],
  template: `
    <!-- Text Skeleton -->
    <div *ngIf="type === 'text'" class="skeleton-container">
      <div
        *ngFor="let line of getLines()"
        class="skeleton skeleton-text"
        [style.width]="getLineWidth(line)"
        [style.height]="height || '1rem'"
      ></div>
    </div>

    <!-- Card Skeleton -->
    <div *ngIf="type === 'card'" class="card skeleton-card">
      <div class="card-body">
        <div class="d-flex align-items-center mb-3">
          <div class="skeleton skeleton-avatar me-3"></div>
          <div class="flex-grow-1">
            <div class="skeleton skeleton-text mb-2" style="width: 60%;"></div>
            <div class="skeleton skeleton-text" style="width: 40%;"></div>
          </div>
        </div>
        <div class="skeleton skeleton-text mb-2" style="width: 100%;"></div>
        <div class="skeleton skeleton-text mb-2" style="width: 80%;"></div>
        <div class="skeleton skeleton-text" style="width: 60%;"></div>
      </div>
    </div>

    <!-- Table Skeleton -->
    <div *ngIf="type === 'table'" class="skeleton-table">
      <div class="table-responsive">
        <table class="table">
          <thead>
            <tr>
              <th *ngFor="let col of getColumns()">
                <div class="skeleton skeleton-text" style="width: 80%;"></div>
              </th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let row of getRows()">
              <td *ngFor="let col of getColumns()">
                <div class="skeleton skeleton-text" [style.width]="getRandomWidth()"></div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Metric Skeleton -->
    <div *ngIf="type === 'metric'" class="card h-100">
      <div class="card-body d-flex align-items-center">
        <div class="flex-shrink-0 me-3">
          <div class="skeleton skeleton-metric-icon"></div>
        </div>
        <div class="flex-grow-1">
          <div class="skeleton skeleton-text mb-2" style="width: 70%;"></div>
          <div class="skeleton skeleton-text mb-1" style="width: 50%; height: 2rem;"></div>
          <div class="skeleton skeleton-text" style="width: 60%;"></div>
        </div>
      </div>
    </div>

    <!-- Avatar Skeleton -->
    <div *ngIf="type === 'avatar'"
         class="skeleton skeleton-avatar"
         [style.width]="width || '40px'"
         [style.height]="height || '40px'">
    </div>

    <!-- Button Skeleton -->
    <div *ngIf="type === 'button'"
         class="skeleton skeleton-button"
         [style.width]="width || '100px'"
         [style.height]="height || '38px'">
    </div>

    <!-- Custom Skeleton -->
    <div *ngIf="type === 'custom'"
         class="skeleton"
         [style.width]="width"
         [style.height]="height"
         [style.border-radius]="borderRadius">
    </div>
  `,
  styles: [`
    .skeleton {
      background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
      background-size: 200% 100%;
      animation: loading 1.5s infinite;
      border-radius: 4px;
    }

    @keyframes loading {
      0% {
        background-position: 200% 0;
      }
      100% {
        background-position: -200% 0;
      }
    }

    .skeleton-container {
      padding: 0.5rem 0;
    }

    .skeleton-text {
      height: 1rem;
      margin-bottom: 0.5rem;
    }

    .skeleton-text:last-child {
      margin-bottom: 0;
    }

    .skeleton-card {
      border: 1px solid #e0e0e0;
    }

    .skeleton-avatar {
      width: 40px;
      height: 40px;
      border-radius: 50%;
    }

    .skeleton-button {
      height: 38px;
      border-radius: 6px;
    }

    .skeleton-metric-icon {
      width: 60px;
      height: 60px;
      border-radius: 50%;
    }

    .skeleton-table th,
    .skeleton-table td {
      padding: 0.75rem;
      border-bottom: 1px solid #e0e0e0;
    }

    /* Dark mode support */
    @media (prefers-color-scheme: dark) {
      .skeleton {
        background: linear-gradient(90deg, #2a2a2a 25%, #3a3a3a 50%, #2a2a2a 75%);
        background-size: 200% 100%;
      }

      .skeleton-card {
        border-color: #3a3a3a;
        background-color: #1a1a1a;
      }

      .skeleton-table th,
      .skeleton-table td {
        border-bottom-color: #3a3a3a;
      }
    }

    /* Reduced motion support */
    @media (prefers-reduced-motion: reduce) {
      .skeleton {
        animation: none;
        background: #f0f0f0;
      }

      @media (prefers-color-scheme: dark) {
        .skeleton {
          background: #2a2a2a;
        }
      }
    }

    /* High contrast mode support */
    @media (prefers-contrast: high) {
      .skeleton {
        background: #d0d0d0;
        border: 1px solid #999;
      }

      @media (prefers-color-scheme: dark) {
        .skeleton {
          background: #404040;
          border-color: #666;
        }
      }
    }
  `]
})
export class LoadingSkeletonComponent {
  @Input() type: SkeletonType = 'text';
  @Input() lines: number = 3;
  @Input() columns: number = 4;
  @Input() rows: number = 5;
  @Input() width?: string;
  @Input() height?: string;
  @Input() borderRadius?: string;

  getLines(): number[] {
    return Array.from({ length: this.lines }, (_, i) => i);
  }

  getColumns(): number[] {
    return Array.from({ length: this.columns }, (_, i) => i);
  }

  getRows(): number[] {
    return Array.from({ length: this.rows }, (_, i) => i);
  }

  getLineWidth(lineIndex: number): string {
    // Vary line widths for more realistic appearance
    const widths = ['100%', '85%', '92%', '78%', '95%'];
    return widths[lineIndex % widths.length];
  }

  getRandomWidth(): string {
    const widths = ['60%', '75%', '85%', '70%', '90%'];
    return widths[Math.floor(Math.random() * widths.length)];
  }
}
