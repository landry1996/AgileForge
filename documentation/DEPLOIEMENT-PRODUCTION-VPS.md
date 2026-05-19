# Déploiement en Production sur VPS

Guide complet pour déployer AgileForge sur un serveur VPS (OVH, Hetzner, DigitalOcean, Contabo, etc.) avec HTTPS, domaine personnalisé, et bonnes pratiques de sécurité.

---

## Architecture cible

```
Internet
    │
    ▼
┌────────────────────────────────────────────────────────────┐
│  VPS (Ubuntu 22.04 / Debian 12)                            │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Nginx Reverse Proxy (port 80/443)                    │  │
│  │  + Let's Encrypt SSL                                  │  │
│  └──────────┬───────────────────────┬────────────────────┘  │
│             │                       │                       │
│  ┌──────────▼────────┐  ┌──────────▼────────────────────┐  │
│  │  Frontend (Nginx)  │  │  Backend (Spring Boot :8080)  │  │
│  │  Static files      │  │  Java 21                      │  │
│  └────────────────────┘  └──────────┬────────────────────┘  │
│                                     │                       │
│  ┌──────────────────┐  ┌───────────▼───────────────────┐  │
│  │  Redis :6379     │  │  PostgreSQL :5432              │  │
│  └──────────────────┘  └───────────────────────────────┘  │
│                                                            │
│  Firewall : UFW (seuls 22, 80, 443 ouverts)               │
└────────────────────────────────────────────────────────────┘
```

---

## Prérequis

- **VPS** : Minimum 2 vCPU, 4 Go RAM, 40 Go SSD
- **OS** : Ubuntu 22.04 LTS ou Debian 12
- **Domaine** : Un nom de domaine pointant vers l'IP du VPS (ex: `agileforge.votredomaine.com`)
- **Accès SSH** : Clé SSH configurée

### Fournisseurs VPS recommandés

| Fournisseur | Config minimum | Prix approximatif |
|-------------|---------------|-------------------|
| Hetzner | CX21 (2 vCPU, 4 Go) | ~5€/mois |
| DigitalOcean | Basic (2 vCPU, 4 Go) | ~24$/mois |
| OVH | VPS Starter (2 vCPU, 4 Go) | ~8€/mois |
| Contabo | VPS S (4 vCPU, 8 Go) | ~6€/mois |

---

## Étape 1 : Préparer le serveur

### Se connecter au VPS

```bash
ssh root@VOTRE_IP_VPS
```

### Mettre à jour le système

```bash
apt update && apt upgrade -y
```

### Créer un utilisateur dédié (ne pas utiliser root)

```bash
adduser agileforge
usermod -aG sudo agileforge
# Copier la clé SSH
mkdir -p /home/agileforge/.ssh
cp ~/.ssh/authorized_keys /home/agileforge/.ssh/
chown -R agileforge:agileforge /home/agileforge/.ssh

# Se reconnecter en tant que agileforge
exit
ssh agileforge@VOTRE_IP_VPS
```

### Configurer le firewall

```bash
sudo ufw default deny incoming
sudo ufw default allow outgoing
sudo ufw allow 22/tcp    # SSH
sudo ufw allow 80/tcp    # HTTP
sudo ufw allow 443/tcp   # HTTPS
sudo ufw enable
sudo ufw status
```

### Installer Docker

```bash
# Installer Docker
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker agileforge

# Installer Docker Compose plugin
sudo apt install docker-compose-plugin -y

# Vérifier
docker --version
docker compose version

# Se déconnecter/reconnecter pour appliquer le groupe docker
exit
ssh agileforge@VOTRE_IP_VPS
```

---

## Étape 2 : Configurer le DNS

Chez votre registrar DNS, créer un enregistrement :

```
Type: A
Nom:  agileforge (ou @ pour le domaine racine)
IP:   VOTRE_IP_VPS
TTL:  300
```

Vérifier la propagation :
```bash
dig agileforge.votredomaine.com
# Doit retourner votre IP
```

---

## Étape 3 : Déployer l'application

### Cloner le projet

```bash
cd /home/agileforge
git clone https://github.com/landry1996/AgileForge.git
cd AgileForge
```

### Créer le fichier d'environnement production

```bash
cat > .env.prod << 'EOF'
# === BASE DE DONNÉES ===
POSTGRES_DB=agileforge_prod
POSTGRES_USER=agileforge
POSTGRES_PASSWORD=CHANGEZ_MOI_MOT_DE_PASSE_FORT_32_CHARS

# === JWT (OBLIGATOIRE : générer une clé unique) ===
# Générer avec : openssl rand -base64 64
JWT_SECRET_KEY=CHANGEZ_MOI_GENEREZ_AVEC_OPENSSL_RAND_BASE64_64

# === CLAUDE API (optionnel) ===
CLAUDE_API_KEY=

# === DOMAINE ===
DOMAIN=agileforge.votredomaine.com

# === EMAIL pour Let's Encrypt ===
LETSENCRYPT_EMAIL=votre@email.com
EOF
```

