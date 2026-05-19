# Documentation Frontend

## Stack technique

| Technologie | Version | Usage |
|------------|---------|-------|
| Angular | 21.2.0 | Framework SPA |
| TypeScript | 5.x | Langage |
| RxJS | 7.x | Programmation réactive (HTTP) |
| Angular Signals | intégré | État réactif local |
| Angular Router | intégré | Navigation, lazy loading |
| SCSS/CSS | - | Styling (inline) |

---

## Architecture frontend

```
agileforge-frontend/
├── src/
│   ├── app/
│   │   ├── components/          # Composants standalone (pages + shared)
│   │   │   ├── login/
│   │   │   ├── register/
│   │   │   ├── dashboard/
│   │   │   ├── board/
│   │   │   ├── backlog/
│   │   │   ├── sprint/
│   │   │   ├── ticket-detail/
│   │   │   ├── ai-assistant/
│   │   │   ├── search/
│   │   │   ├── profile/
│   │   │   ├── sidebar/
│   │   │   ├── releases/
│   │   │   ├── roadmap/
│   │   │   ├── analytics/
│   │   │   ├── documents/
│   │   │   ├── prompt-generator/
│   │   │   ├── knowledge-base/
│   │   │   ├── time-tracking/
│   │   │   └── settings/
│   │   ├── services/            # Services HTTP
│   │   │   ├── auth.service.ts
│   │   │   ├── ticket.service.ts
│   │   │   ├── project.service.ts
│   │   │   ├── board.service.ts
│   │   │   ├── sprint.service.ts
│   │   │   ├── ai.service.ts
│   │   │   ├── notification.service.ts
│   │   │   ├── search.service.ts
│   │   │   ├── activity.service.ts
│   │   │   ├── profile.service.ts
│   │   │   ├── release.service.ts
│   │   │   ├── document.service.ts
│   │   │   ├── git-integration.service.ts
│   │   │   ├── analytics.service.ts
│   │   │   ├── knowledge-base.service.ts
│   │   │   ├── prompt-generator.service.ts
│   │   │   ├── time-tracking.service.ts
│   │   │   ├── label.service.ts
│   │   │   ├── workflow.service.ts
│   │   │   └── invitation.service.ts
│   │   ├── models/              # Interfaces TypeScript
│   │   │   ├── user.model.ts
│   │   │   ├── ticket.model.ts
│   │   │   ├── project.model.ts
│   │   │   ├── sprint.model.ts
│   │   │   ├── board.model.ts
│   │   │   ├── notification.model.ts
│   │   │   └── ...
│   │   ├── guards/
│   │   │   └── auth.guard.ts
│   │   ├── interceptors/
│   │   │   └── auth.interceptor.ts
│   │   ├── app.routes.ts
│   │   ├── app.component.ts
│   │   └── app.config.ts
│   ├── environments/
│   │   ├── environment.ts
│   │   └── environment.prod.ts
│   ├── index.html
│   └── styles.css               # Styles globaux
├── angular.json
├── package.json
└── tsconfig.json
```

---

## Composants

### Convention : Standalone + Inline

Tous les composants sont standalone (pas de NgModule) avec templates et styles inline :

```typescript
@Component({
  selector: 'app-component-name',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <!-- Template HTML ici -->
  `,
  styles: `
    /* Styles CSS ici */
  `
})
export class ComponentNameComponent {
  // Injection avec inject()
  private service = inject(SomeService);

  // État avec Signals
  data = signal<DataType[]>([]);
  loading = signal(true);
  selected = computed(() => this.data().find(d => d.id === this.selectedId()));
}
```

### Liste des composants (pages)

| Composant | Route | Description |
|-----------|-------|-------------|
| `LoginComponent` | `/login` | Formulaire de connexion |
| `RegisterComponent` | `/register` | Formulaire d'inscription |
| `DashboardComponent` | `/dashboard` | Vue d'ensemble, stats |
| `BoardComponent` | `/board/:projectId` | Kanban board |
| `BacklogComponent` | `/backlog/:projectId` | Liste des tickets |
| `SprintComponent` | `/sprint/:projectId` | Gestion des sprints |
| `TicketDetailComponent` | `/tickets/:id` | Détail d'un ticket |
| `AiAssistantComponent` | `/ai` | Chat IA, suggestions |
| `SearchComponent` | `/search` | Recherche globale |
| `ProfileComponent` | `/profile` | Profil utilisateur |
| `ReleasesComponent` | `/releases` | Gestion des versions |
| `RoadmapComponent` | `/roadmap` | Vue roadmap |
| `AnalyticsComponent` | `/analytics` | Tableaux de bord |
| `DocumentsComponent` | `/documents` | Wiki/documentation |
| `PromptGeneratorComponent` | `/prompts` | Générateur de prompts |
| `KnowledgeBaseComponent` | `/knowledge` | Base de connaissances |
| `TimeTrackingComponent` | `/time` | Suivi du temps |
| `SettingsComponent` | `/settings` | Paramètres |

---

## Services

### Pattern de service

```typescript
@Injectable({ providedIn: 'root' })
export class TicketService {
  private http = inject(HttpClient);
  private apiUrl = environment.apiUrl;

