#!/bin/bash
# =============================================================================
# Script de sauvegarde automatique
# À ajouter dans crontab : 0 3 * * * /home/agileforge/AgileForge/deploy/scripts/backup.sh
# =============================================================================

set -e

# Configuration
BACKUP_DIR="/home/agileforge/backups"
RETENTION_DAYS=30
DATE=$(date +%Y%m%d_%H%M%S)
PROJECT_DIR="/home/agileforge/AgileForge"

# Charger les variables
if [ -f "$PROJECT_DIR/.env.prod" ]; then
    export $(grep -v '^#' "$PROJECT_DIR/.env.prod" | xargs)
fi

# Créer le dossier de backup
mkdir -p "$BACKUP_DIR"

echo "[$DATE] Début de la sauvegarde..."

# === Sauvegarde PostgreSQL ===
echo "  → Sauvegarde PostgreSQL..."
docker exec agileforge-postgres pg_dump \
    -U "${POSTGRES_USER:-agileforge}" \
    -d "${POSTGRES_DB:-agileforge_prod}" \
    --format=custom \
    --compress=9 \
    > "$BACKUP_DIR/db_${DATE}.dump"

echo "  → Taille : $(du -h "$BACKUP_DIR/db_${DATE}.dump" | cut -f1)"

# === Sauvegarde des uploads (si existants) ===
if [ -d "$PROJECT_DIR/uploads" ]; then
    echo "  → Sauvegarde des fichiers uploadés..."
    tar -czf "$BACKUP_DIR/uploads_${DATE}.tar.gz" -C "$PROJECT_DIR" uploads/
fi

# === Sauvegarde du .env.prod (chiffré) ===
echo "  → Sauvegarde de la configuration..."
cp "$PROJECT_DIR/.env.prod" "$BACKUP_DIR/env_${DATE}.bak"
chmod 600 "$BACKUP_DIR/env_${DATE}.bak"

# === Nettoyage des anciennes sauvegardes ===
echo "  → Nettoyage des sauvegardes > ${RETENTION_DAYS} jours..."
find "$BACKUP_DIR" -name "db_*.dump" -mtime +$RETENTION_DAYS -delete
find "$BACKUP_DIR" -name "uploads_*.tar.gz" -mtime +$RETENTION_DAYS -delete
find "$BACKUP_DIR" -name "env_*.bak" -mtime +$RETENTION_DAYS -delete

# === Résumé ===
TOTAL_SIZE=$(du -sh "$BACKUP_DIR" | cut -f1)
BACKUP_COUNT=$(ls -1 "$BACKUP_DIR"/db_*.dump 2>/dev/null | wc -l)

echo "[$DATE] Sauvegarde terminée !"
echo "  → Fichiers : $BACKUP_COUNT sauvegardes conservées"
echo "  → Espace total : $TOTAL_SIZE"
echo ""
