import { Routes } from '@angular/router';
import { authGuard } from './auth/guards/auth.guard';
import { LoginComponent } from './auth/components/login/login.component';
import { InventoryComponent } from './components/inventory/inventory.component';
import { RegisterComponent } from './auth/components/register/register.component';
import { HomeComponent } from './components/home/home.component';

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
  },
  { path: '', component: HomeComponent },
];
