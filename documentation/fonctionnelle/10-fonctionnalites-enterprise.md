# Fonctionnalités Enterprise (v4.0)

## Audit Trail Renforcé

### User Story
> En tant qu'administrateur, je veux un journal complet de toutes les actions pour assurer la conformité et la traçabilité.

### Événements audités

| Catégorie | Actions | Sévérité |
|-----------|---------|----------|
| Authentification | Login, logout, login échoué | INFO / WARNING |
| Utilisateurs | Création, modification rôle, désactivation | MEDIUM / HIGH |
| Tickets | Création, modification, suppression, transition | INFO |
| Projets | Création, suppression, changement config | MEDIUM |
| Données | Export, import, suppression en masse | HIGH |
| Sécurité | Changement mot de passe, création API key | HIGH |
| Admin | Modification permissions, configuration système | CRITICAL |

### Informations enregistrées

```json
{
  "id": "uuid",
  "timestamp": "2026-05-19T14:30:00Z",
  "userId": "uuid-de-l-acteur",
  "action": "TICKET_DELETED",
  "resource": "ticket",
  "resourceId": "uuid-du-ticket",
  "severity": "HIGH",
  "details": "Ticket MOB-42 supprimé par admin@company.com",
  "ipAddress": "192.168.1.100",
  "userAgent": "Mozilla/5.0..."
}
```

### Règles d'alerte

Les administrateurs peuvent configurer des alertes automatiques :

| Règle | Déclencheur | Action |
|-------|-------------|--------|
| Connexions suspectes | 5 échecs en 10 minutes | Notification + blocage temporaire |
| Suppression massive | > 10 suppressions en 1h | Notification immédiate |
| Accès hors heures | Connexion entre 22h et 6h | Log avec sévérité WARNING |
| Export de données | Tout export | Notification au ADMIN |

### Filtres de recherche
- Par utilisateur
- Par période (date début / fin)
- Par action (LOGIN, CREATE, DELETE, etc.)
- Par sévérité (INFO, MEDIUM, HIGH, CRITICAL)
- Par ressource (ticket, project, user, etc.)

### Résumé et statistiques
- Nombre d'événements par jour/semaine
- Répartition par action et sévérité
- Utilisateurs les plus actifs
- Tendances d'activité

---

## Portail Client

### User Story
> En tant que responsable de projet, je veux donner à mes clients une vue limitée sur l'avancement sans leur donner accès à l'outil interne.