  getByProject(projectId: string): Observable<Ticket[]> {
    return this.http.get<Ticket[]>(`${this.apiUrl}/tickets/project/${projectId}`);
  }

  create(projectId: string, request: CreateTicketRequest): Observable<Ticket> {
    return this.http.post<Ticket>(`${this.apiUrl}/tickets/project/${projectId}`, request);
  }

  update(id: string, request: UpdateTicketRequest): Observable<Ticket> {
    return this.http.put<Ticket>(`${this.apiUrl}/tickets/${id}`, request);
  }

  transition(id: string, status: string): Observable<Ticket> {
    return this.http.patch<Ticket>(`${this.apiUrl}/tickets/${id}/transition/${status}`, {});
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/tickets/${id}`);
  }
}
```

---

## Routing

### Configuration (`app.routes.ts`)

```typescript
export const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: 'login', loadComponent: () =>
      import('./components/login/login.component').then(m => m.LoginComponent) },
  { path: 'register', loadComponent: () =>
      import('./components/register/register.component').then(m => m.RegisterComponent) },
  { path: 'dashboard', loadComponent: () =>
      import('./components/dashboard/dashboard.component').then(m => m.DashboardComponent),
    canActivate: [authGuard] },
  { path: 'board/:projectId', loadComponent: () =>
      import('./components/board/board.component').then(m => m.BoardComponent),
    canActivate: [authGuard] },
  // ... toutes les routes protégées avec authGuard
];
```

### Lazy Loading

Chaque route utilise `loadComponent` pour ne charger le code que quand l'utilisateur navigue vers cette page. Cela réduit le bundle initial.

---

## Guards

### Auth Guard

```typescript
export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated()) {
    return true;
  }
  return router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
};
```

---

## Intercepteurs

### Auth Interceptor

Ajoute automatiquement le token JWT à chaque requête HTTP :

```typescript
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();

  if (token && !req.url.includes('/auth/')) {
    req = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
  }

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        authService.logout();
        inject(Router).navigate(['/login']);
      }
      return throwError(() => error);
    })
  );
};
```

---

## Modèles TypeScript

### Exemple : Ticket

```typescript
export interface Ticket {
  id: string;
  projectId: string;
  fullKey: string;
  key: string;
  number: number;
  title: string;
  description?: string;
  type: 'STORY' | 'BUG' | 'TASK' | 'EPIC' | 'SUBTASK';
  status: 'BACKLOG' | 'TODO' | 'IN_PROGRESS' | 'IN_REVIEW' | 'TESTING' | 'DONE' | 'CANCELLED';
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  assigneeId?: string;
  reporterId: string;
  epicId?: string;
  parentId?: string;
  sprintId?: string;
  storyPoints?: number;
  estimatedHours?: number;
  loggedHours?: number;
  dueDate?: string;
  environment?: string;
  component?: string;
  labels?: string;
  qualityScore: number;
  createdAt: string;
  updatedAt: string;
}

export interface CreateTicketRequest {
  title: string;
  description?: string;
  type: string;
  priority?: string;
  assigneeId?: string;
  epicId?: string;
  parentId?: string;
  storyPoints?: number;
  estimatedHours?: number;
  dueDate?: string;
}
```

---

## Thème et styles

### Variables globales (`styles.css`)

```css
:root {
  --bg-primary: #0d1117;
  --bg-secondary: #161b22;
  --bg-tertiary: #21262d;
  --border: #30363d;
  --text-primary: #c9d1d9;
  --text-secondary: #8b949e;
  --accent: #58a6ff;
  --success: #3fb950;
  --danger: #f85149;
  --warning: #d29922;
}

body {
  background: var(--bg-primary);
  color: var(--text-primary);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
}
```

---

## Sidebar (navigation)

La sidebar est organisée en 4 groupes :

| Groupe | Items |
|--------|-------|
| **Planning** | Dashboard, Backlog, Board, Sprints, Roadmap |
| **Development** | Tickets, Releases, Git, AI Assistant |
| **Tools** | Analytics, Time Tracking, Documents, Knowledge Base |
| **Management** | Settings, Team, Prompts |

---

## Build et déploiement

```bash
# Développement
ng serve                           # http://localhost:4200

# Build production
ng build --configuration=production

# Le build génère dist/ avec des fichiers optimisés
# Servi par Nginx en production
```

### Configuration Nginx

```nginx
server {
    listen 80;
    root /usr/share/nginx/html;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;  # SPA fallback
    }

    location /api/ {
        proxy_pass http://backend:8080/api/;
    }
}
```
