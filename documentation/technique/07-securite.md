# Documentation Sécurité

## Vue d'ensemble

AgileForge implémente une sécurité en profondeur avec plusieurs couches de protection :

```
┌────────────────────────────────────────────────┐
│  1. CORS (origine autorisée)                    │
├────────────────────────────────────────────────┤
│  2. Rate Limiting (protection DDoS)             │
├────────────────────────────────────────────────┤
│  3. JWT Authentication Filter                   │
├────────────────────────────────────────────────┤
│  4. Authorization (RBAC)                        │
├────────────────────────────────────────────────┤
│  5. Input Validation (Jakarta Bean Validation)  │
├────────────────────────────────────────────────┤
│  6. SQL Injection Prevention (JPA/Prepared Stm) │
├────────────────────────────────────────────────┤
│  7. Audit Trail (traçabilité)                   │
└────────────────────────────────────────────────┘
```

---

## 1. Authentification JWT

### Mécanisme

| Étape | Action | Acteur |
|-------|--------|--------|
| 1 | L'utilisateur envoie email + password | Client |
| 2 | Le backend vérifie le hash BCrypt | AuthService |
| 3 | Le backend génère un JWT signé | JwtService |
| 4 | Le client stocke le token en localStorage | Angular |
| 5 | Chaque requête inclut `Authorization: Bearer <token>` | Interceptor |
| 6 | Le filtre JWT valide le token à chaque requête | JwtFilter |

### Structure du token

```
Header: { "alg": "HS256", "typ": "JWT" }
Payload: {
  "sub": "user@email.com",     // Subject (identifiant)
  "iat": 1716150000,           // Issued At
  "exp": 1716236400            // Expiration (24h après)
}
Signature: HMACSHA256(base64(header) + "." + base64(payload), secret)
```

### Configuration des tokens

| Paramètre | Valeur | Justification |
|-----------|--------|---------------|
| Algorithme | HS256 | Bon compromis performance/sécurité |
| Durée access token | 24h | Limite l'exposition en cas de vol |
| Durée refresh token | 7 jours | UX : pas de reconnexion quotidienne |
| Taille du secret | 256+ bits | Minimum pour HS256 |

### Stockage du secret

```yaml
# JAMAIS dans le code source
jwt:
  secret: ${JWT_SECRET}  # Variable d'environnement
```

---

## 2. Hashage des mots de passe

### BCrypt

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();  // 10 rounds par défaut
}

// Inscription
String hash = passwordEncoder.encode(rawPassword);
// → $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy

// Vérification
boolean valid = passwordEncoder.matches(rawPassword, storedHash);
```

**Pourquoi BCrypt ?**
- Salt unique intégré (protection contre rainbow tables)
- Coût configurable (10 rounds ≈ 100ms, suffisant pour ralentir le brute-force)
- Standard de l'industrie

---

## 3. CORS (Cross-Origin Resource Sharing)

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of(
        "http://localhost:4200",          // Dev
        "https://app.agileforge.com"     // Prod
    ));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
    config.setAllowCredentials(true);
    config.setMaxAge(3600L);  // Cache preflight 1h

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}
```

**Règles** :
- Seules les origines explicitement listées sont autorisées
- Pas de wildcard `*` en production
- `AllowCredentials(true)` pour envoyer les cookies/tokens

---

## 4. RBAC (Role-Based Access Control)

### Rôles

| Rôle | Permissions |
|------|-------------|
| `ADMIN` | Tout (gestion org, suppression, configuration) |
| `MANAGER` | Gestion projets, sprints, releases, membres |
| `DEVELOPER` | CRUD tickets, commentaires, log time, code |
| `VIEWER` | Lecture seule (board, tickets, analytics) |

### Matrice de permissions

| Action | ADMIN | MANAGER | DEVELOPER | VIEWER |
|--------|-------|---------|-----------|--------|
| Créer un projet | x | x | | |
| Supprimer un projet | x | | | |
| Créer un ticket | x | x | x | |
| Modifier un ticket | x | x | x | |
| Supprimer un ticket | x | x | | |
| Voir les tickets | x | x | x | x |
| Gérer les sprints | x | x | | |
| Gérer les releases | x | x | | |
| Inviter des membres | x | x | | |
| Configurer les workflows | x | x | | |
| Voir l'audit trail | x | | | |
| Gérer les API keys | x | x | | |
| Accéder au portail client | x | x | x | x |

### Vérification dans les services

```java
public void deleteTicket(UUID ticketId, UUID userId, String userRole) {
    if (!"ADMIN".equals(userRole) && !"MANAGER".equals(userRole)) {
        throw new AccessDeniedException("Insufficient permissions to delete tickets");
    }
    // ...
}
```

---

## 5. Validation des entrées

### Protection contre l'injection

