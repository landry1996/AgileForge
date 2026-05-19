# Angular Moderne — Tutoriel

## Ce qui a changé dans Angular 21

Angular a beaucoup évolué depuis les versions "classiques" (Angular 2-15). AgileForge utilise les conventions modernes d'Angular 21. Ce tutoriel explique les différences.

---

## 1. Standalone Components vs NgModules

### Avant (Angular < 15) : NgModules

```typescript
// ticket.module.ts — ANCIEN STYLE
@NgModule({
  declarations: [TicketListComponent, TicketDetailComponent],
  imports: [CommonModule, FormsModule, HttpClientModule],
  exports: [TicketListComponent]
})
export class TicketModule {}
```

### Maintenant (Angular 21) : Standalone Components

```typescript
// ticket-list.component.ts — STYLE AGILEFORGE
@Component({
  selector: 'app-ticket-list',
  standalone: true,              // ← Pas besoin de module
  imports: [CommonModule],       // ← Imports directement dans le composant
  template: `...`,
  styles: `...`
})
export class TicketListComponent { }
```

**Avantage** : Pas de fichiers `*.module.ts`, chaque composant est autonome et tree-shakable.

---

## 2. Signals vs Observables

### Observables (RxJS) — toujours utilisé pour HTTP

```typescript
// Les services HTTP retournent des Observables
@Injectable({ providedIn: 'root' })
export class TicketService {
  private http = inject(HttpClient);

  getTickets(projectId: string): Observable<Ticket[]> {
    return this.http.get<Ticket[]>(`${environment.apiUrl}/tickets/project/${projectId}`);
  }
}
```

### Signals — pour l'état local des composants

```typescript
@Component({
  selector: 'app-board',
  standalone: true,
  template: `
    @if (loading()) {
      <div class="spinner">Chargement...</div>
    } @else {
      <div class="board">
        @for (column of columns(); track column.id) {
          <app-board-column [column]="column" />
        }
      </div>
    }
  `
})
export class BoardComponent {
  private ticketService = inject(TicketService);

  // Signals = état réactif sans subscribe/unsubscribe
  loading = signal(true);
  tickets = signal<Ticket[]>([]);
  columns = computed(() => this.groupByStatus(this.tickets()));

  constructor() {
    this.loadTickets();
  }

  loadTickets() {
    this.ticketService.getByProject(this.projectId).subscribe(tickets => {
      this.tickets.set(tickets);   // ← Met à jour le signal
      this.loading.set(false);
    });
  }

  private groupByStatus(tickets: Ticket[]): Column[] {
    // computed() se recalcule automatiquement quand tickets() change
    // ...
  }
}
```

**Quand utiliser quoi :**

| Cas d'usage | Utiliser |
|-------------|----------|
| Appels HTTP | `Observable` (via HttpClient) |
| État local du composant | `signal()` |
| Valeur dérivée/calculée | `computed()` |
| Réaction à un changement | `effect()` |
| Données partagées entre composants | `signal()` dans un service |

---

## 3. inject() vs Constructor Injection

### Avant : injection par constructeur

```typescript
// ANCIEN STYLE
export class TicketListComponent {
  constructor(
    private ticketService: TicketService,
    private router: Router,
    private route: ActivatedRoute
  ) {}
}
```

### Maintenant : inject()

```typescript
// STYLE AGILEFORGE
export class TicketListComponent {
  private ticketService = inject(TicketService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
}
```

**Avantage** : Plus concis, pas besoin de constructeur explicite. Fonctionne aussi dans les fonctions (guards, interceptors).

---

## 4. Control Flow : @if/@for vs *ngIf/*ngFor

### Avant : directives structurelles

```html
<!-- ANCIEN STYLE -->
<div *ngIf="tickets.length > 0; else emptyState">
  <div *ngFor="let ticket of tickets; trackBy: trackById">
    {{ ticket.title }}
  </div>
</div>
<ng-template #emptyState>
  <p>Aucun ticket</p>
</ng-template>
```

### Maintenant : built-in control flow

```html
<!-- STYLE AGILEFORGE -->
@if (tickets().length > 0) {
  @for (ticket of tickets(); track ticket.id) {
    <div class="ticket-card">{{ ticket.title }}</div>
  } @empty {
    <p>Aucun ticket</p>
  }
} @else {
  <p>Chargement...</p>
}
```

**Avantages** :
- Syntaxe plus lisible et proche du JavaScript
- `@empty` intégré pour les listes vides
- `track` obligatoire (meilleure performance par défaut)
- Pas besoin d'importer `CommonModule` juste pour les directives

---

## 5. Routing avec Lazy Loading

