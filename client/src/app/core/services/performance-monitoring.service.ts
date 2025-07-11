import { Injectable, signal, computed } from '@angular/core';

export interface PerformanceMetrics {
  pageLoadTime: number;
  firstContentfulPaint: number;
  largestContentfulPaint: number;
  cumulativeLayoutShift: number;
  firstInputDelay: number;
  memoryUsage?: number;
  bundleSize?: number;
}

export interface RoutePerformance {
  route: string;
  loadTime: number;
  timestamp: number;
}

@Injectable({
  providedIn: 'root'
})
export class PerformanceMonitoringService {
  private metrics = signal<PerformanceMetrics>({
    pageLoadTime: 0,
    firstContentfulPaint: 0,
    largestContentfulPaint: 0,
    cumulativeLayoutShift: 0,
    firstInputDelay: 0
  });

  private routePerformance = signal<RoutePerformance[]>([]);
  private isMonitoring = signal<boolean>(false);

  // Computed properties for performance analysis
  readonly currentMetrics = computed(() => this.metrics());
  readonly averageRouteLoadTime = computed(() => {
    const routes = this.routePerformance();
    if (routes.length === 0) return 0;
    return routes.reduce((sum, route) => sum + route.loadTime, 0) / routes.length;
  });

  readonly performanceScore = computed(() => {
    const metrics = this.metrics();
    let score = 100;

    // Deduct points based on performance metrics
    if (metrics.firstContentfulPaint > 1800) score -= 20;
    else if (metrics.firstContentfulPaint > 1000) score -= 10;

    if (metrics.largestContentfulPaint > 2500) score -= 25;
    else if (metrics.largestContentfulPaint > 1500) score -= 15;

    if (metrics.cumulativeLayoutShift > 0.25) score -= 20;
    else if (metrics.cumulativeLayoutShift > 0.1) score -= 10;

    if (metrics.firstInputDelay > 300) score -= 20;
    else if (metrics.firstInputDelay > 100) score -= 10;

    return Math.max(0, score);
  });

  constructor() {
    this.initializePerformanceMonitoring();
  }

  /**
   * Initialize performance monitoring
   */
  private initializePerformanceMonitoring(): void {
    if (typeof window === 'undefined') return;

    this.isMonitoring.set(true);
    this.measureCoreWebVitals();
    this.measureMemoryUsage();
    this.setupNavigationTiming();
  }

  /**
   * Measure Core Web Vitals
   */
  private measureCoreWebVitals(): void {
    // First Contentful Paint
    this.measureFCP();

    // Largest Contentful Paint
    this.measureLCP();

    // Cumulative Layout Shift
    this.measureCLS();

    // First Input Delay
    this.measureFID();
  }

  private measureFCP(): void {
    const observer = new PerformanceObserver((list) => {
      const entries = list.getEntries();
      const fcpEntry = entries.find(entry => entry.name === 'first-contentful-paint');
      if (fcpEntry) {
        this.updateMetrics({ firstContentfulPaint: fcpEntry.startTime });
        observer.disconnect();
      }
    });
    observer.observe({ entryTypes: ['paint'] });
  }

  private measureLCP(): void {
    const observer = new PerformanceObserver((list) => {
      const entries = list.getEntries();
      const lastEntry = entries[entries.length - 1];
      this.updateMetrics({ largestContentfulPaint: lastEntry.startTime });
    });
    observer.observe({ entryTypes: ['largest-contentful-paint'] });
  }

  private measureCLS(): void {
    let clsValue = 0;
    const observer = new PerformanceObserver((list) => {
      for (const entry of list.getEntries()) {
        if (!(entry as any).hadRecentInput) {
          clsValue += (entry as any).value;
          this.updateMetrics({ cumulativeLayoutShift: clsValue });
        }
      }
    });
    observer.observe({ entryTypes: ['layout-shift'] });
  }

  private measureFID(): void {
    const observer = new PerformanceObserver((list) => {
      const entries = list.getEntries();
      const firstInput = entries[0];
      if (firstInput) {
        const fid = firstInput.processingStart - firstInput.startTime;
        this.updateMetrics({ firstInputDelay: fid });
        observer.disconnect();
      }
    });
    observer.observe({ entryTypes: ['first-input'] });
  }

  /**
   * Measure memory usage if available
   */
  private measureMemoryUsage(): void {
    if ('memory' in performance) {
      const memory = (performance as any).memory;
      this.updateMetrics({
        memoryUsage: memory.usedJSHeapSize / 1024 / 1024 // Convert to MB
      });
    }
  }

  /**
   * Setup navigation timing measurement
   */
  private setupNavigationTiming(): void {
    window.addEventListener('load', () => {
      const navigation = performance.getEntriesByType('navigation')[0] as PerformanceNavigationTiming;
      const pageLoadTime = navigation.loadEventEnd - navigation.navigationStart;
      this.updateMetrics({ pageLoadTime });
    });
  }

  /**
   * Track route performance
   */
  trackRoutePerformance(route: string, startTime: number): void {
    const loadTime = performance.now() - startTime;
    const routePerf: RoutePerformance = {
      route,
      loadTime,
      timestamp: Date.now()
    };

    this.routePerformance.update(routes => [...routes, routePerf]);
  }

  /**
   * Update performance metrics
   */
  private updateMetrics(newMetrics: Partial<PerformanceMetrics>): void {
    this.metrics.update(current => ({ ...current, ...newMetrics }));
  }

  /**
   * Get performance report
   */
  getPerformanceReport(): {
    metrics: PerformanceMetrics;
    score: number;
    averageRouteLoadTime: number;
    recommendations: string[];
  } {
    const metrics = this.currentMetrics();
    const score = this.performanceScore();
    const avgRouteTime = this.averageRouteLoadTime();

    const recommendations: string[] = [];

    if (metrics.firstContentfulPaint > 1800) {
      recommendations.push('Optimize First Contentful Paint - consider code splitting and resource optimization');
    }

    if (metrics.largestContentfulPaint > 2500) {
      recommendations.push('Optimize Largest Contentful Paint - optimize images and critical resources');
    }

    if (metrics.cumulativeLayoutShift > 0.25) {
      recommendations.push('Reduce Cumulative Layout Shift - ensure proper sizing for dynamic content');
    }

    if (metrics.firstInputDelay > 300) {
      recommendations.push('Improve First Input Delay - optimize JavaScript execution');
    }

    if (avgRouteTime > 1000) {
      recommendations.push('Optimize route loading times - consider lazy loading and preloading strategies');
    }

    return {
      metrics,
      score,
      averageRouteLoadTime: avgRouteTime,
      recommendations
    };
  }

  /**
   * Reset performance tracking
   */
  reset(): void {
    this.metrics.set({
      pageLoadTime: 0,
      firstContentfulPaint: 0,
      largestContentfulPaint: 0,
      cumulativeLayoutShift: 0,
      firstInputDelay: 0
    });
    this.routePerformance.set([]);
  }
}
