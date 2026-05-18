# AgileForge - TODO & Progression

## Statut Global
- **Phase actuelle** : MVP v1.0 - Foundation
- **Bloc en cours** : Bloc 8 - Frontend Angular ✅ TERMINE
- **Prochain bloc** : Bloc 9 - Intégration & Déploiement
- **Dernière mise à jour** : 2026-05-18
- **Build** : ✅ SUCCESS (Java 25 + Spring Boot 3.4.4 + Lombok 1.18.38)

---

## Tâches Terminées

- [x] Document de vision projet (AgileForge_Vision_Projet.txt)
- [x] Création structure répertoires (architecture hexagonale)
- [x] TODO.md initial
- [x] .gitignore complet
- [x] pom.xml avec toutes les dépendances (Spring Boot 3.3.5, Java 21)
- [x] AgileForgeApplication.java (main class)
- [x] application.yml (configuration principale)
- [x] application-dev.yml (profil développement)
- [x] application-test.yml (profil test avec Testcontainers)
- [x] application-secret.example.yml (template secrets)
- [x] docker-compose.yml (PostgreSQL, Redis, Kafka, pgAdmin)
- [x] Dockerfile backend
- [x] SecurityConfig (Spring Security 6, stateless, headers sécurité)
- [x] CorsConfig (configurable via properties)
- [x] OpenApiConfig (Swagger avec JWT bearer)
- [x] JpaAuditConfig (auditor aware)
- [x] GlobalExceptionHandler (toutes exceptions métier + validation)
- [x] Exceptions domaine (Business, NotFound, Unauthorized, Forbidden)
- [x] BaseEntity (UUID, audit fields, soft delete)
- [x] V1__initial_schema.sql (users, orgs, roles, permissions, audit, refresh tokens)
- [x] logback-spring.xml (rotation, profils, pas de secrets dans logs)
- [x] ArchitectureTest (ArchUnit - enforcement hexagonal)
- [x] README.md technique

---

## Tâches En Cours

_Aucune - Bloc 2 terminé_

---

## Bloc 2 - Authentification & IAM ✅ TERMINE

- [x] Modèle domaine User (domain/model/User.java)
- [x] Modèle domaine RefreshToken (domain/model/RefreshToken.java)
- [x] Ports entrants : RegisterUseCase, AuthenticationUseCase
- [x] Ports sortants : UserRepositoryPort, RefreshTokenRepositoryPort, PasswordEncoderPort
- [x] JPA Entity : UserEntity, RefreshTokenEntity
- [x] JPA Repository interfaces (JpaUserRepository, JpaRefreshTokenRepository)
- [x] Persistence Adapters (UserRepositoryAdapter, RefreshTokenRepositoryAdapter)
- [x] PasswordEncoderAdapter
- [x] MapStruct Mapper (UserMapper)
- [x] DTOs : RegisterRequest, LoginRequest, RefreshTokenRequest, AuthResponse, UserResponse
- [x] AuthService (application layer - register, login, refresh, logout, logoutAll)
- [x] JwtService (génération/validation tokens)
- [x] JwtAuthenticationFilter
- [x] CustomUserDetailsService
- [x] AuthController (register, login, refresh, logout, logout-all, me)
- [x] Tests unitaires AuthService (10 tests couvrant tous les cas)
- [x] SecurityConfig mis à jour avec JWT filter
- [x] Fix compatibilité Java 25 (Lombok 1.18.38, Spring Boot 3.4.4)
- [x] BUILD SUCCESS vérifié

---

## Bloc 3 - Organisations & Projets ✅ TERMINE

