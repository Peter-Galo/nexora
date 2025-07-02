import { Routes } from '@angular/router';
import { authGuard } from './auth/guards/auth.guard';
import { LoginComponent } from './auth/components/login/login.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';

export const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent,
  },
  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [authGuard],
  },

  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
];
