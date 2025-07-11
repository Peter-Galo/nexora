import { Routes } from '@angular/router';
import { authGuard } from './auth/guards/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./auth/components/login/login.component').then(m => m.LoginComponent),
  },
  {
    path: 'register',
    loadComponent: () => import('./auth/components/register/register.component').then(m => m.RegisterComponent),
  },
  {
    path: 'inventory',
    loadComponent: () => import('./components/inventory/inventory.component').then(m => m.InventoryComponent),
    canActivate: [authGuard],
    children: [
      {
        path: '',
        loadComponent: () => import('./components/inventory/aggregate/aggregate.component').then(m => m.AggregateComponent)
      },
      {
        path: 'product',
        loadComponent: () => import('./components/inventory/product/product.component').then(m => m.ProductComponent)
      },
      {
        path: 'stock',
        loadComponent: () => import('./components/inventory/stock/stock.component').then(m => m.StockComponent)
      },
      {
        path: 'warehouse',
        loadComponent: () => import('./components/inventory/warehouse/warehouse.component').then(m => m.WarehouseComponent)
      },
      {
        path: 'export-jobs',
        loadComponent: () => import('./components/inventory/export-jobs/export-jobs.component').then(m => m.ExportJobsComponent)
      },
    ],
  },
  {
    path: '',
    loadComponent: () => import('./components/home/home.component').then(m => m.HomeComponent)
  },
  {
    path: '404',
    loadComponent: () => import('./components/shared/not-found/not-found.component').then(m => m.NotFoundComponent)
  },
  {
    path: '**',
    redirectTo: '/404'
  },
];
