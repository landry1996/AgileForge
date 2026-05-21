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
      { path: 'releases', loadComponent: () => import('./features/releases/releases.component').then(m => m.ReleasesComponent) },
      { path: 'roadmap', loadComponent: () => import('./features/roadmap/roadmap.component').then(m => m.RoadmapComponent) },
      { path: 'analytics', loadComponent: () => import('./features/analytics/analytics.component').then(m => m.AnalyticsComponent) },
      { path: 'documents', loadComponent: () => import('./features/documents/documents.component').then(m => m.DocumentsComponent) },
      { path: 'prompts', loadComponent: () => import('./features/prompts/prompt-generator.component').then(m => m.PromptGeneratorComponent) },
      { path: 'knowledge', loadComponent: () => import('./features/knowledge/knowledge-base.component').then(m => m.KnowledgeBaseComponent) },
      { path: 'time', loadComponent: () => import('./features/time-tracking/time-tracking.component').then(m => m.TimeTrackingComponent) },
      { path: 'settings', loadComponent: () => import('./features/settings/settings.component').then(m => m.SettingsComponent) },
      { path: 'tickets/:id', loadComponent: () => import('./features/ticket-detail/ticket-detail.component').then(m => m.TicketDetailComponent) },
      { path: 'search', loadComponent: () => import('./features/search/search.component').then(m => m.SearchComponent) },
      { path: 'profile', loadComponent: () => import('./features/profile/profile.component').then(m => m.ProfileComponent) },
      { path: 'audit', loadComponent: () => import('./features/audit/audit.component').then(m => m.AuditComponent) },
      { path: 'portfolio', loadComponent: () => import('./features/portfolio/portfolio.component').then(m => m.PortfolioComponent) },
      { path: 'capacity', loadComponent: () => import('./features/capacity/capacity.component').then(m => m.CapacityComponent) },
      { path: 'incidents', loadComponent: () => import('./features/incidents/incidents.component').then(m => m.IncidentsComponent) },
      { path: 'webhooks', loadComponent: () => import('./features/webhooks/webhooks.component').then(m => m.WebhooksComponent) },
      { path: 'api-keys', loadComponent: () => import('./features/api-keys/api-keys.component').then(m => m.ApiKeysComponent) },
      { path: 'client-portal', loadComponent: () => import('./features/client-portal/client-portal.component').then(m => m.ClientPortalComponent) },
      { path: 'gamification', loadComponent: () => import('./features/gamification/gamification.component').then(m => m.GamificationComponent) },
      { path: 'ai-agents', loadComponent: () => import('./features/ai-agents/ai-agents.component').then(m => m.AiAgentsComponent) },
      { path: 'integrations', loadComponent: () => import('./features/integrations/integrations.component').then(m => m.IntegrationsComponent) },
      { path: 'marketplace', loadComponent: () => import('./features/marketplace/marketplace.component').then(m => m.MarketplaceComponent) },
      { path: 'collaboration', loadComponent: () => import('./features/collaboration/collaboration.component').then(m => m.CollaborationComponent) },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },
  { path: '**', redirectTo: 'dashboard' }
];
