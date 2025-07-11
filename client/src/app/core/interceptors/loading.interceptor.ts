import { inject } from '@angular/core';
import { HttpInterceptorFn } from '@angular/common/http';
import { finalize } from 'rxjs/operators';
import { AppStateService } from '../state/app-state.service';

// Global counter for active requests
let activeRequests = 0;

export const LoadingInterceptor: HttpInterceptorFn = (req, next) => {
  const appStateService = inject(AppStateService);

  // Skip loading indicator for certain requests (like polling or background requests)
  if (req.headers.has('X-Skip-Loading')) {
    return next(req);
  }

  // Increment active requests counter
  activeRequests++;
  updateLoadingState(appStateService);

  return next(req).pipe(
    finalize(() => {
      // Decrement active requests counter
      activeRequests--;
      updateLoadingState(appStateService);
    })
  );
};

function updateLoadingState(appStateService: AppStateService): void {
  // Update global loading state based on active requests
  appStateService.setLoading(activeRequests > 0);
}
