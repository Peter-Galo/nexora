import { Component, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-not-found',
  standalone: true,
  imports: [CommonModule, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="not-found-container">
      <div class="not-found-content">
        <h1 class="error-code">404</h1>
        <h2 class="error-title">Page Not Found</h2>
        <p class="error-message">
          The page you're looking for doesn't exist or has been moved.
        </p>
        <div class="error-actions">
          <a routerLink="/" class="btn btn-primary">Go Home</a>
          <button type="button" class="btn btn-secondary" (click)="goBack()">
            Go Back
          </button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .not-found-container {
      display: flex;
      align-items: center;
      justify-content: center;
      min-height: 100vh;
      padding: 2rem;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }

    .not-found-content {
      text-align: center;
      background: white;
      padding: 3rem;
      border-radius: 1rem;
      box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
      max-width: 500px;
      width: 100%;
    }

    .error-code {
      font-size: 6rem;
      font-weight: bold;
      color: #667eea;
      margin: 0;
      line-height: 1;
    }

    .error-title {
      font-size: 2rem;
      color: #333;
      margin: 1rem 0;
    }

    .error-message {
      color: #666;
      font-size: 1.1rem;
      margin-bottom: 2rem;
      line-height: 1.6;
    }

    .error-actions {
      display: flex;
      gap: 1rem;
      justify-content: center;
      flex-wrap: wrap;
    }

    .btn {
      padding: 0.75rem 1.5rem;
      border: none;
      border-radius: 0.5rem;
      font-size: 1rem;
      font-weight: 500;
      text-decoration: none;
      cursor: pointer;
      transition: all 0.3s ease;
      display: inline-block;
    }

    .btn-primary {
      background: #667eea;
      color: white;
    }

    .btn-primary:hover {
      background: #5a6fd8;
      transform: translateY(-2px);
    }

    .btn-secondary {
      background: #f8f9fa;
      color: #333;
      border: 1px solid #dee2e6;
    }

    .btn-secondary:hover {
      background: #e9ecef;
      transform: translateY(-2px);
    }

    @media (max-width: 768px) {
      .not-found-content {
        padding: 2rem;
      }

      .error-code {
        font-size: 4rem;
      }

      .error-title {
        font-size: 1.5rem;
      }

      .error-actions {
        flex-direction: column;
      }
    }
  `]
})
export class NotFoundComponent {
  goBack(): void {
    window.history.back();
  }
}
