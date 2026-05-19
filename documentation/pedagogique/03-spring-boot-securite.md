# Spring Boot + Sécurité JWT — Tutoriel

## Vue d'ensemble

AgileForge utilise **Spring Security 6** avec des **JSON Web Tokens (JWT)** pour sécuriser l'API REST. Ce tutoriel explique le fonctionnement complet de l'authentification.

---

## Comment fonctionne JWT ?

```
┌──────────┐          ┌──────────────┐          ┌──────────────┐
│  Client  │          │   Backend    │          │  PostgreSQL  │
└────┬─────┘          └──────┬───────┘          └──────┬───────┘
     │                       │                         │
     │  POST /auth/login     │                         │
     │  {email, password}    │                         │
     │──────────────────────>│                         │
     │                       │  SELECT * FROM users    │
     │                       │  WHERE email = ?        │
     │                       │────────────────────────>│
     │                       │                         │
     │                       │  User found             │
     │                       │<────────────────────────│
     │                       │                         │
     │                       │  Verify BCrypt password │
     │                       │  Generate JWT token     │
     │                       │                         │
     │  200 OK               │                         │
     │  {token, refreshToken}│                         │
     │<──────────────────────│                         │
     │                       │                         │
     │  GET /api/tickets     │                         │
     │  Authorization:       │                         │
     │  Bearer <token>       │                         │
     │──────────────────────>│                         │
     │                       │                         │
     │                       │  JwtFilter: validate    │
     │                       │  token, extract email   │
     │                       │                         │
     │  200 OK [tickets]     │                         │
     │<──────────────────────│                         │
```

---

## Structure du JWT

Un JWT est composé de 3 parties séparées par des points :

```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGVtYWlsLmNvbSIsImlhdCI6MTcxNjE1MDAwMCwiZXhwIjoxNzE2MjM2NDAwfQ.signature
\______________________/\__________________________________________________________________________________/\________/
       HEADER                                         PAYLOAD                                              SIGNATURE
```

- **Header** : Algorithme (HS256) et type (JWT)
- **Payload** : Données (email utilisateur, date d'expiration, rôles)
- **Signature** : Garantit que le token n'a pas été modifié

---

## Implémentation dans AgileForge

### 1. Configuration de sécurité (`SecurityConfig.java`)

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())  // API REST stateless, pas besoin de CSRF
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()       // Login/Register publics
                .requestMatchers("/swagger-ui/**").permitAll() // Doc API publique
                .requestMatchers("/actuator/health").permitAll()
                .anyRequest().authenticated()                  // Tout le reste nécessite un token
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        // ...
        return source;
    }
}
```

**Points clés :**
- `STATELESS` : Pas de session HTTP, chaque requête porte son token
- `csrf.disable()` : Sûr pour une API REST stateless (le CSRF exploite les cookies de session)
- Le filtre JWT est exécuté **avant** le filtre d'authentification standard

---

### 2. Filtre JWT (`JwtAuthenticationFilter.java`)

Ce filtre intercepte **chaque requête** pour vérifier le token :

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain chain) throws ServletException, IOException {

        // 1. Extraire le header Authorization
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);  // Pas de token → continuer sans auth
            return;
        }

        // 2. Extraire le token (après "Bearer ")
        String token = authHeader.substring(7);

        // 3. Extraire l'email du token
        String email = jwtService.extractUsername(token);

        // 4. Si l'email est valide et pas déjà authentifié
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // 5. Valider le token (signature + expiration)
            if (jwtService.isTokenValid(token, userDetails)) {
                // 6. Créer l'objet Authentication et le placer dans le contexte
                UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        chain.doFilter(request, response);
    }
}
```

---

### 3. Service JWT (`JwtService.java`)

```java
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expiration;  // 24h en millisecondes

    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())  // email
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
```

---

### 4. Contrôleur d'authentification (`AuthController.java`)

```java
@RestController
@RequestMapping("/auth")
public class AuthController {

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        // 1. Vérifier que l'email n'existe pas déjà
        // 2. Hasher le mot de passe avec BCrypt
        // 3. Sauvegarder l'utilisateur
        // 4. Générer un token JWT
        // 5. Retourner le token
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        // 1. Chercher l'utilisateur par email
        // 2. Vérifier le mot de passe avec BCrypt
        // 3. Générer access token + refresh token
        // 4. Retourner les tokens
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshTokenRequest request) {
        // 1. Valider le refresh token
        // 2. Générer un nouveau access token
        // 3. Retourner le nouveau token
    }
}
```

---

## Récupérer l'utilisateur courant dans un contrôleur

```java
@GetMapping("/my")
public ResponseEntity<List<TicketResponse>> getMyTickets(Authentication auth) {
    // auth.getName() retourne l'email (le "subject" du JWT)
    String email = auth.getName();
    UUID userId = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"))
            .getId();
    // ...
}
```

Spring Security injecte automatiquement l'objet `Authentication` grâce au contexte de sécurité rempli par le filtre JWT.

---

## Refresh Token — Pourquoi ?

| Token | Durée de vie | Utilisation |
|-------|-------------|-------------|
| Access Token | 24 heures | Envoyé avec chaque requête API |
| Refresh Token | 7 jours | Utilisé uniquement pour obtenir un nouveau access token |

**Pourquoi deux tokens ?**
- L'access token est court pour limiter les dégâts en cas de vol
- Le refresh token permet de rester connecté sans re-saisir le mot de passe
- Si le refresh token est compromis, l'utilisateur doit se reconnecter

---

## Côté Angular : Intercepteur HTTP

```typescript
// auth.interceptor.ts
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('token');

  if (token) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        // Token expiré → tenter un refresh ou rediriger vers login
        router.navigate(['/login']);
      }
      return throwError(() => error);
    })
  );
};
```

---

## Bonnes pratiques de sécurité appliquées

1. **Ne jamais stocker le mot de passe en clair** → BCrypt avec salt automatique
2. **Token stateless** → Pas de session serveur, scalabilité horizontale
3. **Secret JWT fort** → Minimum 256 bits, stocké en variable d'environnement
4. **CORS restrictif** → Seul `localhost:4200` autorisé en développement
5. **Validation des entrées** → `@Valid` + Jakarta Validation sur tous les DTOs
6. **Endpoints publics minimaux** → Seuls `/auth/**` et `/swagger-ui/**` sont ouverts
7. **Pas de données sensibles dans le JWT** → Seulement l'email et l'expiration

---

## Points d'attention

- **Ne jamais mettre le secret JWT dans le code source** → Utiliser des variables d'environnement
- **BCrypt est lent volontairement** → C'est une protection contre le brute-force
- **Le token est côté client** → Si un attaquant vole le token (XSS), il a accès. D'où l'importance de protéger contre XSS.
- **Logout** → Avec JWT stateless, on ne peut pas "invalider" un token serveur-side. Solutions : blacklist Redis, ou tokens courts + refresh.
