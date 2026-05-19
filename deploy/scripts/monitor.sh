#!/bin/bash
# =============================================================================
# Script de monitoring simple
# Usage : ./deploy/scripts/monitor.sh
# =============================================================================

PROJECT_DIR="/home/agileforge/AgileForge"
COMPOSE_FILE="docker-compose.prod.yml"

echo "=== AgileForge - État du système ==="
echo "Date : $(date)"
echo ""

# État des conteneurs
echo "--- Conteneurs ---"
cd "$PROJECT_DIR"
docker compose -f $COMPOSE_FILE ps --format "table {{.Name}}\t{{.Status}}\t{{.Ports}}"
echo ""

# Santé du backend
echo "--- Backend Health ---"
HEALTH=$(docker exec agileforge-backend wget -qO- http://localhost:8080/api/actuator/health 2>/dev/null || echo '{"status":"DOWN"}')
echo "  Status: $(echo $HEALTH | grep -o '"status":"[^"]*"')"
echo ""

# Utilisation des ressources
echo "--- Ressources ---"
docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.NetIO}}" 2>/dev/null | head -10
echo ""

# Espace disque
echo "--- Espace disque ---"
df -h / | tail -1 | awk '{print "  Utilisé: " $3 " / " $2 " (" $5 ")"}'
echo ""

# Taille de la BDD
echo "--- Base de données ---"
DB_SIZE=$(docker exec agileforge-postgres psql -U agileforge -d agileforge_prod -t -c "SELECT pg_size_pretty(pg_database_size('agileforge_prod'));" 2>/dev/null || echo "N/A")
echo "  Taille : $DB_SIZE"
CONN_COUNT=$(docker exec agileforge-postgres psql -U agileforge -d agileforge_prod -t -c "SELECT count(*) FROM pg_stat_activity WHERE datname='agileforge_prod';" 2>/dev/null || echo "N/A")
echo "  Connexions actives : $CONN_COUNT"
echo ""

# Certificat SSL
echo "--- Certificat SSL ---"
if [ -f /home/agileforge/AgileForge/deploy/nginx/conf.d/agileforge.conf ]; then
    DOMAIN=$(grep server_name /home/agileforge/AgileForge/deploy/nginx/conf.d/agileforge.conf 2>/dev/null | head -1 | awk '{print $2}' | tr -d ';')
    if [ -n "$DOMAIN" ]; then
        EXPIRY=$(echo | openssl s_client -connect $DOMAIN:443 -servername $DOMAIN 2>/dev/null | openssl x509 -noout -enddate 2>/dev/null | cut -d= -f2)
        if [ -n "$EXPIRY" ]; then
            echo "  Domaine : $DOMAIN"
            echo "  Expiration : $EXPIRY"
        else
            echo "  Impossible de vérifier le certificat"
        fi
    fi
else
    echo "  Non configuré"
fi
echo ""

# Dernières sauvegardes
echo "--- Dernière sauvegarde ---"
LAST_BACKUP=$(ls -t /home/agileforge/backups/db_*.dump 2>/dev/null | head -1)
if [ -n "$LAST_BACKUP" ]; then
    echo "  Fichier : $(basename $LAST_BACKUP)"
    echo "  Taille : $(du -h "$LAST_BACKUP" | cut -f1)"
    echo "  Date : $(stat -c %y "$LAST_BACKUP" 2>/dev/null | cut -d. -f1)"
else
    echo "  Aucune sauvegarde trouvée !"
fi
echo ""
echo "==================================="
