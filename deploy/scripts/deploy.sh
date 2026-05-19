#!/bin/bash
# =============================================================================
# Script de déploiement / mise à jour
# Usage : ./deploy/scripts/deploy.sh
# =============================================================================

set -e

PROJECT_DIR="/home/agileforge/AgileForge"
COMPOSE_FILE="docker-compose.prod.yml"
ENV_FILE=".env.prod"

cd "$PROJECT_DIR"

echo "=== Déploiement AgileForge ==="
echo "Date : $(date)"
echo ""

# Étape 1 : Pull des dernières modifications
echo "[1/5] Récupération du code..."
git pull origin main

# Étape 2 : Sauvegarde de la BDD avant mise à jour
echo "[2/5] Sauvegarde pré-déploiement..."
DATE=$(date +%Y%m%d_%H%M%S)
mkdir -p /home/agileforge/backups
docker exec agileforge-postgres pg_dump \
    -U agileforge -d agileforge_prod --format=custom --compress=9 \
    > "/home/agileforge/backups/pre_deploy_${DATE}.dump" 2>/dev/null || true

# Étape 3 : Reconstruire les images
echo "[3/5] Build des images..."
docker compose -f $COMPOSE_FILE --env-file $ENV_FILE build backend frontend

# Étape 4 : Redémarrer les services applicatifs
echo "[4/5] Redémarrage des services..."
docker compose -f $COMPOSE_FILE --env-file $ENV_FILE up -d backend frontend

# Étape 5 : Vérifier la santé
echo "[5/5] Vérification de la santé..."
echo "Attente du démarrage du backend (60s max)..."

for i in $(seq 1 12); do
    sleep 5
    HEALTH=$(docker exec agileforge-backend wget -qO- http://localhost:8080/api/actuator/health 2>/dev/null || echo "")
    if echo "$HEALTH" | grep -q '"status":"UP"'; then
        echo ""
        echo "=== Déploiement réussi ! ==="
        echo "Backend : UP"
        echo "Version : $(git log --oneline -1)"
        echo ""
        exit 0
    fi
    echo -n "."
done

echo ""
echo "!!! ATTENTION : Le backend ne répond pas après 60s !!!"
echo "Vérifier les logs : docker compose -f $COMPOSE_FILE logs backend"
echo ""
echo "Pour rollback :"
echo "  git checkout HEAD~1"
echo "  docker compose -f $COMPOSE_FILE --env-file $ENV_FILE up -d --build backend frontend"
echo ""
echo "Restaurer la BDD si nécessaire :"
echo "  docker exec -i agileforge-postgres pg_restore -U agileforge -d agileforge_prod < /home/agileforge/backups/pre_deploy_${DATE}.dump"
exit 1
