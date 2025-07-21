import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-export-status-alert',
  standalone: true,
  imports: [CommonModule],
  template: `
    @if (exportStatus && exportStatus !== 'COMPLETED') {
      <div
        class="alert"
        [class]="
          exportStatus === 'FAILED' || exportStatus === 'TIMEOUT'
            ? 'alert-danger'
            : 'alert-info'
        "
        role="alert"
      >
        <div class="d-flex align-items-center">
          @if (exportStatus === 'PENDING' || exportStatus === 'PROCESSING') {
            <div class="spinner-border spinner-border-sm me-2" role="status">
              <span class="visually-hidden">Loading...</span>
            </div>
          }
          @if (exportStatus === 'PENDING') {
            <span
              >Export request submitted. Preparing
              {{ entityType }} data...</span
            >
          } @else if (exportStatus === 'PROCESSING') {
            <span
              >Processing {{ entityType }} export. This may take a few
              moments...</span
            >
          } @else if (exportStatus === 'FAILED') {
            <span>
              <i class="fas fa-exclamation-triangle me-2"></i>Export failed.
              Please try again.
            </span>
          } @else if (exportStatus === 'TIMEOUT') {
            <span>
              <i class="fas fa-clock me-2"></i>Export is taking longer than
              expected. Please check back later.
            </span>
          }
          @if (
            exportJobId &&
            (exportStatus === 'FAILED' || exportStatus === 'TIMEOUT')
          ) {
            <button
              class="btn btn-sm ms-auto"
              (click)="onDownloadExport(exportJobId)"
            >
              <i class="fas fa-download"></i> Try Download
            </button>
          }
        </div>
      </div>
    }
  `,
})
export class ExportStatusAlertComponent {
  @Input() exportStatus: string | null = null;
  @Input() exportJobId: string | null = null;
  @Input() entityType: string = 'data';
  @Input() onDownloadExport: (jobId: string) => void = () => {};
}