- [x] Modèle domaine Organization
- [x] Modèle domaine OrganizationMember
- [x] Modèle domaine Project (avec enums Type, Visibility, Status)
- [x] Modèle domaine ProjectMember
- [x] Ports sortants : OrganizationRepositoryPort, OrganizationMemberRepositoryPort, ProjectRepositoryPort, ProjectMemberRepositoryPort
- [x] JPA Entities : OrganizationEntity, OrganizationMemberEntity, ProjectEntity, ProjectMemberEntity, RoleEntity
- [x] JPA Repositories (6 interfaces)
- [x] Persistence Adapters (5 adapters)
- [x] DTOs : CreateOrganizationRequest, UpdateOrganizationRequest, CreateProjectRequest, UpdateProjectRequest, AddMemberRequest
- [x] DTOs Response : OrganizationResponse, ProjectResponse, MemberResponse
- [x] OrganizationService (create, getById, getBySlug, getByUserId, update, addMember, removeMember)
- [x] ProjectService (create, getById, getByOrganizationId, getByUserId, update, addMember, removeMember)
- [x] OrganizationController (7 endpoints)
- [x] ProjectController (7 endpoints)
- [x] Migration Flyway V2 (projects, project_members)
- [x] Vérification limites plan (max users, max projects)
- [x] Vérification membership avant actions
- [x] BUILD SUCCESS

---

## Prochains Blocs

### Bloc 4 - Tickets (Core) ✅ TERMINE
- [x] Modèle domaine : Ticket, TicketComment, TicketHistory
- [x] Enums : TicketType (15 types), TicketStatus (10 statuts), TicketPriority (5 niveaux)
- [x] Ports sortants : TicketRepositoryPort, TicketCommentRepositoryPort, TicketHistoryRepositoryPort
- [x] JPA Entities : TicketEntity, TicketCommentEntity, TicketHistoryEntity
- [x] JPA Repositories (3 interfaces)
- [x] Persistence Adapters (3 adapters)
- [x] DTOs : CreateTicketRequest, UpdateTicketRequest, CreateCommentRequest
- [x] DTOs Response : TicketResponse, CommentResponse, TicketHistoryResponse
- [x] TicketService (create, get, update, transition, comments, history, logTime, qualityScore)
- [x] TicketController (14 endpoints)
- [x] Migration Flyway V3 (tickets, comments, history, links + indexes)
- [x] Numérotation auto par projet (KEY-1, KEY-2...)
- [x] Score qualité auto-calculé
- [x] Historique complet de chaque changement
- [x] BUILD SUCCESS

### Bloc 5 - Board & Sprint ✅ TERMINE
- [x] Modèle domaine : Sprint (avec SprintStatus lifecycle), BoardColumn
- [x] Ports sortants : SprintRepositoryPort, BoardColumnRepositoryPort
- [x] JPA Entities : SprintEntity, BoardColumnEntity
- [x] JPA Repositories (JpaSprintRepository, JpaBoardColumnRepository)
- [x] Persistence Adapters (SprintRepositoryAdapter, BoardColumnRepositoryAdapter)
- [x] DTOs : CreateSprintRequest, SprintResponse, BoardResponse (with nested BoardColumnResponse)
- [x] SprintService (create, start, complete, addTicket, removeTicket, getMetrics + velocity)
- [x] BoardService (getBoard, addColumn, removeColumn, moveTicket, getBacklog, default columns)
- [x] SprintController (10 endpoints: CRUD + start/complete + tickets + metrics)
- [x] BoardController (5 endpoints: board view + columns + move + backlog)
- [x] Migration Flyway V4 (sprints, board_columns, FK tickets.sprint_id)
- [x] Sprint lifecycle (PLANNING → ACTIVE → COMPLETED/CANCELLED)
- [x] Unfinished tickets moved to backlog on sprint complete
- [x] WIP limit support on board columns
- [x] Auto-create default columns (Backlog, To Do, In Progress, Code Review, QA, Done)
- [x] BUILD SUCCESS

