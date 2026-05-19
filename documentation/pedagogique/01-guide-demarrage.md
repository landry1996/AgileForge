# Guide de Démarrage Rapide

## Prérequis

| Outil | Version minimale | Vérification |
|-------|-----------------|--------------|
| Java JDK | 21+ | `java --version` |
| Maven | 3.9+ | `mvn --version` |
| Node.js | 20+ | `node --version` |
| npm | 10+ | `npm --version` |
| Angular CLI | 21+ | `ng version` |
| PostgreSQL | 16+ | `psql --version` |
| Redis | 7+ | `redis-cli --version` |
| Docker | 24+ | `docker --version` |
| Git | 2.40+ | `git --version` |

---

## 1. Cloner le projet

```bash
git clone https://github.com/landry1996/AgileForge.git
cd AgileForge
```

---

## 2. Configuration de la base de données

### Option A : PostgreSQL local

```bash
# Créer la base de données
psql -U postgres -c "CREATE DATABASE agileforge;"
psql -U postgres -c "CREATE USER agileforge WITH PASSWORD 'agileforge';"
psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE agileforge TO agileforge;"
```

### Option B : Docker (recommandé)

```bash
docker run -d \
  --name agileforge-postgres \
  -e POSTGRES_DB=agileforge \
  -e POSTGRES_USER=agileforge \
  -e POSTGRES_PASSWORD=agileforge \
  -p 5432:5432 \
  postgres:16-alpine
```

### Redis

```bash
docker run -d \
  --name agileforge-redis \
  -p 6379:6379 \
  redis:7-alpine
```

---

## 3. Configuration du Backend

### Variables d'environnement

Créez le fichier `agileforge-backend/src/main/resources/application-local.yml` :

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/agileforge
    username: agileforge
    password: agileforge
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
  flyway:
    enabled: true
    locations: classpath:db/migration
  redis:
    host: localhost
    port: 6379

jwt:
  secret: votre-cle-secrete-de-minimum-256-bits-pour-hs256
  expiration: 86400000
  refresh-expiration: 604800000

server:
  port: 8080
```

### Lancer le backend

```bash
cd agileforge-backend

# Compiler le projet
mvn clean compile

# Lancer les tests
mvn test

# Démarrer l'application
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Le backend démarre sur `http://localhost:8080`.

### Vérifier le fonctionnement

```bash
# Swagger UI
curl http://localhost:8080/swagger-ui.html

# Health check
curl http://localhost:8080/actuator/health
```

---

## 4. Configuration du Frontend

```bash
cd agileforge-frontend

# Installer les dépendances
npm install

# Configurer l'URL de l'API
# Vérifier src/environments/environment.ts
# apiUrl doit pointer vers http://localhost:8080/api
```

### Fichier `environment.ts`

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
```

### Lancer le frontend

```bash
ng serve
```

L'application est accessible sur `http://localhost:4200`.

---

## 5. Premier lancement complet avec Docker Compose

Si vous préférez tout lancer d'un coup :

```bash
# À la racine du projet
docker-compose up -d
```

Cela démarre :
- PostgreSQL 16 (port 5432)
- Redis 7 (port 6379)
- Backend Spring Boot (port 8080)
- Frontend Angular (port 4200)

---

## 6. Premiers pas dans l'application

### Créer un compte

1. Ouvrir `http://localhost:4200`
2. Cliquer sur "S'inscrire"
3. Remplir : nom, email, mot de passe
4. Se connecter avec les identifiants créés

### Créer une organisation et un projet

1. Depuis le dashboard, cliquer "Nouvelle Organisation"
2. Donner un nom (ex: "Mon Entreprise")
3. Dans l'organisation, créer un projet (ex: "Mon Premier Projet")
4. Choisir une clé de projet (ex: "MPP")

### Créer un ticket

1. Aller dans le backlog du projet
2. Cliquer "Nouveau ticket"
3. Remplir : titre, type (Story/Bug/Task), priorité
4. Le ticket apparaît dans le backlog avec la clé "MPP-1"

---

## 7. Structure du projet

```
AgileForge/
├── agileforge-backend/          # API REST Spring Boot
│   ├── src/main/java/com/agileforge/
│   │   ├── domain/              # Coeur métier (modèles, ports)
│   │   ├── application/         # Services applicatifs, DTOs
│   │   └── infrastructure/      # Implémentations (JPA, Web, Security)
│   └── src/main/resources/
│       └── db/migration/        # Migrations Flyway (V1 à V20)
├── agileforge-frontend/         # SPA Angular
│   └── src/app/
│       ├── components/          # Composants standalone
│       ├── services/            # Services HTTP
│       ├── models/              # Interfaces TypeScript
│       └── guards/              # Guards de navigation
├── docker-compose.yml           # Orchestration Docker
└── documentation/               # Ce dossier
```

---

## 8. Commandes utiles

| Action | Commande |
|--------|----------|
| Compiler le backend | `mvn compile` |
| Tests backend | `mvn test` |
| Lancer backend | `mvn spring-boot:run` |
| Installer deps frontend | `npm install` |
| Lancer frontend | `ng serve` |
| Build production frontend | `ng build --configuration=production` |
| Créer migration Flyway | Ajouter `V{N}__description.sql` dans `db/migration/` |
| Voir logs Docker | `docker-compose logs -f` |

---

## Résolution de problèmes courants

### Le backend ne démarre pas

- **Port 8080 occupé** : `lsof -i :8080` puis `kill <PID>`
- **BDD non accessible** : Vérifier que PostgreSQL tourne sur le port 5432
- **Migration Flyway échoue** : Vérifier les fichiers SQL dans `db/migration/`

### Le frontend ne compile pas

- **Versions incompatibles** : Supprimer `node_modules/` et relancer `npm install`
- **Erreur CORS** : Vérifier la config CORS dans `SecurityConfig.java`

### Erreur d'authentification

- **Token expiré** : Le token JWT expire après 24h, se reconnecter
- **401 sur les requêtes** : Vérifier que le header `Authorization: Bearer <token>` est envoyé
