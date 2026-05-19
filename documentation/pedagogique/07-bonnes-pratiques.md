# Bonnes Pratiques du Projet

## 1. Conventions de nommage

### Java (Backend)

| Élément | Convention | Exemple |
|---------|-----------|---------|
| Packages | lowercase, singulier | `com.agileforge.domain.model` |
| Classes | PascalCase | `TicketService`, `CreateTicketRequest` |
| Interfaces (Ports) | PascalCase + suffixe `Port` | `TicketRepositoryPort` |
| Adapters | PascalCase + suffixe `Adapter` | `TicketRepositoryAdapter` |
| Entities JPA | PascalCase + suffixe `Entity` | `TicketEntity` |
| Controllers | PascalCase + suffixe `Controller` | `TicketController` |
| Méthodes | camelCase, verbe | `getById()`, `createTicket()` |
| Constants | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT` |
| DTOs request | `Create/Update` + Nom + `Request` | `CreateTicketRequest` |
| DTOs response | Nom + `Response` | `TicketResponse` |

### TypeScript (Frontend)

| Élément | Convention | Exemple |
|---------|-----------|---------|
| Fichiers | kebab-case | `ticket-detail.component.ts` |
| Classes | PascalCase | `TicketDetailComponent` |
| Services | PascalCase + `Service` | `TicketService` |
| Interfaces/Models | PascalCase | `Ticket`, `CreateTicketRequest` |
| Signals/Variables | camelCase | `loading`, `currentProject` |
| Fonctions | camelCase | `loadTickets()`, `onSubmit()` |

### SQL (Migrations Flyway)

| Élément | Convention | Exemple |
|---------|-----------|---------|
| Tables | snake_case, pluriel | `tickets`, `board_columns` |
| Colonnes | snake_case | `project_id`, `created_at` |
| Fichiers migration | `V{N}__{description}.sql` | `V3__tickets_comments_history.sql` |
| Index | `idx_{table}_{columns}` | `idx_tickets_project_id` |
| Contraintes FK | `fk_{table}_{ref_table}` | `fk_tickets_project` |

---

## 2. Structure des packages

```
com.agileforge/
├── domain/                     # INDÉPENDANT de tout framework
│   ├── model/                  # Objets métier (Ticket, Project, Sprint)
│   │   ├── Ticket.java
│   │   ├── TicketStatus.java   # Enums métier
│   │   └── TicketType.java
│   ├── port/
│   │   └── out/                # Interfaces vers l'extérieur
│   │       └── TicketRepositoryPort.java
│   └── exception/              # Exceptions métier
│       ├── BusinessException.java
│       └── EntityNotFoundException.java
│
├── application/                # Orchestration, use cases
│   ├── service/                # Services applicatifs
│   │   └── TicketService.java
│   └── dto/
│       ├── request/            # Entrées validées
│       │   └── CreateTicketRequest.java
│       └── response/           # Sorties formatées
│           └── TicketResponse.java
│
└── infrastructure/             # Détails techniques
    ├── persistence/
    │   ├── entity/             # Entités JPA
    │   │   └── TicketEntity.java
    │   ├── adapter/            # Implémentation des ports
    │   │   └── TicketRepositoryAdapter.java
    │   └── repository/         # Interfaces Spring Data
    │       └── JpaTicketRepository.java
    ├── web/
    │   └── controller/         # Endpoints REST
    │       └── TicketController.java
    └── security/               # JWT, filtres, config
        ├── JwtService.java
        └── SecurityConfig.java
```

**Règles** :
- `domain` n'importe JAMAIS de classes de `infrastructure` ou `application`
- `application` importe `domain` mais pas `infrastructure`
- `infrastructure` peut importer `domain` et `application`

---

## 3. Gestion des erreurs

### Exceptions métier

```java
// Exception générique pour les règles métier violées
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}

// Exception pour les entités non trouvées
public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String entity, UUID id) {
        super(entity + " not found with id: " + id);
    }
}
```

### Handler global (traduit les exceptions en réponses HTTP)

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(404, ex.getMessage()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(400, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(400, message));
    }
}
```

### Principes

| Faire | Ne pas faire |
|-------|-------------|
| Lever `BusinessException` pour les règles métier | Retourner null |
| Lever `EntityNotFoundException` pour les 404 | Retourner `Optional.empty()` au controller |
| Valider avec `@Valid` sur les DTOs | Valider manuellement chaque champ |
| Utiliser `@RestControllerAdvice` centralisé | `try/catch` dans chaque controller |

---

## 4. Logging

### Configuration

```java
// Utiliser SLF4J (via Logback inclus dans Spring Boot)
private static final Logger log = LoggerFactory.getLogger(TicketService.class);
```

### Niveaux de log

| Niveau | Usage | Exemple |
|--------|-------|---------|
| `ERROR` | Erreur critique, action requise | `log.error("Payment failed for order {}", orderId, exception)` |
| `WARN` | Situation anormale mais gérée | `log.warn("Retry attempt {} for webhook {}", attempt, url)` |
| `INFO` | Événement métier important | `log.info("Ticket created: {}-{}", key, number)` |
| `DEBUG` | Détail technique pour diagnostic | `log.debug("Query returned {} results", count)` |

### Bonnes pratiques

