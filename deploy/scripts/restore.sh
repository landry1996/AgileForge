#!/bin/bash
# =============================================================================
# Script de restauration d'une sauvegarde
# Usage : ./deploy/scripts/restore.sh [fichier_backup.dump]
# =============================================================================

set -e

PROJECT_DIR="/home/agileforge/AgileForge"
BACKUP_DIR="/home/agileforge/backups"

if [ -f "$PROJECT_DIR/.env.prod" ]; then
    export $(grep -v '^#' "$PROJECT_DIR/.env.prod" | xargs)
fi

DB_USER="${POSTGRES_USER:-agileforge}"
DB_NAME="${POSTGRES_DB:-agileforge_prod}"

# Vérifier l'argument
if [ -z "$1" ]; then
    echo "Usage : $0 <fichier_backup.dump>"
    echo ""
    echo "Sauvegardes disponibles :"
    ls -lh "$BACKUP_DIR"/db_*.dump "$BACKUP_DIR"/pre_deploy_*.dump 2>/dev/null | awk '{print "  " $NF " (" $5 ")"}'
    exit 1
fi

BACKUP_FILE="$1"

if [ ! -f "$BACKUP_FILE" ]; then
    # Chercher dans le dossier de backup
    if [ -f "$BACKUP_DIR/$BACKUP_FILE" ]; then
        BACKUP_FILE="$BACKUP_DIR/$BACKUP_FILE"
    else
        echo "Erreur : Fichier $BACKUP_FILE introuvable"
        exit 1
    fi
fi

echo "=== Restauration de la base de données ==="
echo "Fichier : $BACKUP_FILE"
echo "Base cible : $DB_NAME"
echo ""
echo "ATTENTION : Cela va ÉCRASER toutes les données actuelles !"
echo "Continuer ? (yes/no)"
read -r response

if [ "$response" != "yes" ]; then
    echo "Annulé."
    exit 0
fi

# Sauvegarde de sécurité avant restauration
echo "[1/3] Sauvegarde de sécurité..."
DATE=$(date +%Y%m%d_%H%M%S)
docker exec agileforge-postgres pg_dump \
    -U $DB_USER -d $DB_NAME --format=custom --compress=9 \
    > "$BACKUP_DIR/pre_restore_${DATE}.dump"

# Arrêter le backend pour éviter les connexions actives
echo "[2/3] Arrêt du backend..."
cd "$PROJECT_DIR"
docker compose -f docker-compose.prod.yml stop backend

# Restaurer
echo "[3/3] Restauration en cours..."
docker exec -i agileforge-postgres pg_restore \
    -U $DB_USER \
    -d $DB_NAME \
    --clean \
    --if-exists \
    --no-owner \
    < "$BACKUP_FILE"

# Redémarrer le backend
echo "Redémarrage du backend..."
docker compose -f docker-compose.prod.yml --env-file .env.prod up -d backend

echo ""
echo "=== Restauration terminée ! ==="
echo "Sauvegarde de sécurité : $BACKUP_DIR/pre_restore_${DATE}.dump"
