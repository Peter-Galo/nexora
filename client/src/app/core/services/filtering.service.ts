import { Injectable, signal, computed } from '@angular/core';

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

export interface PaginationState {
  page: number;
  pageSize: number;
  totalItems: number;
  totalPages: number;
}

/**
 * Modern Filtering Service using Angular Signals
 * Provides reusable filtering, searching, and pagination logic
 */
@Injectable({
  providedIn: 'root'
})
export class FilteringService {

  /**
   * Create a reactive filter state
   */
  createFilterState<T>(
    data: T[],
    config: FilterConfig<T>,
    initialState?: Partial<FilterState>
  ) {
    // Filter state signals
    const _allData = signal<T[]>(data);
    const _searchTerm = signal<string>(initialState?.searchTerm || '');
    const _filters = signal<{ [key: string]: any }>(initialState?.filters || {});
    const _sortBy = signal<string | undefined>(initialState?.sortBy);
    const _sortDirection = signal<'asc' | 'desc'>(initialState?.sortDirection || 'asc');
    const _showActiveOnly = signal<boolean>(initialState?.showActiveOnly || false);

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
      hasFilters: _searchTerm().trim() !== '' || Object.keys(_filters()).length > 0 || _showActiveOnly(),
      activeFilters: this.getActiveFilters(_searchTerm(), _filters(), _showActiveOnly())
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

      setFilter: (key: string, value: any) => {
        _filters.update(current => ({ ...current, [key]: value }));
      },

      removeFilter: (key: string) => {
        _filters.update(current => {
          const updated = { ...current };
          delete updated[key];
          return updated;
        });
      },

      clearFilters: () => {
        _searchTerm.set('');
        _filters.set({});
        _showActiveOnly.set(false);
      },

      setSorting: (field: string, direction: 'asc' | 'desc' = 'asc') => {
        _sortBy.set(field);
        _sortDirection.set(direction);
      },

      toggleSortDirection: () => {
        _sortDirection.update(current => current === 'asc' ? 'desc' : 'asc');
      },

      setShowActiveOnly: (show: boolean) => _showActiveOnly.set(show),

      toggleActiveOnly: () => _showActiveOnly.update(current => !current),

      // Utility methods
      getFilteredCount: () => filteredData().length,
      getTotalCount: () => _allData().length,
      hasActiveFilters: () => filterStats().hasFilters,
      getActiveFiltersList: () => filterStats().activeFilters
    };
  }

  /**
   * Create pagination state
   */
  createPaginationState(initialPageSize: number = 20) {
    const _page = signal<number>(0);
    const _pageSize = signal<number>(initialPageSize);
    const _totalItems = signal<number>(0);

    const totalPages = computed(() => Math.ceil(_totalItems() / _pageSize()));
    const hasNextPage = computed(() => _page() < totalPages() - 1);
    const hasPreviousPage = computed(() => _page() > 0);
    const startIndex = computed(() => _page() * _pageSize());
    const endIndex = computed(() => Math.min(startIndex() + _pageSize(), _totalItems()));

    return {
      // Readonly signals
      page: _page.asReadonly(),
      pageSize: _pageSize.asReadonly(),
      totalItems: _totalItems.asReadonly(),
      totalPages,
      hasNextPage,
      hasPreviousPage,
      startIndex,
      endIndex,

      // Methods
      setPage: (page: number) => _page.set(Math.max(0, Math.min(page, totalPages() - 1))),
      setPageSize: (size: number) => {
        _pageSize.set(size);
        _page.set(0); // Reset to first page
      },
      setTotalItems: (total: number) => _totalItems.set(total),
      nextPage: () => {
        if (hasNextPage()) _page.update(current => current + 1);
      },
      previousPage: () => {
        if (hasPreviousPage()) _page.update(current => current - 1);
      },
      firstPage: () => _page.set(0),
      lastPage: () => _page.set(totalPages() - 1),

      // Utility methods
      getPaginatedData: <T>(data: T[]) => {
        const start = startIndex();
        const end = endIndex();
        return data.slice(start, end);
      }
    };
  }

  /**
   * Apply search filter to data
   */
  private applySearch<T>(data: T[], searchTerm: string, searchFields: readonly (keyof T)[]): T[] {
    return data.filter(item => {
      return searchFields.some(field => {
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
    filterFields?: FilterConfig<T>['filterFields']
  ): T[] {
    return data.filter(item => {
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
            if (typeof value === 'object' && value.min !== undefined && value.max !== undefined) {
              return itemValue >= value.min && itemValue <= value.max;
            }
            return true;

          default:
            // Default string comparison
            return itemValue?.toString().toLowerCase().includes(value.toString().toLowerCase());
        }
      });
    });
  }

  /**
   * Apply sorting to data
   */
  private applySorting<T>(data: T[], sortBy: string, direction: 'asc' | 'desc'): T[] {
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
    showActiveOnly: boolean
  ): Array<{ key: string; value: any; label: string }> {
    const activeFilters: Array<{ key: string; value: any; label: string }> = [];

    if (searchTerm.trim()) {
      activeFilters.push({
        key: 'search',
        value: searchTerm,
        label: `Search: "${searchTerm}"`
      });
    }

    Object.entries(filters).forEach(([key, value]) => {
      if (value != null && value !== '' && value !== 'all') {
        activeFilters.push({
          key,
          value,
          label: `${this.formatFilterKey(key)}: ${value}`
        });
      }
    });

    if (showActiveOnly) {
      activeFilters.push({
        key: 'activeOnly',
        value: true,
        label: 'Active items only'
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
      .replace(/^./, str => str.toUpperCase())
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
        active: { type: 'boolean' as const }
      }
    },
    product: {
      searchFields: ['name', 'code', 'description', 'sku'] as const,
      filterFields: {
        category: { type: 'exact' as const },
        brand: { type: 'exact' as const },
        active: { type: 'boolean' as const },
        price: { type: 'range' as const }
      }
    },
    stock: {
      searchFields: ['product.name', 'product.code', 'warehouse.name'] as const,
      filterFields: {
        warehouse: { type: 'exact' as const },
        status: { type: 'exact' as const },
        quantity: { type: 'range' as const }
      }
    }
  };
}
