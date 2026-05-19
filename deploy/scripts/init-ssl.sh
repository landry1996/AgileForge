#!/bin/bash
# =============================================================================
# Script d'initialisation SSL (Let's Encrypt)
# À exécuter UNE SEULE FOIS lors du premier déploiement
# =============================================================================

set -e

# Charger les variables d'environnement
if [ -f .env.prod ]; then
    export $(grep -v '^#' .env.prod | xargs)
fi

if [ -z "$DOMAIN" ]; then
    echo "Erreur : DOMAIN n'est pas défini dans .env.prod"
    exit 1
fi

if [ -z "$LETSENCRYPT_EMAIL" ]; then
    echo "Erreur : LETSENCRYPT_EMAIL n'est pas défini dans .env.prod"
    exit 1
fi

echo "=== Initialisation SSL pour $DOMAIN ==="

# Étape 1 : Utiliser la configuration initiale (HTTP uniquement)
echo "[1/5] Configuration Nginx en mode HTTP..."
cp deploy/nginx/conf.d/agileforge-initial.conf deploy/nginx/conf.d/default.conf
# S'assurer que la config SSL n'est pas active
rm -f deploy/nginx/conf.d/agileforge-active.conf

# Étape 2 : Démarrer Nginx et le backend
echo "[2/5] Démarrage des services..."
docker compose -f docker-compose.prod.yml --env-file .env.prod up -d postgres redis backend frontend nginx-proxy

# Attendre que Nginx soit prêt
echo "Attente du démarrage de Nginx..."
sleep 10

# Étape 3 : Vérifier que le domaine est accessible
echo "[3/5] Vérification de l'accessibilité du domaine..."
if ! curl -s -o /dev/null -w "%{http_code}" http://$DOMAIN/.well-known/acme-challenge/test 2>/dev/null | grep -q "404\|200"; then
    echo "Attention : Le domaine $DOMAIN ne semble pas accessible."
    echo "Vérifiez que le DNS pointe vers ce serveur."
    echo "Continuer quand même ? (y/n)"
    read -r response
    if [ "$response" != "y" ]; then
        exit 1
    fi
fi

# Étape 4 : Demander le certificat
echo "[4/5] Demande du certificat Let's Encrypt..."
docker compose -f docker-compose.prod.yml --env-file .env.prod run --rm certbot \
    certbot certonly \
    --webroot \
    --webroot-path=/var/www/certbot \
    --email $LETSENCRYPT_EMAIL \
    --agree-tos \
    --no-eff-email \
    -d $DOMAIN

# Étape 5 : Basculer vers la configuration HTTPS
echo "[5/5] Activation de la configuration HTTPS..."

# Remplacer la variable DOMAIN dans le template
envsubst '${DOMAIN}' < deploy/nginx/conf.d/agileforge.conf > deploy/nginx/conf.d/default.conf

# Redémarrer Nginx avec SSL
docker compose -f docker-compose.prod.yml --env-file .env.prod restart nginx-proxy

echo ""
echo "=== SSL configuré avec succès ! ==="
echo "Votre application est accessible sur : https://$DOMAIN"
echo ""
echo "Le renouvellement automatique est géré par le conteneur certbot."
