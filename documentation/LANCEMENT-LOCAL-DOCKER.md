# Lancement Local AVEC Docker

Guide pour lancer AgileForge entièrement conteneurisé avec Docker Compose. C'est la méthode la plus simple : une seule commande lance tout.

---

## Prérequis

| Logiciel | Version | Téléchargement |
|----------|---------|----------------|
| Docker Desktop | 24+ | https://www.docker.com/products/docker-desktop/ |
| Docker Compose | 2.x (inclus dans Docker Desktop) | - |
| Git | 2.40+ | https://git-scm.com/ |

### Vérification

```bash
docker --version         # → Docker version 24.x.x
docker compose version   # → Docker Compose version v2.x.x
git --version           # → git version 2.4x.x
```

> **Windows/macOS** : Docker Desktop inclut Docker Compose.
> **Linux** : Installer Docker Engine + Docker Compose plugin séparément.

---

## Étape 1 : Cloner le projet

```bash
git clone https://github.com/landry1996/AgileForge.git
cd AgileForge
```

---

## Étape 2 : Configurer les variables d'environnement

Créer un fichier `.env` à la racine du projet :

```bash
# Créer le fichier .env
cat > .env << 'EOF'
# === JWT Secret (obligatoire) ===
JWT_SECRET_KEY=bXktc3VwZXItc2VjcmV0LWtleS1mb3ItZGV2LXRoYXQtaXMtYXQtbGVhc3QtMjU2LWJpdHMtbG9uZy1lbm91Z2g=

# === Claude API (optionnel, pour l'assistant IA) ===
CLAUDE_API_KEY=

# === Database (les valeurs par défaut suffisent pour le dev) ===
# DB_PASSWORD=agileforge_dev
EOF
```

> Ce fichier est déjà dans `.gitignore`, il ne sera jamais commité.

---

## Étape 3 : Lancer tous les services

```bash
docker compose up -d
```

C'est tout. Docker va :
1. Télécharger les images (PostgreSQL, Redis, Kafka, Nginx)
2. Construire le backend Java (compilation Maven dans Docker)
3. Construire le frontend Angular (build de production)
4. Démarrer tous les services dans le bon ordre

### Première exécution

La première fois, le build prend **5-10 minutes** (téléchargement des dépendances Maven et npm). Les exécutions suivantes sont beaucoup plus rapides grâce au cache Docker.

---

## Étape 4 : Vérifier que tout fonctionne

```bash
# Voir l'état des conteneurs
docker compose ps

# Résultat attendu :
# NAME                  STATUS          PORTS
# agileforge-postgres   Up (healthy)    0.0.0.0:5432->5432/tcp
# agileforge-redis      Up (healthy)    0.0.0.0:6379->6379/tcp
# agileforge-kafka      Up              0.0.0.0:9092->9092/tcp
# agileforge-backend    Up (healthy)    0.0.0.0:8080->8080/tcp
# agileforge-frontend   Up              0.0.0.0:80->80/tcp
# agileforge-pgadmin    Up              0.0.0.0:5050->80/tcp
```

### Tester les services

```bash
# Health check backend
curl http://localhost:8080/api/actuator/health
# → {"status":"UP"}

# Ou ouvrir dans le navigateur :
# http://localhost         → Application (frontend)
# http://localhost:8080/api/swagger-ui.html → Documentation API
# http://localhost:5050    → pgAdmin (admin@agileforge.io / admin)
```

---

## Accès aux services

| Service | URL | Identifiants |
|---------|-----|-------------|
| **Application** | http://localhost | - (créer un compte) |
| **API Backend** | http://localhost:8080/api | Bearer token |
| **Swagger UI** | http://localhost:8080/api/swagger-ui.html | - |
| **pgAdmin** | http://localhost:5050 | admin@agileforge.io / admin |
| **PostgreSQL** | localhost:5432 | agileforge / agileforge_dev |
| **Redis** | localhost:6379 | - |

### Configurer pgAdmin pour voir la BDD

1. Ouvrir http://localhost:5050
2. Se connecter : `admin@agileforge.io` / `admin`
3. Ajouter un serveur :
   - Nom : AgileForge Dev
   - Host : `postgres` (nom du conteneur Docker)
   - Port : `5432`
   - Username : `agileforge`
   - Password : `agileforge_dev`

---

## Commandes utiles

### Voir les logs

```bash
# Tous les services
docker compose logs -f

# Un service spécifique
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f postgres
```

### Reconstruire après modification du code

```bash
# Reconstruire uniquement le backend
docker compose up -d --build backend

# Reconstruire uniquement le frontend
docker compose up -d --build frontend

# Tout reconstruire
docker compose up -d --build
```

### Arrêter les services

```bash
# Arrêter (conserve les données)
docker compose stop

# Arrêter et supprimer les conteneurs (conserve les volumes)
docker compose down

# Arrêter et SUPPRIMER les données (reset complet)
docker compose down -v
```

### Accéder au shell d'un conteneur

```bash
# Shell dans le conteneur backend
docker exec -it agileforge-backend sh

# Shell PostgreSQL
docker exec -it agileforge-postgres psql -U agileforge -d agileforge_dev

# Shell Redis
docker exec -it agileforge-redis redis-cli
```

### Reset de la base de données

```bash
# Arrêter et supprimer le volume PostgreSQL
docker compose down
docker volume rm agileforge_postgres_data

# Relancer (Flyway recrée toutes les tables)
docker compose up -d
```

---

## Développement hybride (recommandé)

Pour un cycle de développement rapide, vous pouvez utiliser Docker pour l'infrastructure et lancer le backend/frontend en local :

```bash
# Démarrer seulement l'infrastructure
docker compose up -d postgres redis kafka

# Lancer le backend en local (rechargement plus rapide)
cd agileforge-backend
mvn spring-boot:run

# Lancer le frontend en local (hot-reload)
cd agileforge-frontend
ng serve
```

Avantage : Hot-reload instantané pour le frontend, redémarrage rapide du backend, sans reconstruire les images Docker.

---

## Résolution de problèmes

### Le backend ne démarre pas (health check failed)

```bash
# Vérifier les logs
docker compose logs backend

# Causes fréquentes :
# - PostgreSQL pas encore prêt → attendre quelques secondes
# - Port 8080 déjà utilisé → arrêter le processus local
# - Erreur de compilation → vérifier les logs du build
```

### Port déjà utilisé

```bash
# Sur Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Sur Linux/macOS
lsof -i :8080
kill <PID>
```

### Espace disque insuffisant

```bash
# Nettoyer les images et volumes Docker non utilisés
docker system prune -a --volumes
```

### Le frontend affiche une page blanche

```bash
# Vérifier que le build Angular a réussi
docker compose logs frontend

# Si "ng build" a échoué, reconstruire
docker compose up -d --build frontend
```

### Impossible de se connecter à PostgreSQL depuis pgAdmin

- Host doit être `postgres` (pas `localhost`) car pgAdmin est dans le réseau Docker
- Port : `5432`
- Si ça ne marche pas, vérifier : `docker compose ps postgres`

---

## Variables Docker Compose (.env)

| Variable | Défaut | Description |
|----------|--------|-------------|
| `JWT_SECRET_KEY` | (dev key) | Clé JWT (base64, min 256 bits) |
| `CLAUDE_API_KEY` | (vide) | Clé API Claude pour l'assistant IA |
| `DB_PASSWORD` | `agileforge_dev` | Mot de passe PostgreSQL |
