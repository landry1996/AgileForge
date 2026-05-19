# Documentation Backend

## Configuration Spring Boot

### Dépendances principales (pom.xml)

| Dépendance | Usage |
|-----------|-------|
| `spring-boot-starter-web` | API REST, Jackson JSON |
| `spring-boot-starter-data-jpa` | ORM Hibernate, Spring Data |
| `spring-boot-starter-security` | Authentification, autorisation |
| `spring-boot-starter-validation` | Jakarta Bean Validation |
| `spring-boot-starter-data-redis` | Cache Redis |
| `springdoc-openapi-starter-webmvc-ui` | Swagger UI / OpenAPI 3 |
| `flyway-core` + `flyway-database-postgresql` | Migrations BDD |
| `jjwt-api` + `jjwt-impl` + `jjwt-jackson` | Génération/validation JWT |
| `lombok` | Réduction boilerplate (entités JPA) |
| `postgresql` | Driver JDBC PostgreSQL |

### Profils Spring

| Profil | Fichier | Usage |
|--------|---------|-------|
| default | `application.yml` | Configuration commune |
| local | `application-local.yml` | Développement local |
| test | `application-test.yml` | Tests d'intégration |
| prod | `application-prod.yml` | Production |

### Configuration type (`application.yml`)

```yaml
spring:
  application:
    name: agileforge-backend
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:agileforge}
    username: ${DB_USER:agileforge}
    password: ${DB_PASSWORD:agileforge}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}

jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000       # 24h
  refresh-expiration: 604800000  # 7 jours

server:
  port: ${SERVER_PORT:8080}
  servlet:
    context-path: /api

springdoc:
  swagger-ui:
    path: /swagger-ui.html
  api-docs:
    path: /v3/api-docs
```

---

## Sécurité JWT

### Flux d'authentification

1. `POST /auth/register` → Crée l'utilisateur (BCrypt hash du mot de passe)
2. `POST /auth/login` → Vérifie les credentials, retourne access token + refresh token
3. Requêtes suivantes → Header `Authorization: Bearer <token>`
4. `POST /auth/refresh` → Renouvelle l'access token via le refresh token

