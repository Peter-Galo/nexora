import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import {
  Product,
  ProductAnalytics,
} from '../../components/inventory/models/inventory.models';
import { BaseInventoryService } from './base-inventory.service';

@Injectable({
  providedIn: 'root',
})
export class ProductService extends BaseInventoryService<Product> {
  protected readonly apiUrl = 'http://localhost:8080/api/v1/inventory/products';
  protected readonly entityName = 'product';

  // Alias methods for backward compatibility
  getAllProducts = () => this.getAll();
  getActiveProducts = () => this.getActive();
  getProductById = (id: string) => this.getById(id);
  getProductByCode = (code: string) => this.getByCode(code);
  searchProductsByName = (name: string) => this.searchByName(name);

  /**
   * Fetches products by category
   */
  getProductsByCategory(category: string): Observable<Product[]> {
    return this.getArray(`category/${category}`);
  }

  /**
   * Fetches products by brand
   */
  getProductsByBrand(brand: string): Observable<Product[]> {
    return this.getArray(`brand/${brand}`);
  }

  /**
   * Calculates product analytics from a list of products
   * @param products Array of products
   * @returns ProductAnalytics object
   */
  calculateProductAnalytics(products: Product[]): ProductAnalytics {
    const totalProducts = products.length;
    const activeProducts = products.filter((p) => p.active).length;
    const inactiveProducts = totalProducts - activeProducts;
    const totalValue = products.reduce((sum, p) => sum + p.price, 0);
    const averagePrice = totalProducts > 0 ? totalValue / totalProducts : 0;

    // Category breakdown
    const categoryBreakdown: Record<string, number> = {};
    products.forEach((p) => {
      categoryBreakdown[p.category] = (categoryBreakdown[p.category] || 0) + 1;
    });
    const categoriesCount = Object.keys(categoryBreakdown).length;

    // Brand breakdown
    const brandBreakdown: Record<string, number> = {};
    products.forEach((p) => {
      brandBreakdown[p.brand] = (brandBreakdown[p.brand] || 0) + 1;
    });
    const brandsCount = Object.keys(brandBreakdown).length;

    // Price ranges
    const priceRanges = {
      under100: products.filter((p) => p.price < 100).length,
      between100And500: products.filter((p) => p.price >= 100 && p.price < 500)
        .length,
      between500And1000: products.filter(
        (p) => p.price >= 500 && p.price < 1000,
      ).length,
      over1000: products.filter((p) => p.price >= 1000).length,
    };

    return {
      totalProducts,
      activeProducts,
      inactiveProducts,
      totalValue,
      averagePrice,
      categoriesCount,
      brandsCount,
      categoryBreakdown,
      brandBreakdown,
      priceRanges,
    };
  }

  /**
   * Gets product analytics by fetching all products and calculating analytics
   * @returns Observable with ProductAnalytics
   */
  getProductAnalytics(): Observable<ProductAnalytics> {
    return this.getAllProducts().pipe(
      map((products) => this.calculateProductAnalytics(products)),
    );
  }
}
