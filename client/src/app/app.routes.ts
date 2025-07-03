import { HomeComponent } from './components/home/home.component';
import { Routes } from '@angular/router';
import { LoginComponent } from './auth/components/login/login.component';
import { RegisterComponent } from './auth/components/register/register.component';
import { InventoryComponent } from './components/inventory/inventory.component';
import { authGuard } from './auth/guards/auth.guard';
import { WarehouseComponent } from './components/inventory/warehouse/warehouse.component';
import { StockComponent } from './components/inventory/stock/stock.component';
import { ProductComponent } from './components/inventory/product/product.component';
import { AggregateComponent } from './components/inventory/aggregate/aggregate.component';

export const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent,
  },
  {
    path: 'register',
    component: RegisterComponent,
  },
  {
    path: 'inventory',
    component: InventoryComponent,
    canActivate: [authGuard],
    children: [
      { path: '', component: AggregateComponent },
      { path: 'product', component: ProductComponent },
      { path: 'stock', component: StockComponent },
      { path: 'warehouse', component: WarehouseComponent },
    ],
  },
  { path: '', component: HomeComponent },
];
