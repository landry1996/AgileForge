# Gestion des Tickets

## Qu'est-ce qu'un ticket ?

Un ticket est l'unité de travail fondamentale dans AgileForge. Il représente une tâche, une fonctionnalité, un bug, ou un ensemble de travail à réaliser.

---

## Types de tickets

| Type | Icône | Usage | Exemple |
|------|-------|-------|---------|
| **EPIC** | | Grand ensemble de travail (plusieurs sprints) | "Refonte du module de paiement" |
| **STORY** | | Fonctionnalité utilisateur | "En tant qu'utilisateur, je veux..." |
| **BUG** | | Défaut à corriger | "Crash au login sur iOS 17" |
| **TASK** | | Travail technique sans valeur utilisateur directe | "Migrer la BDD vers PostgreSQL 16" |
| **SUBTASK** | | Sous-tâche d'un ticket parent | "Écrire les tests unitaires" |

### Hiérarchie
```
EPIC
├── STORY
│   ├── SUBTASK
│   └── SUBTASK
├── STORY
└── BUG
    └── SUBTASK
```

---

## Cycle de vie (statuts)

```
                    ┌──────────────┐
                    │   BACKLOG    │ (Nouveau ticket)
                    └──────┬───────┘
                           │
                    ┌──────▼───────┐
                    │     TODO     │ (Priorisé, prêt)
                    └──────┬───────┘
                           │
                    ┌──────▼───────┐
                    │ IN_PROGRESS  │ (En cours de dev)
                    └──────┬───────┘
                           │
                    ┌──────▼───────┐
                    │  IN_REVIEW   │ (Code review)
                    └──────┬───────┘
                           │
                    ┌──────▼───────┐
                    │   TESTING    │ (Tests QA)
                    └──────┬───────┘
                           │
              ┌────────────┼────────────┐
              ▼                         ▼
       ┌──────────┐             ┌───────────┐
       │   DONE   │             │ CANCELLED │
       └──────────┘             └───────────┘
```

| Statut | Signification |
|--------|---------------|
| BACKLOG | Créé mais pas encore planifié |
| TODO | Priorisé, prêt à être pris en charge |
| IN_PROGRESS | Travail en cours |
| IN_REVIEW | En revue de code |
| TESTING | En phase de test/QA |
| DONE | Terminé et validé |
| CANCELLED | Annulé (ne sera pas fait) |

---

## Priorités

| Priorité | Signification | SLA indicatif |
|----------|---------------|---------------|
| CRITICAL | Bloquant, impact production | < 4 heures |
| HIGH | Important, à traiter en priorité | < 2 jours |
| MEDIUM | Normal, planifié normalement | Sprint courant |
| LOW | Nice to have, quand possible | Backlog |

---

## Création d'un ticket

### User Story
> En tant que développeur, je veux créer un ticket pour documenter le travail à faire.

### Champs

| Champ | Obligatoire | Description |
|-------|-------------|-------------|
| Titre | Oui | 5-500 caractères, descriptif |
| Type | Oui | STORY, BUG, TASK, EPIC, SUBTASK |
| Description | Non | Détail, critères d'acceptation |
| Priorité | Non (défaut: MEDIUM) | LOW, MEDIUM, HIGH, CRITICAL |
| Assigné | Non | Membre responsable |
| Epic | Non | Epic parent |
| Parent | Non | Ticket parent (pour les subtasks) |
| Story Points | Non | Estimation d'effort (1, 2, 3, 5, 8, 13, 21) |
| Heures estimées | Non | Estimation en heures |
| Date d'échéance | Non | Deadline |
| Environnement | Non | Concerné (pour les bugs) |
| Composant | Non | Module/service impacté |
| Labels | Non | Tags de catégorisation |

### Score qualité

Chaque ticket reçoit un score de qualité (0-100) calculé automatiquement :

| Critère | Points |
|---------|--------|
| Titre > 10 caractères | +20 |
| Description > 50 caractères | +30 |
| Priorité définie | +15 |
| Story points estimés | +15 |
| Assigné à quelqu'un | +20 |

Un bon ticket (score > 80) est plus facile à comprendre et à réaliser.

---

## Transitions de statut

### Règles par défaut
- Un ticket ne peut pas revenir au même statut
- Seuls les membres avec rôle DEVELOPER+ peuvent changer le statut
- Chaque transition est enregistrée dans l'historique

### Notifications
Quand un ticket change de statut :
- L'assigné reçoit une notification
- Le reporter reçoit une notification
- Les watchers reçoivent une notification

---

## Commentaires

### User Story
> En tant que membre de l'équipe, je veux commenter un ticket pour communiquer sur l'avancement.

### Règles
- Tout membre du projet peut commenter (sauf VIEWER)
- Un auteur peut supprimer son propre commentaire
- Les commentaires sont ordonnés chronologiquement
- Le contenu supporte le texte brut

---

## Historique

Chaque modification d'un ticket est tracée :

| Champ modifié | Ancienne valeur | Nouvelle valeur | Par | Date |
|---------------|----------------|-----------------|-----|------|
| status | BACKLOG | IN_PROGRESS | alice@... | 19/05 10:30 |
| assignee | - | bob@... | alice@... | 19/05 10:30 |
| priority | MEDIUM | HIGH | alice@... | 19/05 14:15 |
| sprint | - | Sprint 5 | scrum@... | 20/05 09:00 |

---

## Liens entre tickets

| Type de lien | Signification | Exemple |
|-------------|---------------|---------|
| BLOCKS | Ce ticket bloque l'autre | "API login" bloque "UI login" |
| IS_BLOCKED_BY | Ce ticket est bloqué par l'autre | Inverse du précédent |
| RELATES_TO | Relation informationnelle | Tickets liés au même sujet |
| DUPLICATES | Ce ticket est un doublon | Bug reporté deux fois |
| IS_DUPLICATED_BY | Inverse de DUPLICATES | |

---

## Suivi du temps

### Logger du temps
- Indiquer les heures passées sur un ticket
- Le temps est cumulatif (s'ajoute aux heures déjà loggées)
- Permet de comparer temps estimé vs temps réel

### Indicateurs
| Métrique | Calcul |
|----------|--------|
| Temps estimé | Champ `estimatedHours` |
| Temps passé | Somme des `loggedHours` |
| Écart | Passé - Estimé (positif = dépassement) |

---

## Recherche et filtres

### Recherche full-text
Recherche dans le titre et la description de tous les tickets d'un projet.

### Filtres disponibles
| Filtre | Valeurs |
|--------|---------|
| Statut | BACKLOG, TODO, IN_PROGRESS, IN_REVIEW, TESTING, DONE, CANCELLED |
| Type | STORY, BUG, TASK, EPIC, SUBTASK |
| Priorité | LOW, MEDIUM, HIGH, CRITICAL |
| Assigné | Liste des membres |
| Sprint | Sprint actif, sprints passés |
| Epic | Liste des epics |
| Labels | Tags définis |

### Filtres sauvegardés
Les utilisateurs peuvent sauvegarder des combinaisons de filtres pour y accéder rapidement (ex: "Mes bugs critiques", "Tickets sans estimation").
