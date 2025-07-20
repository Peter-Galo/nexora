import { computed, Injectable, signal } from '@angular/core';

export interface FilterConfig<T> {
  searchFields: readonly (keyof T)[];
  filterFields?: {
    [K in keyof T]?: {
      type: 'exact' | 'range' | 'boolean' | 'array';
      transform?: (value: any) => any;
    };
  };
}

export interface FilterState {
  searchTerm: string;
  filters: { [key: string]: any };
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
  showActiveOnly?: boolean;
}

/**
 * Modern Filtering Service using Angular Signals
 * Provides reusable filtering, searching, and pagination logic
 */
@Injectable({
  providedIn: 'root',
})
export class FilteringService {
  /**
   * Create a reactive filter state
   */
  createFilterState<T>(
    data: T[],
    config: FilterConfig<T>,
    initialState?: Partial<FilterState>,
  ) {
    // Filter state signals
    const _allData = signal<T[]>(data);
    const _searchTerm = signal<string>(initialState?.searchTerm || '');
    const _filters = signal<{ [key: string]: any }>(
      initialState?.filters || {},
    );
    const _sortBy = signal<string | undefined>(initialState?.sortBy);
    const _sortDirection = signal<'asc' | 'desc'>(
      initialState?.sortDirection || 'asc',
    );
    const _showActiveOnly = signal<boolean>(
      initialState?.showActiveOnly || false,
    );

    // Computed filtered data
    const filteredData = computed(() => {
      let result = [..._allData()];

      // Apply search filter
      const searchTerm = _searchTerm().trim().toLowerCase();
      if (searchTerm) {
        result = this.applySearch(result, searchTerm, config.searchFields);
      }

      // Apply field filters
      const filters = _filters();
      if (Object.keys(filters).length > 0) {
        result = this.applyFilters(result, filters, config.filterFields);
      }

      // Apply active only filter
      if (_showActiveOnly() && this.hasActiveField(result)) {
        result = result.filter((item: any) => item.active === true);
      }

      // Apply sorting
      const sortBy = _sortBy();
      if (sortBy) {
        result = this.applySorting(result, sortBy, _sortDirection());
      }

      return result;
    });

    // Computed filter statistics
    const filterStats = computed(() => ({
      totalItems: _allData().length,
      filteredItems: filteredData().length,
      hasFilters:
        _searchTerm().trim() !== '' ||
        Object.keys(_filters()).length > 0 ||
        _showActiveOnly(),
      activeFilters: this.getActiveFilters(
        _searchTerm(),
        _filters(),
        _showActiveOnly(),
      ),
    }));

    return {
      // Readonly signals
      allData: _allData.asReadonly(),
      searchTerm: _searchTerm.asReadonly(),
      filters: _filters.asReadonly(),
      sortBy: _sortBy.asReadonly(),
      sortDirection: _sortDirection.asReadonly(),
      showActiveOnly: _showActiveOnly.asReadonly(),
      filteredData,
      filterStats,

      // Methods
      updateData: (newData: T[]) => _allData.set(newData),

      setSearchTerm: (term: string) => _searchTerm.set(term),

      toggleActiveOnly: () => _showActiveOnly.update((current) => !current),
    };
  }

  /**
   * Apply search filter to data
   */
  private applySearch<T>(
    data: T[],
    searchTerm: string,
    searchFields: readonly (keyof T)[],
  ): T[] {
    return data.filter((item) => {
      return searchFields.some((field) => {
        const value = item[field];
        if (value == null) return false;
        return value.toString().toLowerCase().includes(searchTerm);
      });
    });
  }

  /**
   * Apply field filters to data
   */
  private applyFilters<T>(
    data: T[],
    filters: { [key: string]: any },
    filterFields?: FilterConfig<T>['filterFields'],
  ): T[] {
    return data.filter((item) => {
      return Object.entries(filters).every(([key, value]) => {
        if (value == null || value === '' || value === 'all') return true;

        const fieldConfig = filterFields?.[key as keyof T];
        const itemValue = (item as any)[key];

        switch (fieldConfig?.type) {
          case 'exact':
            return itemValue === value;

          case 'boolean':
            return itemValue === (value === 'true' || value === true);

          case 'array':
            return Array.isArray(itemValue) ? itemValue.includes(value) : false;

          case 'range':
            if (
              typeof value === 'object' &&
              value.min !== undefined &&
              value.max !== undefined
            ) {
              return itemValue >= value.min && itemValue <= value.max;
            }
            return true;

          default:
            // Default string comparison
            return itemValue
              ?.toString()
              .toLowerCase()
              .includes(value.toString().toLowerCase());
        }
      });
    });
  }

  /**
   * Apply sorting to data
   */
  private applySorting<T>(
    data: T[],
    sortBy: string,
    direction: 'asc' | 'desc',
  ): T[] {
    return [...data].sort((a, b) => {
      const aValue = (a as any)[sortBy];
      const bValue = (b as any)[sortBy];

      let comparison = 0;

      if (aValue < bValue) comparison = -1;
      else if (aValue > bValue) comparison = 1;

      return direction === 'desc' ? -comparison : comparison;
    });
  }

  /**
   * Check if data has active field
   */
  private hasActiveField<T>(data: T[]): boolean {
    return data.length > 0 && 'active' in (data[0] as any);
  }

  /**
   * Get list of active filters for display
   */
  private getActiveFilters(
    searchTerm: string,
    filters: { [key: string]: any },
    showActiveOnly: boolean,
  ): Array<{ key: string; value: any; label: string }> {
    const activeFilters: Array<{ key: string; value: any; label: string }> = [];

    if (searchTerm.trim()) {
      activeFilters.push({
        key: 'search',
        value: searchTerm,
        label: `Search: "${searchTerm}"`,
      });
    }

    Object.entries(filters).forEach(([key, value]) => {
      if (value != null && value !== '' && value !== 'all') {
        activeFilters.push({
          key,
          value,
          label: `${this.formatFilterKey(key)}: ${value}`,
        });
      }
    });

    if (showActiveOnly) {
      activeFilters.push({
        key: 'activeOnly',
        value: true,
        label: 'Active items only',
      });
    }

    return activeFilters;
  }

  /**
   * Format filter key for display
   */
  private formatFilterKey(key: string): string {
    return key
      .replace(/([A-Z])/g, ' $1')
      .replace(/^./, (str) => str.toUpperCase())
      .trim();
  }

  /**
   * Common filter configurations
   */
  static readonly COMMON_CONFIGS = {
    warehouse: {
      searchFields: ['name', 'code', 'address', 'city', 'country'] as const,
      filterFields: {
        city: { type: 'exact' as const },
        country: { type: 'exact' as const },
        stateProvince: { type: 'exact' as const },
        active: { type: 'boolean' as const },
      },
    },
    product: {
      searchFields: ['name', 'code', 'description', 'sku'] as const,
      filterFields: {
        category: { type: 'exact' as const },
        brand: { type: 'exact' as const },
        active: { type: 'boolean' as const },
        price: { type: 'range' as const },
      },
    },
    stock: {
      searchFields: ['product.name', 'product.code', 'warehouse.name'] as const,
      filterFields: {
        warehouse: { type: 'exact' as const },
        status: { type: 'exact' as const },
        quantity: { type: 'range' as const },
      },
    },
  };
}