### Générer les secrets

```bash
# Générer un mot de passe BDD fort
echo "POSTGRES_PASSWORD=$(openssl rand -base64 32)"

# Générer le JWT secret
echo "JWT_SECRET_KEY=$(openssl rand -base64 64)"
```

Remplacer les valeurs `CHANGEZ_MOI` dans `.env.prod` avec les valeurs générées.

---

## Étape 4 : Fichiers de configuration production

Les fichiers suivants sont déjà créés dans le dossier `deploy/` du projet (voir ci-dessous). Ils sont automatiquement utilisés par le `docker-compose.prod.yml`.

---

## Étape 5 : Lancer en production

```bash
cd /home/agileforge/AgileForge

# Lancer avec le fichier compose de production
docker compose -f docker-compose.prod.yml --env-file .env.prod up -d

# Vérifier
docker compose -f docker-compose.prod.yml ps

# Voir les logs
docker compose -f docker-compose.prod.yml logs -f
```

### Vérifier le SSL

```bash
# Attendre 1-2 minutes pour que Certbot génère le certificat
curl https://agileforge.votredomaine.com/api/actuator/health
# → {"status":"UP"}
```

---

## Étape 6 : Mise à jour de l'application

```bash
cd /home/agileforge/AgileForge

# Récupérer les dernières modifications
git pull origin main

# Reconstruire et redémarrer
docker compose -f docker-compose.prod.yml --env-file .env.prod up -d --build backend frontend

# Vérifier la santé
docker compose -f docker-compose.prod.yml ps
```

---

## Maintenance

### Sauvegardes automatiques

```bash
# Ajouter au crontab (sauvegarde quotidienne à 3h du matin)
crontab -e

# Ajouter cette ligne :
0 3 * * * /home/agileforge/AgileForge/deploy/scripts/backup.sh
```

### Voir les logs

```bash
# Tous les services
docker compose -f docker-compose.prod.yml logs -f

# Backend uniquement
docker compose -f docker-compose.prod.yml logs -f backend

# 100 dernières lignes
docker compose -f docker-compose.prod.yml logs --tail=100 backend
```

### Renouvellement SSL

Le certificat Let's Encrypt est renouvelé automatiquement par Certbot. Vérifier :

```bash
# Test du renouvellement
docker compose -f docker-compose.prod.yml exec certbot certbot renew --dry-run
```

### Monitoring basique

```bash
# Utilisation CPU/RAM
docker stats

# Espace disque
df -h

# Connexions actives
ss -tuln
```

---

## Sécurité production

### Checklist avant mise en ligne

- [ ] Mot de passe PostgreSQL fort (32+ caractères aléatoires)
- [ ] JWT secret unique et fort (64 caractères base64)
- [ ] Firewall UFW activé (seuls 22, 80, 443 ouverts)
- [ ] SSH par clé uniquement (désactiver l'auth par mot de passe)
- [ ] HTTPS avec Let's Encrypt activé
- [ ] Pas d'accès root direct (utilisateur dédié)
- [ ] Swagger UI désactivé en production
- [ ] Logs activés et monitorés
- [ ] Sauvegardes automatiques configurées
- [ ] Redis non exposé sur l'extérieur
- [ ] PostgreSQL non exposé sur l'extérieur

### Désactiver l'authentification SSH par mot de passe

```bash
sudo nano /etc/ssh/sshd_config
# Changer : PasswordAuthentication no
sudo systemctl restart sshd
```

### Activer les mises à jour automatiques de sécurité

```bash
sudo apt install unattended-upgrades -y
sudo dpkg-reconfigure -plow unattended-upgrades
```

---

## Troubleshooting

### Le site n'est pas accessible

```bash
# Vérifier le firewall
sudo ufw status

# Vérifier que les conteneurs tournent
docker compose -f docker-compose.prod.yml ps

# Vérifier Nginx
docker compose -f docker-compose.prod.yml logs nginx-proxy
```

### Erreur 502 Bad Gateway

```bash
# Le backend n'est pas prêt
docker compose -f docker-compose.prod.yml logs backend

# Vérifier le healthcheck
docker compose -f docker-compose.prod.yml exec backend wget -qO- http://localhost:8080/api/actuator/health
```

### Certificat SSL non généré

```bash
# Vérifier les logs Certbot
docker compose -f docker-compose.prod.yml logs certbot

# Le domaine doit pointer vers le VPS AVANT de lancer Certbot
dig agileforge.votredomaine.com
```

### Base de données pleine

```bash
# Vérifier la taille
docker compose -f docker-compose.prod.yml exec postgres psql -U agileforge -d agileforge_prod -c "SELECT pg_size_pretty(pg_database_size('agileforge_prod'));"
```
