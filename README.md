# AgileForge

**Plateforme Intelligente de Pilotage Projet & Delivery**

> De l'idée à la production, piloté par l'intelligence.

## Stack Technique

### Backend
- Java 21
- Spring Boot 3.3.x
- Spring Security 6 + JWT + OAuth2
- PostgreSQL 16
- Redis 7
- Apache Kafka
- Flyway
- MapStruct
- OpenAPI/Swagger

### Frontend
- Angular 18+
- Angular Material + Tailwind CSS

### Infrastructure
- Docker + Docker Compose
- Kubernetes (production)

## Prérequis

- Java 21+
- Maven 3.9+
- Docker & Docker Compose
- Node.js 20+ (pour le frontend)

## Démarrage rapide

### 1. Lancer l'infrastructure

```bash
docker-compose up -d
```

Cela démarre : PostgreSQL, Redis, Kafka, pgAdmin.

### 2. Configurer les secrets

```bash
cp agileforge-backend/src/main/resources/application-secret.example.yml \
   agileforge-backend/src/main/resources/application-secret.yml
```

Remplir les valeurs dans `application-secret.yml`.

### 3. Lancer le backend

```bash
cd agileforge-backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev,secret
```

### 4. Accéder à l'application

- API : http://localhost:8080/api
- Swagger : http://localhost:8080/api/swagger-ui.html
- pgAdmin : http://localhost:5050

## Architecture

```
agileforge-backend/
├── src/main/java/com/agileforge/
│   ├── domain/              # Logique métier pure (pas de dépendance Spring)
│   │   ├── model/           # Entités du domaine
│   │   ├── port/in/         # Ports entrants (use cases)
│   │   ├── port/out/        # Ports sortants (repositories, services externes)
│   │   ├── event/           # Événements du domaine
│   │   └── exception/       # Exceptions métier
│   ├── application/         # Orchestration (services applicatifs)
│   │   ├── service/         # Implémentation des use cases
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── mapper/          # MapStruct mappers
│   │   └── usecase/         # Interfaces use cases
│   └── infrastructure/      # Détails techniques
│       ├── persistence/     # JPA entities, repositories, adapters
│       ├── security/        # JWT, filters, auth
│       ├── config/          # Configuration Spring
│       ├── messaging/       # Kafka producers/consumers
│       └── web/             # Controllers, filters, exception handlers
```

## Conventions

- **Architecture** : Hexagonale (Ports & Adapters)
- **Tests** : JUnit 5 + Mockito + Testcontainers + ArchUnit
- **API** : REST, DTOs obligatoires, pas d'entités JPA exposées
- **Sécurité** : BCrypt, JWT signé, validation entrées, CORS configuré
- **Base de données** : Flyway migrations, pas de ddl-auto en prod

## Licence

Proprietary - AgileForge Team
