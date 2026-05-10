import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: 'login', loadComponent: () => import('./features/auth/login.component').then(m => m.LoginComponent) },
  { path: 'register', loadComponent: () => import('./features/auth/register.component').then(m => m.RegisterComponent) },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent)
  },
  {
    path: 'games',
    canActivate: [authGuard],
    loadComponent: () => import('./features/games/games-list.component').then(m => m.GamesListComponent)
  },
  {
    path: 'games/:id',
    canActivate: [authGuard],
    loadComponent: () => import('./features/games/game-detail.component').then(m => m.GameDetailComponent)
  },
  {
    path: 'customers',
    canActivate: [authGuard],
    loadComponent: () => import('./features/customers/customers-list.component').then(m => m.CustomersListComponent)
  },
  {
    path: 'customers/:id',
    canActivate: [authGuard],
    loadComponent: () => import('./features/customers/customer-detail.component').then(m => m.CustomerDetailComponent)
  },
  {
    path: 'rentals',
    canActivate: [authGuard],
    loadComponent: () => import('./features/rentals/rentals-list.component').then(m => m.RentalsListComponent)
  },
  {
    path: 'rentals/new',
    canActivate: [authGuard],
    loadComponent: () => import('./features/rentals/create-rental.component').then(m => m.CreateRentalComponent)
  },
  {
    path: 'dictionaries',
    canActivate: [authGuard],
    loadComponent: () => import('./features/dictionaries/dictionaries.component').then(m => m.DictionariesComponent)
  },
  { path: '**', redirectTo: '' }
];
