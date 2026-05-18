import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: 'auth',
    canActivate: [guestGuard],
    children: [
      { path: 'login', loadComponent: () => import('./features/auth/login.component').then(m => m.LoginComponent) },
      { path: 'register', loadComponent: () => import('./features/auth/register.component').then(m => m.RegisterComponent) },
      { path: '', redirectTo: 'login', pathMatch: 'full' }
    ]
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./layout/main-layout.component').then(m => m.MainLayoutComponent),
    children: [
      { path: 'dashboard', loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent) },
      { path: 'board', loadComponent: () => import('./features/board/board.component').then(m => m.BoardComponent) },
      { path: 'backlog', loadComponent: () => import('./features/backlog/backlog.component').then(m => m.BacklogComponent) },
      { path: 'sprints', loadComponent: () => import('./features/sprint/sprint.component').then(m => m.SprintComponent) },
      { path: 'ai', loadComponent: () => import('./features/ai/ai-assistant.component').then(m => m.AiAssistantComponent) },
      { path: 'search', loadComponent: () => import('./features/search/search.component').then(m => m.SearchComponent) },
      { path: 'profile', loadComponent: () => import('./features/profile/profile.component').then(m => m.ProfileComponent) },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },
  { path: '**', redirectTo: 'dashboard' }
];
