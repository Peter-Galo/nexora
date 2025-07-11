import { Injectable, signal, computed, inject } from '@angular/core';
import { DOCUMENT } from '@angular/common';

export interface PerformanceMetrics {
  loadTime: number;
  renderTime: number;
  memoryUsage: number;
  bundleSize: number;
  cacheHitRatio: number;
}

export interface LazyLoadConfig {
  threshold: number;
  rootMargin: string;
  enablePreload: boolean;
}

/**
 * Performance Optimization Service
 * Provides utilities for performance monitoring, lazy loading, and optimization
 */
@Injectable({
  providedIn: 'root'
})
export class PerformanceService {
  private document = inject(DOCUMENT);

  // Performance state signals
  private readonly _metrics = signal<PerformanceMetrics>({
    loadTime: 0,
    renderTime: 0,
    memoryUsage: 0,
    bundleSize: 0,
    cacheHitRatio: 0
  });

  private readonly _isOptimized = signal<boolean>(false);
  private readonly _lazyLoadConfig = signal<LazyLoadConfig>({
    threshold: 0.1,
    rootMargin: '50px',
    enablePreload: true
  });

  // Public readonly signals
  readonly metrics = this._metrics.asReadonly();
  readonly isOptimized = this._isOptimized.asReadonly();
  readonly lazyLoadConfig = this._lazyLoadConfig.asReadonly();

  // Computed performance indicators
  readonly performanceScore = computed(() => {
    const metrics = this._metrics();
    let score = 100;

    // Deduct points based on performance metrics
    if (metrics.loadTime > 3000) score -= 20;
    if (metrics.renderTime > 100) score -= 15;
    if (metrics.memoryUsage > 50) score -= 10;
    if (metrics.cacheHitRatio < 0.8) score -= 15;

    return Math.max(0, score);
  });

  readonly performanceGrade = computed(() => {
    const score = this.performanceScore();
    if (score >= 90) return 'A';
    if (score >= 80) return 'B';
    if (score >= 70) return 'C';
    if (score >= 60) return 'D';
    return 'F';
  });

  // Cache for lazy-loaded components
  private componentCache = new Map<string, any>();
  private imageCache = new Map<string, HTMLImageElement>();
  private intersectionObserver?: IntersectionObserver;

  constructor() {
    this.initializePerformanceMonitoring();
    this.setupIntersectionObserver();
  }

  /**
   * Initialize performance monitoring
   */
  private initializePerformanceMonitoring(): void {
    if (typeof window !== 'undefined' && 'performance' in window) {
      // Monitor page load performance
      window.addEventListener('load', () => {
        this.measureLoadTime();
        this.measureMemoryUsage();
      });

      // Monitor render performance
      this.measureRenderTime();
    }
  }

  /**
   * Measure page load time
   */
  private measureLoadTime(): void {
    const navigation = performance.getEntriesByType('navigation')[0] as PerformanceNavigationTiming;
    if (navigation) {
      const loadTime = navigation.loadEventEnd - navigation.fetchStart;
      this._metrics.update(metrics => ({ ...metrics, loadTime }));
    }
  }

  /**
   * Measure render time using Performance Observer
   */
  private measureRenderTime(): void {
    if ('PerformanceObserver' in window) {
      const observer = new PerformanceObserver((list) => {
        const entries = list.getEntries();
        const renderTime = entries.reduce((total, entry) => total + entry.duration, 0);
        this._metrics.update(metrics => ({ ...metrics, renderTime }));
      });

      observer.observe({ entryTypes: ['measure', 'paint'] });
    }
  }

  /**
   * Measure memory usage
   */
  private measureMemoryUsage(): void {
    if ('memory' in performance) {
      const memory = (performance as any).memory;
      const memoryUsage = (memory.usedJSHeapSize / memory.jsHeapSizeLimit) * 100;
      this._metrics.update(metrics => ({ ...metrics, memoryUsage }));
    }
  }

  /**
   * Setup Intersection Observer for lazy loading
   */
  private setupIntersectionObserver(): void {
    if ('IntersectionObserver' in window) {
      const config = this._lazyLoadConfig();
      this.intersectionObserver = new IntersectionObserver(
        (entries) => {
          entries.forEach(entry => {
            if (entry.isIntersecting) {
              this.handleIntersection(entry.target as HTMLElement);
            }
          });
        },
        {
          threshold: config.threshold,
          rootMargin: config.rootMargin
        }
      );
    }
  }

  /**
   * Handle intersection for lazy loading
   */
  private handleIntersection(element: HTMLElement): void {
    const src = element.getAttribute('data-src');
    const componentName = element.getAttribute('data-component');

    if (src && element.tagName === 'IMG') {
      this.lazyLoadImage(element as HTMLImageElement, src);
    } else if (componentName) {
      this.lazyLoadComponent(element, componentName);
    }

    this.intersectionObserver?.unobserve(element);
  }

  /**
   * Lazy load images
   */
  lazyLoadImage(img: HTMLImageElement, src: string): Promise<void> {
    return new Promise((resolve, reject) => {
      // Check cache first
      if (this.imageCache.has(src)) {
        const cachedImg = this.imageCache.get(src)!;
        img.src = cachedImg.src;
        img.classList.add('loaded');
        resolve();
        return;
      }

      // Create new image
      const newImg = new Image();
      newImg.onload = () => {
        img.src = src;
        img.classList.add('loaded');
        this.imageCache.set(src, newImg);
        resolve();
      };
      newImg.onerror = reject;
      newImg.src = src;
    });
  }

