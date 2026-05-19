# Dossier Deploy — Configuration Production

Ce dossier contient tout le nécessaire pour déployer AgileForge en production sur un VPS.

## Structure

```
deploy/
├── nginx/
│   ├── nginx-prod.conf              # Configuration Nginx principale (workers, gzip, security)
│   └── conf.d/
│       ├── agileforge.conf           # Virtual host HTTPS (production)
│       └── agileforge-initial.conf   # Virtual host HTTP (premier déploiement, avant SSL)
├── scripts/
│   ├── init-ssl.sh                   # Initialisation du certificat Let's Encrypt
│   ├── deploy.sh                     # Mise à jour de l'application
│   ├── backup.sh                     # Sauvegarde automatique (crontab)
│   ├── restore.sh                    # Restauration d'une sauvegarde
│   └── monitor.sh                    # Vérification de l'état du système
└── README.md                         # Ce fichier
```

## Utilisation rapide

```bash
# Premier déploiement
chmod +x deploy/scripts/*.sh
./deploy/scripts/init-ssl.sh

# Mises à jour
./deploy/scripts/deploy.sh

# Monitoring
./deploy/scripts/monitor.sh

# Sauvegarde manuelle
./deploy/scripts/backup.sh

# Restauration
./deploy/scripts/restore.sh /home/agileforge/backups/db_20260519_030000.dump
```

## Fichiers liés (à la racine du projet)

- `docker-compose.prod.yml` — Compose file de production
- `.env.prod` — Variables d'environnement (à créer, ne pas committer)
- `agileforge-backend/src/main/resources/application-prod.yml` — Config Spring production