### Configuration sécurité

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) {
    return http
        .csrf(csrf -> csrf.disable())
        .sessionManagement(s -> s.sessionCreationPolicy(STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/auth/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
}
```

### Endpoints publics

| Endpoint | Description |
|----------|-------------|
| `POST /auth/register` | Inscription |
| `POST /auth/login` | Connexion |
| `POST /auth/refresh` | Renouvellement token |
| `GET /swagger-ui/**` | Documentation API |
| `GET /v3/api-docs/**` | OpenAPI spec |
| `GET /actuator/health` | Health check |

---

## Services applicatifs

Chaque service suit le même pattern :

```java
@Service
@Transactional
public class XxxService {
    private final XxxRepositoryPort repository;

    // Méthodes de lecture : @Transactional(readOnly = true)
    // Méthodes d'écriture : @Transactional (par défaut via la classe)
}
```

### Liste des services

| Service | Responsabilité | Port(s) utilisé(s) |
|---------|---------------|---------------------|
| `AuthService` | Inscription, connexion, tokens | `UserRepositoryPort` |
| `TicketService` | CRUD tickets, transitions, commentaires | `TicketRepositoryPort`, `TicketCommentRepositoryPort`, `TicketHistoryRepositoryPort` |
| `ProjectService` | CRUD projets, membres | `ProjectRepositoryPort` |
| `SprintService` | CRUD sprints, burndown | `SprintRepositoryPort`, `TicketRepositoryPort` |
| `BoardService` | Colonnes du kanban | `BoardColumnRepositoryPort` |
| `NotificationService` | Notifications in-app | `NotificationRepositoryPort` |
| `SearchService` | Recherche full-text | `TicketRepositoryPort` |
| `AiAssistantService` | Suggestions IA | `TicketRepositoryPort` |
| `WorkflowService` | Règles de transition | `WorkflowRepositoryPort` |
| `LabelService` | Gestion des labels | `LabelRepositoryPort` |
| `AttachmentService` | Upload/download fichiers | `AttachmentRepositoryPort` |
| `TimeTrackingService` | Saisie de temps | `TimeEntryRepositoryPort` |
| `InvitationService` | Invitations par email | `InvitationRepositoryPort` |
| `SavedFilterService` | Filtres personnalisés | `SavedFilterRepositoryPort` |
| `ReleaseService` | Versions et changelogs | `ReleaseRepositoryPort` |
| `RoadmapService` | Jalons et dépendances | `RoadmapRepositoryPort` |
| `GitIntegrationService` | Branches, commits, PRs | `GitRepositoryPort` |
| `AnalyticsService` | Métriques, graphiques | Multiple ports |
| `DocumentService` | Wiki collaboratif | `DocumentRepositoryPort` |
| `KnowledgeBaseService` | Mémoire projet | `KnowledgeEntryRepositoryPort` |
| `OkrService` | Objectifs et résultats clés | `OkrRepositoryPort` |
| `DoraMetricsService` | Métriques DevOps | Multiple ports |
| `PredictionService` | Prévisions de livraison | `TicketRepositoryPort`, `SprintRepositoryPort` |
| `AuditService` | Trail d'audit | `AuditEventRepositoryPort` |
| `ClientPortalService` | Portail client | `ClientPortalRepositoryPort` |
| `PortfolioService` | Gestion multi-projets | `PortfolioRepositoryPort` |
| `CapacityPlanningService` | Planification capacité | `CapacityEntryRepositoryPort` |
| `IncidentService` | Gestion des incidents | `IncidentRepositoryPort` |
| `WebhookService` | Abonnements webhooks | `WebhookRepositoryPort` |
| `ApiKeyService` | Clés API externes | `ApiKeyRepositoryPort` |

---

## Repositories (Ports & Adapters)

### Pattern

```
Port (interface dans domain/)
    ↓ implémente
Adapter (classe dans infrastructure/persistence/adapter/)
    ↓ utilise
JpaRepository (interface Spring Data dans infrastructure/persistence/repository/)
    ↓ génère
Hibernate/JDBC → PostgreSQL
```

### Adapter type

```java
@Component
public class TicketRepositoryAdapter implements TicketRepositoryPort {
    private final JpaTicketRepository jpa;

    @Override
    public Ticket save(Ticket ticket) {
        TicketEntity entity = toEntity(ticket);
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID());
            entity.setCreatedAt(LocalDateTime.now());
        }
        entity.setUpdatedAt(LocalDateTime.now());
        return toDomain(jpa.save(entity));
    }

    @Override
    public Optional<Ticket> findById(UUID id) {
        return jpa.findById(id).map(this::toDomain);
    }

    // Conversions bidirectionnelles
    private Ticket toDomain(TicketEntity e) { /* ... */ }
    private TicketEntity toEntity(Ticket t) { /* ... */ }
}
```

---

## Gestion des erreurs

### Exceptions métier

| Exception | HTTP Status | Usage |
|-----------|-------------|-------|
| `EntityNotFoundException` | 404 | Entité non trouvée |
| `BusinessException` | 400 | Règle métier violée |
| `MethodArgumentNotValidException` | 400 | Validation DTO échouée |
| `AccessDeniedException` | 403 | Pas les droits |
| `AuthenticationException` | 401 | Token invalide/expiré |

### Handler global

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(404).body(new ErrorResponse(404, ex.getMessage()));
    }
    // ...
}
```

---

## Conventions de code

| Règle | Exemple |
|-------|---------|
| Domain models : pas de Lombok | Getters/setters explicites |
| JPA Entities : Lombok | `@Getter @Setter @NoArgsConstructor` |
| DTOs : Java records | `public record CreateTicketRequest(...)` |
| Services : injection par constructeur | Pas de `@Autowired` sur champs |
| Controllers : retournent `ResponseEntity<T>` | Contrôle du status code |
| Transactions : au niveau service | `@Transactional` sur la classe |
| Lectures : `@Transactional(readOnly = true)` | Optimisation Hibernate |