```typescript
// app.routes.ts
export const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: 'login', loadComponent: () =>
      import('./components/login/login.component').then(m => m.LoginComponent) },
  { path: 'dashboard', loadComponent: () =>
      import('./components/dashboard/dashboard.component').then(m => m.DashboardComponent),
    canActivate: [authGuard] },
  { path: 'board/:projectId', loadComponent: () =>
      import('./components/board/board.component').then(m => m.BoardComponent),
    canActivate: [authGuard] },
  { path: 'tickets/:id', loadComponent: () =>
      import('./components/ticket-detail/ticket-detail.component').then(m => m.TicketDetailComponent),
    canActivate: [authGuard] },
  // ...
];
```

**Lazy loading** : Chaque composant est chargé à la demande. L'utilisateur ne télécharge que le code des pages qu'il visite.

---

## 6. Guards fonctionnels

### Avant : Guard sous forme de classe

```typescript
// ANCIEN STYLE
@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(): boolean {
    if (this.authService.isAuthenticated()) return true;
    this.router.navigate(['/login']);
    return false;
  }
}
```

### Maintenant : Guard sous forme de fonction

```typescript
// STYLE AGILEFORGE
export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated()) {
    return true;
  }
  return router.createUrlTree(['/login']);
};
```

**Avantage** : Plus simple, pas de classe à maintenir. `inject()` fonctionne dans les fonctions grâce au contexte d'injection.

---

## 7. Intercepteurs fonctionnels

```typescript
// auth.interceptor.ts
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = inject(AuthService).getToken();

  if (token) {
    req = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
  }

  return next(req);
};
```

Enregistrement dans `app.config.ts` :

```typescript
export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor])),
  ]
};
```

---

## 8. Services avec état réactif (Signal Store pattern)

```typescript
@Injectable({ providedIn: 'root' })
export class ProjectService {
  private http = inject(HttpClient);

  // État partagé via signals
  currentProject = signal<Project | null>(null);
  projects = signal<Project[]>([]);
  loading = signal(false);

  loadProjects() {
    this.loading.set(true);
    this.http.get<Project[]>(`${environment.apiUrl}/projects`).subscribe({
      next: (projects) => {
        this.projects.set(projects);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  selectProject(id: string) {
    const project = this.projects().find(p => p.id === id);
    this.currentProject.set(project ?? null);
  }
}
```

Les composants qui injectent ce service voient automatiquement les changements :

```typescript
@Component({
  template: `
    @if (projectService.currentProject(); as project) {
      <h1>{{ project.name }}</h1>
    }
  `
})
export class HeaderComponent {
  projectService = inject(ProjectService);
}
```

---

## 9. Inline Templates et Styles

AgileForge utilise des templates et styles inline pour les composants de taille moyenne :

```typescript
@Component({
  selector: 'app-ticket-card',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="card" [class.high-priority]="ticket().priority === 'HIGH'">
      <span class="key">{{ ticket().fullKey }}</span>
      <h3>{{ ticket().title }}</h3>
      <div class="meta">
        <span class="type">{{ ticket().type }}</span>
        <span class="points">{{ ticket().storyPoints }} pts</span>
      </div>
    </div>
  `,
  styles: `
    .card {
      background: #161b22;
      border: 1px solid #30363d;
      border-radius: 8px;
      padding: 12px;
      color: #c9d1d9;
    }
    .card.high-priority { border-left: 3px solid #f85149; }
    .key { color: #58a6ff; font-size: 12px; }
    .meta { display: flex; gap: 8px; margin-top: 8px; }
  `
})
export class TicketCardComponent {
  ticket = input.required<Ticket>();  // Signal input (Angular 17+)
}
```

**Quand utiliser inline vs fichiers séparés :**
- **Inline** : Template < 50 lignes, styles < 30 lignes
- **Fichiers séparés** : Composants complexes avec beaucoup de markup/CSS

---

## 10. Thème sombre AgileForge

Toute l'application utilise un thème sombre cohérent :

| Élément | Couleur | Usage |
|---------|---------|-------|
| Background | `#0d1117` | Fond de page |
| Cards | `#161b22` | Panneaux, cartes |
| Borders | `#30363d` | Séparations |
| Text | `#c9d1d9` | Texte principal |
| Text secondaire | `#8b949e` | Labels, metadata |
| Links/Accent | `#58a6ff` | Liens, actions |
| Success | `#3fb950` | Badges verts |
| Danger | `#f85149` | Erreurs, haute priorité |
| Warning | `#d29922` | Avertissements |

---

## Résumé des conventions AgileForge

| Aspect | Convention |
|--------|-----------|
| Components | Standalone, inline template/styles |
| State | Signals (`signal()`, `computed()`) |
| DI | `inject()` |
| Control flow | `@if`, `@for`, `@switch` |
| Routing | Lazy-loaded components |
| Guards | Fonctions (`CanActivateFn`) |
| Interceptors | Fonctions (`HttpInterceptorFn`) |
| HTTP | Services avec `Observable<T>` |
| Styling | Inline, thème sombre |