| Vecteur d'attaque | Protection |
|-------------------|-----------|
| SQL Injection | JPA avec PreparedStatements (jamais de concaténation SQL) |
| XSS | Pas de rendu HTML côté serveur, JSON encoding automatique |
| Path Traversal | Validation des noms de fichiers dans les attachments |
| Command Injection | Pas d'exécution de commandes shell |
| Mass Assignment | DTOs explicites (seuls les champs déclarés sont acceptés) |

### Validation Jakarta

```java
public record CreateTicketRequest(
    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 500)
    String title,

    @Size(max = 10000)
    String description,

    @NotNull
    @Pattern(regexp = "STORY|BUG|TASK|EPIC|SUBTASK")
    String type,

    @Pattern(regexp = "LOW|MEDIUM|HIGH|CRITICAL")
    String priority
) {}
```

---

## 6. Protection CSRF

CSRF est **désactivé** car :
- L'API est stateless (pas de cookies de session)
- L'authentification se fait via le header `Authorization`
- Un attaquant ne peut pas forger ce header depuis un site tiers

```java
http.csrf(csrf -> csrf.disable());
```

Si l'application utilisait des cookies pour l'auth, CSRF serait obligatoire.

---

## 7. Headers de sécurité

```java
http.headers(headers -> headers
    .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
    .frameOptions(frame -> frame.deny())
    .httpStrictTransportSecurity(hsts -> hsts.maxAgeInSeconds(31536000))
);
```

| Header | Valeur | Protection |
|--------|--------|-----------|
| `X-Content-Type-Options` | `nosniff` | Empêche le MIME sniffing |
| `X-Frame-Options` | `DENY` | Empêche le clickjacking |
| `Strict-Transport-Security` | `max-age=31536000` | Force HTTPS |
| `Content-Security-Policy` | `default-src 'self'` | Limite les sources de contenu |

---

## 8. Audit Trail

Toutes les actions sensibles sont journalisées :

```java
@Service
public class AuditService {
    public void logEvent(UUID userId, AuditAction action, String resource,
                         UUID resourceId, String details, AuditSeverity severity) {
        AuditEvent event = new AuditEvent();
        event.setUserId(userId);
        event.setAction(action);
        event.setResource(resource);
        event.setResourceId(resourceId);
        event.setDetails(details);
        event.setSeverity(severity);
        event.setIpAddress(getCurrentIpAddress());
        event.setUserAgent(getCurrentUserAgent());
        auditRepository.save(event);
    }
}
```

### Actions auditées

| Action | Sévérité | Exemple |
|--------|----------|---------|
| LOGIN | INFO | Connexion réussie |
| LOGIN_FAILED | WARNING | Tentative échouée |
| CREATE | INFO | Création ticket/projet |
| DELETE | HIGH | Suppression d'une ressource |
| PERMISSION_CHANGE | HIGH | Changement de rôle |
| API_KEY_CREATED | HIGH | Nouvelle clé API |
| EXPORT | MEDIUM | Export de données |

---

## 9. API Keys

Pour l'accès programmatique (CI/CD, intégrations) :

```java
public class ApiKey {
    private UUID id;
    private UUID userId;
    private String name;
    private String keyHash;        // BCrypt hash de la clé
    private String keyPrefix;      // 8 premiers caractères (pour identification)
    private LocalDateTime expiresAt;
    private LocalDateTime lastUsedAt;
    private List<String> scopes;   // READ, WRITE, ADMIN
}
```

**Sécurité des API keys** :
- La clé complète n'est montrée qu'une fois (à la création)
- Stockée hashée en BDD (comme un mot de passe)
- Expiration configurable
- Scopes limités (principe du moindre privilège)
- Révocable à tout moment

---

## 10. Bonnes pratiques appliquées

| Pratique | Implémentation |
|----------|---------------|
| Principe du moindre privilège | Rôles avec permissions minimales |
| Defense in depth | Multiples couches de sécurité |
| Fail securely | Les erreurs retournent 401/403, pas de stack trace |
| Don't trust the client | Validation serveur-side obligatoire |
| Secrets hors du code | Variables d'environnement |
| Logging sécurité | Audit trail complet |
| Dépendances à jour | Versions récentes de Spring Security |

---

## 11. Checklist sécurité production

- [ ] JWT secret unique et fort (256+ bits)
- [ ] CORS configuré avec les domaines de production uniquement
- [ ] HTTPS activé (TLS 1.3)
- [ ] Rate limiting configuré
- [ ] Logs d'audit activés
- [ ] Sauvegardes chiffrées
- [ ] Variables d'environnement pour tous les secrets
- [ ] Pas de mode debug/Swagger en production
- [ ] Monitoring des connexions échouées
- [ ] Plan de rotation des secrets
