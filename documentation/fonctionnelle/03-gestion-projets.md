# Gestion des Projets et Organisations

## Organisations

### User Story
> En tant qu'utilisateur, je veux créer une organisation pour regrouper mes équipes et projets.

### Création d'une organisation
1. L'utilisateur donne un nom (ex: "Acme Corp")
2. Le système génère un slug unique (ex: "acme-corp")
3. Le créateur devient automatiquement ADMIN de l'organisation
4. Il peut ensuite inviter des membres

### Attributs d'une organisation
| Champ | Description | Obligatoire |
|-------|-------------|-------------|
| Nom | Nom affiché | Oui |
| Slug | Identifiant URL unique | Auto-généré |
| Description | Présentation | Non |
| Propriétaire | Créateur (ADMIN) | Auto |

### Gestion des membres
- Inviter par email (avec choix du rôle)
- Modifier le rôle d'un membre existant
- Retirer un membre (il perd l'accès à tous les projets de l'org)
- Un ADMIN ne peut pas se retirer lui-même (il doit transférer la propriété)

---

## Projets

### User Story
> En tant que manager, je veux créer un projet dans mon organisation pour organiser le travail de l'équipe.

### Création d'un projet
1. Choisir l'organisation parente
2. Donner un nom (ex: "Application Mobile")
3. Définir une clé de projet (ex: "MOB") — 2 à 10 caractères, majuscules
4. Ajouter une description optionnelle

### Attributs d'un projet
| Champ | Description | Exemple |
|-------|-------------|---------|
| Nom | Nom complet du projet | "Application Mobile" |
| Clé | Préfixe des tickets | "MOB" → MOB-1, MOB-2... |
| Description | Contexte et objectifs | "App iOS et Android..." |
| Statut | ACTIVE, ARCHIVED, ON_HOLD | ACTIVE |
| Propriétaire | Responsable du projet | UUID |

### Clé de projet
- Unique au sein de l'organisation
- Utilisée comme préfixe des tickets : `{CLÉ}-{numéro}`
- Non modifiable après création (pour cohérence des références)
- Exemples : PROJ-1, MOB-42, API-128

### Membres du projet
- Hérités de l'organisation par défaut
- Possibilité d'ajouter/retirer des membres spécifiquement au projet
- Le rôle au niveau projet peut différer du rôle organisationnel

---

## Cycle de vie d'un projet

```
ACTIVE ──────► ARCHIVED
   │               │
   │               ▼
   └──────► ON_HOLD ──────► ACTIVE
```

| Statut | Signification | Actions possibles |
|--------|---------------|-------------------|
| ACTIVE | Projet en cours | Tout (tickets, sprints, releases) |
| ON_HOLD | Projet en pause | Lecture seule, pas de nouveaux tickets |
| ARCHIVED | Projet terminé | Lecture seule, caché par défaut |

---

## Configuration du projet

### Board Kanban
Le board est automatiquement créé avec des colonnes par défaut :

| Colonne | Statut mappé | Position |
|---------|-------------|----------|
| Backlog | BACKLOG | 1 |
| À faire | TODO | 2 |
| En cours | IN_PROGRESS | 3 |
| En review | IN_REVIEW | 4 |
| Tests | TESTING | 5 |
| Terminé | DONE | 6 |

Les colonnes sont personnalisables : ajout, suppression, réordonnancement, limites WIP.

### Workflows
Chaque projet peut avoir un workflow personnalisé définissant :
- Les transitions autorisées entre statuts
- Les conditions de transition (ex: un ticket IN_REVIEW ne peut passer DONE que si un reviewer a approuvé)
- Les actions automatiques (ex: assigner automatiquement au reporter quand un bug est reporté)

---

## Tableau de bord projet

Le dashboard d'un projet affiche :

| Widget | Données |
|--------|---------|
| Tickets par statut | Compteurs + graphique en anneau |
| Vélocité | Story points livrés par sprint (historique) |
| Sprint actif | Progression, jours restants, burndown |
| Tickets récents | 5 derniers tickets créés/modifiés |
| Membres actifs | Qui a contribué récemment |
| Score qualité moyen | Moyenne des quality scores des tickets |

---

## Relations entre entités

```
Organisation
├── Membres (avec rôles)
├── Projet 1
│   ├── Membres (hérités + spécifiques)
│   ├── Tickets (PROJ-1, PROJ-2, ...)
│   ├── Sprints
│   ├── Board (colonnes)
│   ├── Releases
│   ├── Labels
│   └── Workflow
├── Projet 2
│   └── ...
└── Projet N
```
