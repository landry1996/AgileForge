# Guide de Déploiement

## Prérequis

- Docker 24+
- Docker Compose 2.x
- 4 Go RAM minimum
- 10 Go espace disque

---

## Architecture de déploiement

```
┌─────────────────────────────────────────────────────────┐
│                      Docker Host                         │
│                                                         │
│  ┌───────────┐  ┌───────────────┐  ┌───────────────┐  │
│  │  Nginx    │  │   Backend     │  │   Frontend    │  │
│  │  (proxy)  │──│  Spring Boot  │  │   (Nginx)     │  │
│  │  :80/:443 │  │    :8080      │  │    :4200      │  │
│  └─────┬─────┘  └───────┬───────┘  └───────────────┘  │
│        │                 │                              │
│  ┌─────▼─────────────────▼──────────────────────────┐  │
│  │              Docker Network                       │  │
│  └──────┬────────────┬────────────────┬─────────────┘  │
│         │            │                │                 │
│  ┌──────▼──────┐ ┌──▼──────────┐ ┌───▼──────────┐     │
│  │ PostgreSQL  │ │    Redis    │ │    Kafka     │     │
│  │    :5432    │ │   :6379     │ │   :9092      │     │
│  └─────────────┘ └─────────────┘ └──────────────┘     │
│                                                         │
│  Volumes: pgdata, redis-data                           │
└─────────────────────────────────────────────────────────┘
```

---

## Docker Compose

### `docker-compose.yml`

```yaml
version: '3.8'

services:
  # === Base de données ===
  postgres:
    image: postgres:16-alpine
    container_name: agileforge-postgres
    environment:
      POSTGRES_DB: agileforge
      POSTGRES_USER: agileforge
      POSTGRES_PASSWORD: ${DB_PASSWORD:-agileforge}
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U agileforge"]
      interval: 10s
      timeout: 5s
      retries: 5

  # === Cache ===
  redis:
    image: redis:7-alpine
    container_name: agileforge-redis
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5

  # === Message Broker ===
  kafka:
    image: confluentinc/cp-kafka:7.5.0
    container_name: agileforge-kafka
    environment:
      KAFKA_NODE_ID: 1
      KAFKA_PROCESS_ROLES: broker,controller
      KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9092,CONTROLLER://0.0.0.0:9093
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
      KAFKA_CONTROLLER_QUORUM_VOTERS: 1@kafka:9093
      KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
      CLUSTER_ID: 'agileforge-kafka-cluster'
    ports:
      - "9092:9092"

  # === Backend ===
  backend:
    build:
      context: ./agileforge-backend
      dockerfile: Dockerfile
    container_name: agileforge-backend
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DB_HOST: postgres
      DB_PORT: 5432
      DB_NAME: agileforge
      DB_USER: agileforge
      DB_PASSWORD: ${DB_PASSWORD:-agileforge}
      REDIS_HOST: redis
      REDIS_PORT: 6379
      JWT_SECRET: ${JWT_SECRET}
      SERVER_PORT: 8080
    ports:
      - "8080:8080"
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/api/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s

  # === Frontend ===
  frontend:
    build:
      context: ./agileforge-frontend
      dockerfile: Dockerfile
    container_name: agileforge-frontend
    ports:
      - "4200:80"
    depends_on:
      - backend

volumes:
  pgdata:
  redis-data:
```

---

## Dockerfiles

### Backend (`agileforge-backend/Dockerfile`)

```dockerfile
# Build stage
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Sécurité : ne pas tourner en root
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Frontend (`agileforge-frontend/Dockerfile`)

```dockerfile
# Build stage
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build -- --configuration=production

# Runtime stage
FROM nginx:alpine
COPY --from=build /app/dist/agileforge-frontend/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### Frontend Nginx (`agileforge-frontend/nginx.conf`)

```nginx
server {
    listen 80;
    server_name _;
    root /usr/share/nginx/html;
    index index.html;

    # Gzip compression
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml;
    gzip_min_length 256;

    # SPA routing fallback
    location / {
        try_files $uri $uri/ /index.html;
    }

    # Proxy API vers le backend
    location /api/ {
        proxy_pass http://backend:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # Cache des assets statiques
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff2?)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
```

---

## Variables d'environnement