### Bloc 6 - IA Assistant ✅ TERMINE
- [x] Port sortant : AiAssistantPort (interface domaine pour l'IA)
- [x] Infrastructure adapter : ClaudeAiAdapter (appels Claude API via RestClient)
- [x] AiConfig (configuration externalisée: api-key, model, max-tokens, rate-limit)
- [x] AiAssistantService (orchestration avec validation + logging)
- [x] DTOs Request : GenerateTicketsRequest, GenerateBacklogRequest, AnalyzeQualityRequest, DecomposeTicketRequest, SuggestDescriptionRequest
- [x] DTOs Response : GeneratedTicketResponse, QualityAnalysisResponse, SuggestDescriptionResponse
- [x] AiAssistantController (5 endpoints)
- [x] POST /ai/generate-tickets — Génération de tickets depuis description naturelle
- [x] POST /ai/generate-backlog — Génération de backlog complet pour nouveau projet
- [x] POST /ai/analyze-quality — Analyse qualité ticket + suggestions d'amélioration
- [x] POST /ai/decompose — Décomposition ticket en sous-tâches
- [x] POST /ai/suggest-description — Génération description détaillée depuis titre
- [x] Modèle par défaut : claude-sonnet-4-20250514 (configurable)
- [x] Gestion gracieuse si API key absente (fallback vide, pas de crash)
- [x] Nettoyage réponses JSON (strip markdown code blocks)
- [x] Configuration dans application.yml + application-secret.example.yml
- [x] Architecture hexagonale respectée (port domaine / adapter infra)
- [x] BUILD SUCCESS

### Bloc 7 - Tests & Qualité ✅ TERMINE
- [x] Tests unitaires SprintService (13 tests : create, start, complete, add/remove tickets, metrics)
- [x] Tests unitaires BoardService (9 tests : getBoard, columns, moveTicket, backlog)
- [x] Tests unitaires AiAssistantService (12 tests : generate tickets/backlog, quality, decompose, suggest)
- [x] Tests d'intégration SprintController (6 tests MockMvc : CRUD + start/complete + metrics + 401)
- [x] Tests d'intégration BoardController (6 tests MockMvc : board view + columns + move + backlog + 401)
- [x] Tests d'intégration AiAssistantController (7 tests MockMvc : 5 endpoints + validation 400 + 401)
- [x] Fix compatibilité Java 25 + Mockito 5.18.0 + ByteBuddy 1.17.5
- [x] Fix ArchUnit failOnEmptyShould pour Java 25
- [x] Fix @EnableJpaAuditing déplacé vers JpaAuditConfig (WebMvcTest compatible)
- [x] maven-surefire-plugin configuré avec --add-opens pour Java 25
- [x] Total : 71 tests, 0 failures, 0 errors
- [x] BUILD SUCCESS

### Bloc 8 - Frontend Angular ✅ TERMINE
- [x] Projet Angular 19+ initialisé (standalone components, signals, new control flow)
- [x] Architecture : core/ (models, services, guards, interceptors), features/, layout/, shared/
- [x] Modèles TypeScript : User, Ticket, Sprint, Board, Project, Organization, AI models
- [x] Services HTTP : AuthService, TicketService, SprintService, BoardService, ProjectService, AiService
- [x] Auth guard (authGuard + guestGuard) functional style
- [x] JWT interceptor (authInterceptor) functional style
- [x] Layout principal : SidebarComponent + MainLayoutComponent (dark theme GitHub-like)
- [x] Auth pages : LoginComponent, RegisterComponent (reactive forms + validation)
- [x] DashboardComponent (stats cards, sections)
- [x] BoardComponent (Kanban avec drag & drop natif HTML5, colonnes dynamiques)
- [x] BacklogComponent (liste tickets avec type/priority badges)
- [x] SprintComponent (cards avec progress bars, statuts)
- [x] AiAssistantComponent (4 tabs : generate, backlog, quality, decompose)
- [x] Routing lazy-loaded avec guards
- [x] Environment files (dev: localhost:8080/api, prod: /api)
- [x] Global SCSS (dark theme, scrollbar custom)
- [x] BUILD SUCCESS (ng build)

### Bloc 9 - Intégration & Déploiement
- [ ] Proxy config Angular → Backend (dev)
- [ ] Docker Compose full stack (backend + frontend + DB)
- [ ] CI/CD pipeline (GitHub Actions)
- [ ] Push initial vers GitHub

---

## Décisions Techniques

| ID | Décision | Statut | Date |
|----|----------|--------|------|
| ADR-001 | Architecture hexagonale (ports & adapters) | Accepté | 2026-05-18 |
| ADR-002 | Monolithe modulaire pour MVP, microservices plus tard | Accepté | 2026-05-18 |
| ADR-003 | PostgreSQL + Flyway migrations | Accepté | 2026-05-18 |
| ADR-004 | JWT + Refresh Token (stateless, pas de session) | Accepté | 2026-05-18 |
| ADR-005 | MapStruct pour mapping DTO/Entity/Domain | Accepté | 2026-05-18 |
| ADR-006 | Spring Events en V1, Kafka en V2 | Accepté | 2026-05-18 |
| ADR-007 | Multi-tenant par colonne (organization_id) | Accepté | 2026-05-18 |
| ADR-008 | UUID comme clé primaire partout | Accepté | 2026-05-18 |
| ADR-009 | Soft delete (is_deleted) sur entités principales | Accepté | 2026-05-18 |
| ADR-010 | BCrypt strength 12 pour hash passwords | Accepté | 2026-05-18 |

---

## Risques Identifiés

| Risque | Impact | Probabilité | Mitigation |
|--------|--------|-------------|------------|
| Complexité architecture hexagonale pour MVP | Moyen | Faible | Commencer simple, ajouter couches si besoin |
| Performance multi-tenant sur gros volumes | Élevé | Moyen | Index tenant_id, partitioning si nécessaire |
| Intégration IA coûteuse en tokens | Moyen | Élevé | Rate limiting, cache, modèles légers pour triage |

---

## Bugs Détectés

_Aucun pour le moment (projet en initialisation)_

---

## Fichiers Créés (Bloc 1)

```
AgileForge/
├── .gitignore
├── README.md
├── TODO.md
├── docker-compose.yml
├── AgileForge_Vision_Projet.txt
├── docs/
│   ├── architecture/
│   └── adr/
└── agileforge-backend/
    ├── Dockerfile
    ├── pom.xml
    └── src/
        ├── main/
        │   ├── java/com/agileforge/
        │   │   ├── AgileForgeApplication.java
        │   │   ├── domain/
        │   │   │   ├── model/
        │   │   │   ├── port/in/
        │   │   │   ├── port/out/
        │   │   │   ├── event/
        │   │   │   └── exception/
        │   │   │       ├── BusinessException.java
        │   │   │       ├── EntityNotFoundException.java
        │   │   │       ├── UnauthorizedException.java
        │   │   │       └── ForbiddenException.java
        │   │   ├── application/
        │   │   │   ├── service/
        │   │   │   ├── dto/request/
        │   │   │   ├── dto/response/
        │   │   │   ├── mapper/
        │   │   │   └── usecase/
        │   │   └── infrastructure/
        │   │       ├── config/
        │   │       │   ├── CorsConfig.java
        │   │       │   ├── SecurityConfig.java
        │   │       │   ├── OpenApiConfig.java
        │   │       │   └── JpaAuditConfig.java
        │   │       ├── persistence/
        │   │       │   ├── entity/BaseEntity.java
        │   │       │   ├── repository/
        │   │       │   └── adapter/
        │   │       ├── security/
        │   │       ├── messaging/
        │   │       └── web/
        │   │           ├── controller/
        │   │           ├── filter/
        │   │           └── advice/GlobalExceptionHandler.java
        │   └── resources/
        │       ├── application.yml
        │       ├── application-dev.yml
        │       ├── application-test.yml
        │       ├── application-secret.example.yml
        │       ├── logback-spring.xml
        │       └── db/migration/V1__initial_schema.sql
        └── test/
            ├── java/com/agileforge/ArchitectureTest.java
            └── resources/application-test.yml
```

---

## Notes

- Le frontend Angular sera initialisé dans un bloc séparé
- Kafka configuré dans docker-compose mais utilisé seulement à partir de V2
- Les secrets ne doivent JAMAIS être commités
- Prochain bloc : Authentification complète (JWT + Refresh + RBAC)
