import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter, withPreloading, PreloadAllModules } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';

import { routes } from './app.routes';
import { AuthInterceptor } from './auth/interceptors/auth.interceptor';
import { LoadingInterceptor } from './core/interceptors/loading.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    // Optimize zone change detection for better performance
    provideZoneChangeDetection({
      eventCoalescing: true,
      runCoalescing: true
    }),
    // Add preloading strategy for lazy-loaded routes
    provideRouter(
      routes,
      withPreloading(PreloadAllModules)
    ),
    provideHttpClient(withInterceptors([LoadingInterceptor, AuthInterceptor]))
  ]
};