### Obligatoires (production)

| Variable | Description | Exemple |
|----------|-------------|---------|
| `DB_PASSWORD` | Mot de passe PostgreSQL | `strong-random-password` |
| `JWT_SECRET` | Clé secrète JWT (256+ bits, base64) | `dGhpcyBpcyBhIHZlcnkgbG9uZyBzZWNyZXQga2V5...` |

### Optionnelles

| Variable | Défaut | Description |
|----------|--------|-------------|
| `DB_HOST` | `localhost` | Hôte PostgreSQL |
| `DB_PORT` | `5432` | Port PostgreSQL |
| `DB_NAME` | `agileforge` | Nom de la base |
| `DB_USER` | `agileforge` | Utilisateur BDD |
| `REDIS_HOST` | `localhost` | Hôte Redis |
| `REDIS_PORT` | `6379` | Port Redis |
| `SERVER_PORT` | `8080` | Port du backend |
| `SPRING_PROFILES_ACTIVE` | `default` | Profil Spring actif |

### Fichier `.env` (ne pas committer)

```env
DB_PASSWORD=my-secure-db-password
JWT_SECRET=bXktc3VwZXItc2VjcmV0LWtleS10aGF0LWlzLWF0LWxlYXN0LTI1Ni1iaXRzLWxvbmc=
```

---

## Commandes de déploiement

### Développement local

```bash
# Démarrer l'infrastructure
docker-compose up -d postgres redis

# Lancer le backend (hors Docker)
cd agileforge-backend
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Lancer le frontend (hors Docker)
cd agileforge-frontend
ng serve
```

### Production (tout en Docker)

```bash
# Build et démarrage
docker-compose up -d --build

# Vérifier les logs
docker-compose logs -f backend

# Vérifier la santé
docker-compose ps
curl http://localhost:8080/api/actuator/health
```

### Mise à jour

```bash
# Pull les dernières modifications
git pull origin main

# Rebuild et restart
docker-compose up -d --build backend frontend

# Vérifier les migrations Flyway (automatiques au démarrage)
docker-compose logs backend | grep "Flyway"
```

---

## CI/CD (GitHub Actions)

```yaml
# .github/workflows/deploy.yml
name: Build & Deploy

on:
  push:
    branches: [main]

jobs:
  test:
    runs-on: ubuntu-latest
    services:
      postgres:
        image: postgres:16-alpine
        env:
          POSTGRES_DB: agileforge_test
          POSTGRES_USER: test
          POSTGRES_PASSWORD: test
        ports: ['5432:5432']
        options: --health-cmd pg_isready --health-interval 10s --health-timeout 5s --health-retries 5
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { java-version: '21', distribution: 'temurin' }
      - run: mvn test -B
        working-directory: agileforge-backend
        env:
          DB_HOST: localhost
          DB_PORT: 5432

  build-and-push:
    needs: test
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: docker/login-action@v3
        with:
          registry: ghcr.io
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}
      - uses: docker/build-push-action@v5
        with:
          context: ./agileforge-backend
          push: true
          tags: ghcr.io/${{ github.repository }}/backend:latest
      - uses: docker/build-push-action@v5
        with:
          context: ./agileforge-frontend
          push: true
          tags: ghcr.io/${{ github.repository }}/frontend:latest
```

---

## Monitoring

### Health checks

```bash
# Backend health
curl http://localhost:8080/api/actuator/health

# PostgreSQL
docker exec agileforge-postgres pg_isready

# Redis
docker exec agileforge-redis redis-cli ping
```

### Logs

```bash
# Tous les services
docker-compose logs -f

# Backend uniquement
docker-compose logs -f backend

# 100 dernières lignes
docker-compose logs --tail=100 backend
```

---

## Sauvegarde et restauration

### PostgreSQL

```bash
# Sauvegarde
docker exec agileforge-postgres pg_dump -U agileforge agileforge > backup_$(date +%Y%m%d).sql

# Restauration
cat backup_20260519.sql | docker exec -i agileforge-postgres psql -U agileforge agileforge
```

### Volumes Docker

```bash
# Sauvegarde des volumes
docker run --rm -v agileforge_pgdata:/data -v $(pwd):/backup alpine tar czf /backup/pgdata.tar.gz /data
```