  /**
   * Lazy load Angular components
   */
  async lazyLoadComponent(element: HTMLElement, componentName: string): Promise<void> {
    try {
      // Check cache first
      if (this.componentCache.has(componentName)) {
        const component = this.componentCache.get(componentName);
        this.renderComponent(element, component);
        return;
      }

      // Dynamic import based on component name
      const componentModule = await this.importComponent(componentName);
      this.componentCache.set(componentName, componentModule);
      this.renderComponent(element, componentModule);
    } catch (error) {
      console.error(`Failed to lazy load component: ${componentName}`, error);
    }
  }

  /**
   * Dynamic component import
   */
  private async importComponent(componentName: string): Promise<any> {
    const componentMap: { [key: string]: () => Promise<any> } = {
      'stock-dashboard': () => import('../../components/inventory/stock/stock.component'),
      'product-list': () => import('../../components/inventory/product/product.component'),
      'warehouse-list': () => import('../../components/inventory/warehouse/warehouse.component'),
      // Add more components as needed
    };

    const importFn = componentMap[componentName];
    if (!importFn) {
      throw new Error(`Unknown component: ${componentName}`);
    }

    return await importFn();
  }

  /**
   * Render component in element
   */
  private renderComponent(element: HTMLElement, component: any): void {
    // This would typically involve Angular's dynamic component loading
    // Implementation depends on specific Angular version and requirements
    element.innerHTML = '<div>Component loaded</div>';
    element.classList.add('component-loaded');
  }

  /**
   * Register element for lazy loading
   */
  observeElement(element: HTMLElement): void {
    if (this.intersectionObserver) {
      this.intersectionObserver.observe(element);
    }
  }

  /**
   * Unregister element from lazy loading
   */
  unobserveElement(element: HTMLElement): void {
    if (this.intersectionObserver) {
      this.intersectionObserver.unobserve(element);
    }
  }

  /**
   * Preload critical resources
   */
  preloadCriticalResources(resources: string[]): void {
    resources.forEach(resource => {
      const link = this.document.createElement('link');
      link.rel = 'preload';
      link.href = resource;

      // Determine resource type
      if (resource.endsWith('.js')) {
        link.as = 'script';
      } else if (resource.endsWith('.css')) {
        link.as = 'style';
      } else if (resource.match(/\.(jpg|jpeg|png|webp|svg)$/)) {
        link.as = 'image';
      }

      this.document.head.appendChild(link);
    });
  }

  /**
   * Optimize images with WebP support
   */
  optimizeImage(src: string): string {
    if (this.supportsWebP()) {
      return src.replace(/\.(jpg|jpeg|png)$/, '.webp');
    }
    return src;
  }

  /**
   * Check WebP support
   */
  private supportsWebP(): boolean {
    const canvas = this.document.createElement('canvas');
    canvas.width = 1;
    canvas.height = 1;
    return canvas.toDataURL('image/webp').indexOf('data:image/webp') === 0;
  }

  /**
   * Debounce function for performance optimization
   */
  debounce<T extends (...args: any[]) => any>(
    func: T,
    delay: number
  ): (...args: Parameters<T>) => void {
    let timeoutId: ReturnType<typeof setTimeout>;
    return (...args: Parameters<T>) => {
      clearTimeout(timeoutId);
      timeoutId = setTimeout(() => func.apply(this, args), delay);
    };
  }

  /**
   * Throttle function for performance optimization
   */
  throttle<T extends (...args: any[]) => any>(
    func: T,
    limit: number
  ): (...args: Parameters<T>) => void {
    let inThrottle: boolean;
    return (...args: Parameters<T>) => {
      if (!inThrottle) {
        func.apply(this, args);
        inThrottle = true;
        setTimeout(() => inThrottle = false, limit);
      }
    };
  }

  /**
   * Memoization for expensive calculations
   */
  memoize<T extends (...args: any[]) => any>(func: T): T {
    const cache = new Map();
    return ((...args: Parameters<T>) => {
      const key = JSON.stringify(args);
      if (cache.has(key)) {
        return cache.get(key);
      }
      const result = func.apply(this, args);
      cache.set(key, result);
      return result;
    }) as T;
  }

  /**
   * Clear all caches
   */
  clearCaches(): void {
    this.componentCache.clear();
    this.imageCache.clear();
  }

  /**
   * Update lazy load configuration
   */
  updateLazyLoadConfig(config: Partial<LazyLoadConfig>): void {
    this._lazyLoadConfig.update(current => ({ ...current, ...config }));

    // Recreate intersection observer with new config
    if (this.intersectionObserver) {
      this.intersectionObserver.disconnect();
      this.setupIntersectionObserver();
    }
  }

  /**
   * Get performance recommendations
   */
  getPerformanceRecommendations(): string[] {
    const recommendations: string[] = [];
    const metrics = this._metrics();

    if (metrics.loadTime > 3000) {
      recommendations.push('Consider optimizing bundle size and using code splitting');
    }

    if (metrics.renderTime > 100) {
      recommendations.push('Optimize component rendering and use OnPush change detection');
    }

    if (metrics.memoryUsage > 50) {
      recommendations.push('Check for memory leaks and optimize data structures');
    }

    if (metrics.cacheHitRatio < 0.8) {
      recommendations.push('Improve caching strategy for better performance');
    }

    return recommendations;
  }

  /**
   * Export performance report
   */
  exportPerformanceReport(): any {
    return {
      timestamp: new Date().toISOString(),
      metrics: this._metrics(),
      score: this.performanceScore(),
      grade: this.performanceGrade(),
      recommendations: this.getPerformanceRecommendations(),
      cacheStats: {
        componentCacheSize: this.componentCache.size,
        imageCacheSize: this.imageCache.size
      }
    };
  }
}