```java
// BON : paramètres avec {}
log.info("Ticket {}-{} transitioned: {} -> {}", key, number, oldStatus, newStatus);

// MAUVAIS : concaténation (évaluée même si le log est désactivé)
log.info("Ticket " + key + "-" + number + " transitioned");

// BON : inclure l'exception en dernier argument
log.error("Failed to send webhook to {}", url, exception);

// MAUVAIS : logger des données sensibles
log.info("User logged in with password: {}", password); // JAMAIS !
```

---

## 5. Validation des entrées

### Jakarta Validation sur les DTOs

```java
public record CreateTicketRequest(
    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 500, message = "Title must be between 5 and 500 characters")
    String title,

    @Size(max = 10000, message = "Description too long")
    String description,

    @NotNull(message = "Type is required")
    String type,

    @Pattern(regexp = "LOW|MEDIUM|HIGH|CRITICAL", message = "Invalid priority")
    String priority
) {}
```

### Activation dans le contrôleur

```java
@PostMapping
public ResponseEntity<TicketResponse> create(
    @Valid @RequestBody CreateTicketRequest request) {  // @Valid active la validation
    // Si la validation échoue, Spring retourne automatiquement 400
}
```

### Validation métier (dans le service)

```java
public Ticket transition(UUID id, TicketStatus newStatus, UUID userId) {
    Ticket ticket = getById(id);
    if (ticket.getStatus() == newStatus) {
        throw new BusinessException("Ticket is already in status: " + newStatus);
    }
    // La validation de format est sur le DTO, la validation métier est ici
}
```

---

## 6. Documentation API (OpenAPI/Swagger)

### Annotations sur les contrôleurs

```java
@RestController
@RequestMapping("/tickets")
@Tag(name = "Tickets", description = "Ticket management endpoints")
public class TicketController {

    @PostMapping("/project/{projectId}")
    @Operation(summary = "Create a new ticket",
               description = "Creates a ticket in the specified project")
    @ApiResponse(responseCode = "201", description = "Ticket created")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "404", description = "Project not found")
    public ResponseEntity<TicketResponse> create(...) { }
}
```

### Accès à la documentation

- **Swagger UI** : `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON** : `http://localhost:8080/v3/api-docs`

---

## 7. Gestion des migrations Flyway

### Règles absolues

1. **Ne JAMAIS modifier une migration existante** — Flyway vérifie le checksum
2. **Toujours ajouter une nouvelle migration** — V21, V22, etc.
3. **Tester la migration sur une copie** avant de la déployer
4. **Migrations idempotentes** quand possible (IF NOT EXISTS)

### Structure d'une migration

```sql
-- V21__add_ticket_resolution.sql

-- Ajouter une colonne
ALTER TABLE tickets ADD COLUMN resolution VARCHAR(50);
ALTER TABLE tickets ADD COLUMN resolved_at TIMESTAMP;

-- Créer un index
CREATE INDEX idx_tickets_resolution ON tickets(resolution);

-- Données par défaut pour les tickets existants
UPDATE tickets SET resolution = 'UNRESOLVED' WHERE status != 'DONE';
UPDATE tickets SET resolution = 'FIXED' WHERE status = 'DONE';
```

### Nommage

```
V{numéro}__{description_en_snake_case}.sql
```

Le double underscore `__` est obligatoire entre le numéro et la description.

---

## 8. Git Workflow

### Branches

| Branche | Rôle | Qui push |
|---------|------|----------|
| `main` | Production | Merge depuis feature branches |
| `feature/XXX` | Nouvelle fonctionnalité | Développeur |
| `fix/XXX` | Correction de bug | Développeur |
| `hotfix/XXX` | Correction urgente prod | Lead dev |

### Commits

Format : `type: description courte`

```
feat: add time tracking to tickets
fix: resolve null pointer in sprint calculation
refactor: extract ticket validation into separate method
docs: add API documentation for webhooks
test: add integration tests for auth flow
chore: update Spring Boot to 3.4.5
```

### Workflow type

```
1. git checkout -b feature/my-feature
2. ... développement ...
3. git add <fichiers spécifiques>
4. git commit -m "feat: implement my feature"
5. git push -u origin feature/my-feature
6. Créer une Pull Request sur GitHub
7. Code review + CI verts
8. Merge dans main
```

---

## 9. Sécurité — Règles de base

| Règle | Application |
|-------|-------------|
| Ne jamais committer de secrets | `.gitignore` exclut `*.key`, `*.pem`, `*-secret.yml` |
| Hasher les mots de passe | BCrypt dans `AuthService` |
| Valider toutes les entrées | `@Valid` sur chaque DTO |
| Échapper les sorties | Spring Boot le fait par défaut (JSON) |
| Limiter les accès | `@PreAuthorize` ou vérification dans le service |
| Tokens courts | Access token 24h, refresh token 7j |
| CORS restrictif | Seules les origines connues sont autorisées |

---

## 10. Checklist avant commit

- [ ] Le code compile (`mvn compile`)
- [ ] Les tests passent (`mvn test`)
- [ ] Pas de `System.out.println` (utiliser le logger)
- [ ] Pas de code commenté laissé en place
- [ ] Les DTOs ont la validation Jakarta
- [ ] Les nouveaux endpoints sont documentés (Swagger)
- [ ] La migration Flyway est numérotée correctement
- [ ] Pas de secrets dans le code
- [ ] Les imports inutilisés sont supprimés