### Configuration du portail
1. Activer le portail pour un projet
2. Choisir les informations visibles :
   - Tickets visibles (par statut, par label)
   - Informations affichées (titre, statut, priorité, dates)
   - Masquer les détails internes (commentaires d'équipe, estimations)
3. Créer des comptes clients (email + mot de passe dédié)

### Vue client

```
┌─────────────────────────────────────────────────┐
│  AgileForge - Portail Client                     │
│  Projet: Application Mobile v2                   │
├─────────────────────────────────────────────────┤
│                                                  │
│  Progression globale: [████████░░░] 72%          │
│                                                  │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐           │
│  │ En cours│ │ À venir │ │  Livrés │           │
│  │    5    │ │   12    │ │   28    │           │
│  └─────────┘ └─────────┘ └─────────┘           │
│                                                  │
│  Dernières mises à jour:                         │
│  • MOB-42 Dark mode → En test (il y a 2h)      │
│  • MOB-45 Fix crash → Livré (hier)             │
│                                                  │
│  [Soumettre un feedback]                        │
└─────────────────────────────────────────────────┘
```

### Feedback client
Les clients peuvent :
- Soumettre des demandes (feature request)
- Reporter des bugs
- Donner leur avis sur les livraisons
- Voter sur les fonctionnalités proposées

| Type de feedback | Description |
|------------------|-------------|
| FEATURE_REQUEST | Demande de nouvelle fonctionnalité |
| BUG_REPORT | Signalement de bug |
| IMPROVEMENT | Suggestion d'amélioration |
| SATISFACTION | Note de satisfaction |

---

## Gestion de Portfolio

### User Story
> En tant que CTO, je veux voir l'état de tous les projets sur un seul écran pour piloter la stratégie technologique.

### Dashboard portfolio

```
┌────────────────────────────────────────────────────────────┐
│  Portfolio: Produits Digitaux                                │
├────────────────────────────────────────────────────────────┤
│                                                             │
│  Projets (6)          Santé globale: ●●●●○ (80%)           │
│                                                             │
│  ┌──────────────┬──────────┬──────────┬──────────────────┐ │
│  │ Projet       │ Santé    │ Tickets  │ Prochain jalon    │ │
│  ├──────────────┼──────────┼──────────┼──────────────────┤ │
│  │ Mobile App   │ 🟢 92%  │ 45/60    │ v2.3 (12 juin)   │ │
│  │ API Gateway  │ 🟡 68%  │ 22/40    │ v1.1 (20 juin)   │ │
│  │ Admin Panel  │ 🟢 85%  │ 30/35    │ v3.0 (5 juil)    │ │
│  │ Data Pipeline│ 🔴 45%  │ 8/30     │ v1.0 (retard!)   │ │
│  │ Auth Service │ 🟢 95%  │ 18/19    │ v2.0 (30 mai)    │ │
│  │ Monitoring   │ 🟡 70%  │ 12/20    │ v1.2 (15 juin)   │ │
│  └──────────────┴──────────┴──────────┴──────────────────┘ │
│                                                             │
│  Risk Heatmap:                                              │
│                Impact                                       │
│           Low   Med   High                                  │
│  High   │     │  ●  │ ●●  │  ← Data Pipeline (retard)    │
│  Med    │  ●  │ ●●  │     │                               │
│  Low    │ ●●  │     │     │                               │
│          Probabilité                                        │
└────────────────────────────────────────────────────────────┘
```

### Heatmap des risques
Visualisation matricielle des projets selon :
- **Axe X** : Probabilité de retard (basée sur la vélocité et les blocages)
- **Axe Y** : Impact business (configuré manuellement)

### Fonctionnalités
- Vue consolidée de tous les projets
- Score de santé par projet (basé sur vélocité, blocages, retards)
- Alertes automatiques quand un projet passe en rouge
- Allocation des ressources entre projets

---

## Planification de Capacité

### User Story
> En tant que manager, je veux planifier l'allocation de mon équipe sur les prochains sprints pour éviter la surcharge.

### Vue capacité

```
Équipe: Backend Team (5 développeurs)
Période: Sprint 6 (02/06 - 15/06)

┌─────────────┬──────────┬──────────┬──────────┬────────────┐
│ Membre      │ Capacité │ Alloué   │ Dispo    │ Charge %   │
├─────────────┼──────────┼──────────┼──────────┼────────────┤
│ Alice       │ 8j       │ 7j       │ 1j       │ ███████░ 88%│
│ Bob         │ 8j       │ 9j       │ -1j      │ █████████ 112%│ ⚠️
│ Charlie     │ 6j       │ 5j       │ 1j       │ ██████░░ 83%│
│ Diana       │ 8j       │ 4j       │ 4j       │ ████░░░░ 50%│
│ Eve         │ 4j       │ 3j       │ 1j       │ ██████░░ 75%│
├─────────────┼──────────┼──────────┼──────────┼────────────┤
│ TOTAL       │ 34j      │ 28j      │ 6j       │ 82%        │
└─────────────┴──────────┴──────────┴──────────┴────────────┘
```

### Prévisions
- **Sous-capacité** : L'équipe peut absorber plus de travail
- **Sur-capacité** : Risque de burnout, redistribuer
- **Indisponibilités** : Congés, formations (réduisent la capacité)

### Alertes
| Situation | Alerte |
|-----------|--------|
| Membre > 100% | ⚠️ Surcharge détectée |
| Équipe > 90% | ⚠️ Risque de dépassement |
| Membre < 50% | ℹ️ Sous-utilisation |

---

## Gestion des Incidents

### User Story
> En tant que responsable technique, je veux gérer les incidents de production avec une timeline et un suivi structuré.

### Cycle de vie d'un incident

```
OPEN ──► INVESTIGATING ──► IDENTIFIED ──► MONITORING ──► RESOLVED
                                │                           │
                                └─── ESCALATED ─────────────┘
```

### Attributs

| Champ | Description |
|-------|-------------|
| Titre | Description courte de l'incident |
| Sévérité | LOW, MEDIUM, HIGH, CRITICAL |
| Statut | Étape de résolution |
| Assigné | Personne en charge |
| Participants | Équipe mobilisée |
| Services impactés | Composants affectés |
| Impact | Description de l'impact utilisateur |
| Root cause | Cause racine (post-mortem) |
| Résolution | Actions correctives |

### Timeline

```
Incident INC-7: "API Gateway timeout"
Sévérité: CRITICAL | Durée: 2h15

14:00 ● OUVERT par monitoring automatique
       "Timeout > 5s sur /api/tickets (p99)"

14:05 ● INVESTIGATION DÉMARRÉE
       Assigné à: alice@team.com
       "Vérification des logs backend"

14:20 ● CAUSE IDENTIFIÉE
       "Query N+1 sur le endpoint /tickets/project/{id}"

14:30 ● FIX DÉPLOYÉ
       "Ajout de fetch join dans la requête JPA"

15:00 ● EN MONITORING
       "Latence revenue à la normale (< 200ms)"

16:15 ● RÉSOLU
       MTTR: 2h15 | Impact: 500 utilisateurs affectés
```

### Métriques incidents
- Nombre d'incidents par mois (tendance)
- MTTR moyen (par sévérité)
- Services les plus impactés
- Causes racines récurrentes

---

## Webhooks

### User Story
> En tant qu'administrateur, je veux configurer des webhooks pour intégrer AgileForge avec nos autres outils (Slack, CI/CD, etc.).

### Événements disponibles

| Événement | Déclencheur |
|-----------|-------------|
| `ticket.created` | Nouveau ticket |
| `ticket.updated` | Modification d'un ticket |
| `ticket.status_changed` | Transition de statut |
| `sprint.started` | Sprint démarré |
| `sprint.completed` | Sprint terminé |
| `release.published` | Release publiée |
| `incident.opened` | Nouvel incident |
| `incident.resolved` | Incident résolu |

### Configuration
```json
{
  "targetUrl": "https://hooks.slack.com/services/T.../B.../xxx",
  "events": ["ticket.status_changed", "incident.opened"],
  "projectId": "uuid-du-projet",
  "secret": "hmac-secret-pour-validation"
}
```

### Payload envoyé
```json
{
  "event": "ticket.status_changed",
  "timestamp": "2026-05-19T14:30:00Z",
  "data": {
    "ticketId": "uuid",
    "ticketKey": "MOB-42",
    "oldStatus": "IN_PROGRESS",
    "newStatus": "DONE",
    "changedBy": "alice@team.com"
  }
}
```

---

## API Keys

### User Story
> En tant que développeur, je veux générer des clés API pour accéder à AgileForge depuis mes scripts et mon CI/CD.

### Fonctionnalités
- Créer une clé avec un nom descriptif et des scopes
- La clé complète n'est affichée qu'une seule fois
- Expiration configurable (30j, 90j, 1 an, jamais)
- Révocation immédiate
- Dernière utilisation tracée

### Scopes

| Scope | Accès |
|-------|-------|
| `read:tickets` | Lecture des tickets |
| `write:tickets` | Création/modification de tickets |
| `read:projects` | Lecture des projets |
| `admin` | Accès complet |

### Utilisation

```bash
# Avec l'API key dans le header
curl -H "X-API-Key: agf_xxxxxxxxxxxxxxxxxxxx" \
     https://api.agileforge.com/tickets/project/{id}
```

### Sécurité
- La clé est hashée en BDD (pas de stockage en clair)
- Seul le préfixe (8 caractères) est affiché dans la liste
- Rate limiting par clé (100 req/min par défaut)
- Audit de chaque utilisation
