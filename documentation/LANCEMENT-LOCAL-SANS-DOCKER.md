# Lancement Local SANS Docker

Guide pas à pas pour lancer AgileForge en local directement sur votre machine (sans conteneurisation).

---

## Prérequis à installer

| Logiciel | Version | Téléchargement |
|----------|---------|----------------|
| Java JDK | 21+ | https://adoptium.net/ |
| Maven | 3.9+ | https://maven.apache.org/download.cgi |
| Node.js | 22+ | https://nodejs.org/ |
| PostgreSQL | 16+ | https://www.postgresql.org/download/ |
| Redis | 7+ | https://redis.io/download/ (ou via WSL sur Windows) |
| Git | 2.40+ | https://git-scm.com/ |

### Vérification des prérequis

```bash
java --version          # → openjdk 21.x.x
mvn --version           # → Apache Maven 3.9.x
node --version          # → v22.x.x
npm --version           # → 10.x.x
psql --version          # → psql 16.x
redis-cli --version     # → redis-cli 7.x.x
git --version           # → git version 2.4x.x
```

---

## Étape 1 : Cloner le projet

```bash
git clone https://github.com/landry1996/AgileForge.git
cd AgileForge
```

---

## Étape 2 : Configurer PostgreSQL

### Créer la base de données

```bash
# Se connecter en tant que superuser
psql -U postgres

# Dans le shell psql :
CREATE DATABASE agileforge_dev;
CREATE USER agileforge WITH PASSWORD 'agileforge_dev';
GRANT ALL PRIVILEGES ON DATABASE agileforge_dev TO agileforge;
ALTER USER agileforge CREATEDB;
\c agileforge_dev
GRANT ALL ON SCHEMA public TO agileforge;
\q
```

### Vérifier la connexion

```bash
psql -U agileforge -d agileforge_dev -h localhost
# Si ça se connecte, c'est bon. Tapez \q pour quitter.
```

> **Windows** : Si PostgreSQL est installé via l'installeur, il est accessible sur `localhost:5432`.
> Vérifiez que le service "postgresql-x64-16" est démarré dans les Services Windows.

---

## Étape 3 : Démarrer Redis

### Sur Linux/macOS

```bash
redis-server
```

### Sur Windows (via WSL)

```bash
wsl
sudo service redis-server start
redis-cli ping  # → PONG
```

### Sur Windows (natif, via Memurai ou Redis pour Windows)

Télécharger Memurai (https://www.memurai.com/) ou utiliser `winget install Redis.Redis`.

```bash
# Vérifier que Redis répond
redis-cli ping
# → PONG
```

---

## Étape 4 : Configurer le backend

### Créer le fichier de secrets

```bash
cd agileforge-backend/src/main/resources
cp application-secret.example.yml application-secret.yml
```

### Modifier `application-secret.yml`

```yaml
spring:
  datasource:
    password: agileforge_dev

  data:
    redis:
      password:   # laisser vide si Redis sans mot de passe

agileforge:
  security:
    jwt:
      secret-key: bXktc3VwZXItc2VjcmV0LWtleS1mb3ItZGV2LXRoYXQtaXMtYXQtbGVhc3QtMjU2LWJpdHMtbG9uZy1lbm91Z2g=

  ai:
    api-key:   # optionnel : votre clé API Claude si vous voulez l'assistant IA
```

> Le `secret-key` est un secret encodé en Base64. Pour le dev, la valeur ci-dessus suffit.

---

## Étape 5 : Lancer le backend

```bash
cd agileforge-backend

# Compiler (la première fois, télécharge les dépendances ~5 min)
mvn clean compile

# Lancer l'application
mvn spring-boot:run
```

### Vérifications

```bash
# Le backend est disponible sur :
# API : http://localhost:8080/api/
# Swagger : http://localhost:8080/api/swagger-ui.html
# Health : http://localhost:8080/api/actuator/health

curl http://localhost:8080/api/actuator/health
# → {"status":"UP"}
```

> **Note** : Les migrations Flyway s'exécutent automatiquement au démarrage et créent toutes les tables.

---

## Étape 6 : Lancer le frontend

Ouvrir un **nouveau terminal** :

```bash
cd agileforge-frontend

# Installer les dépendances (première fois uniquement)
npm install

# Lancer en mode développement
ng serve
# ou
npm start
```

### Accès

```
Application : http://localhost:4200
```

L'application Angular est configurée pour appeler le backend sur `http://localhost:8080/api`.

---

## Étape 7 : Utiliser l'application

1. Ouvrir http://localhost:4200 dans le navigateur
2. Cliquer "S'inscrire"
3. Créer un compte (prénom, nom, email, mot de passe)
4. Se connecter
5. Créer une organisation puis un projet
6. Commencer à créer des tickets

---

## Résumé des services

| Service | URL | Port |
|---------|-----|------|
| Frontend Angular | http://localhost:4200 | 4200 |
| Backend Spring Boot | http://localhost:8080/api | 8080 |
| Swagger UI | http://localhost:8080/api/swagger-ui.html | 8080 |
| PostgreSQL | localhost | 5432 |
| Redis | localhost | 6379 |

---

## Arrêter les services

```bash
# Backend : Ctrl+C dans le terminal Maven
# Frontend : Ctrl+C dans le terminal ng serve
# Redis : redis-cli shutdown (ou arrêter le service)
# PostgreSQL : selon l'installation (service stop)
```

---

## Résolution de problèmes

### Le backend ne démarre pas

| Erreur | Solution |
|--------|----------|
| `Connection refused :5432` | PostgreSQL n'est pas démarré |
| `FATAL: password authentication failed` | Vérifier le mot de passe dans `application-secret.yml` |
| `FATAL: database "agileforge_dev" does not exist` | Créer la base (étape 2) |
| `Connection refused :6379` | Redis n'est pas démarré |
| `Port 8080 already in use` | Un autre service utilise le port. `lsof -i :8080` ou `netstat -ano | findstr 8080` |

### Le frontend ne compile pas

| Erreur | Solution |
|--------|----------|
| `Module not found` | Supprimer `node_modules` et relancer `npm install` |
| `Version mismatch` | Vérifier la version de Node.js (22+) |
| `CORS error dans la console` | Le backend n'est pas démarré, ou mauvais port |

### Les migrations Flyway échouent

```bash
# Vérifier que l'utilisateur a les droits sur le schéma
psql -U postgres -d agileforge_dev -c "GRANT ALL ON SCHEMA public TO agileforge;"
```

---

## Mode développement : rechargement automatique

- **Frontend** : Le `ng serve` recharge automatiquement à chaque modification de fichier
- **Backend** : Ajouter `spring-boot-devtools` au pom.xml pour le hot-reload, ou relancer `mvn spring-boot:run`

---

## Kafka (optionnel)

Kafka n'est pas requis pour le fonctionnement de base. Si vous voulez tester les événements asynchrones :

```bash
# Télécharger Kafka
# https://kafka.apache.org/downloads

# Démarrer en mode KRaft (sans Zookeeper)
bin/kafka-storage.sh format -t $(bin/kafka-storage.sh random-uuid) -c config/kraft/server.properties
bin/kafka-server-start.sh config/kraft/server.properties
```
