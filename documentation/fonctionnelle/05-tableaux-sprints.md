# Tableaux Kanban et Sprints

## Tableau Kanban

### User Story
> En tant que membre de l'équipe, je veux visualiser l'état des tickets sur un tableau Kanban pour suivre l'avancement.

### Description
Le board Kanban offre une vue visuelle de tous les tickets d'un projet, organisés en colonnes correspondant aux statuts.

### Colonnes par défaut

```
┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐
│ BACKLOG  │ │   TODO   │ │IN PROGRESS│ │IN REVIEW │ │ TESTING  │ │   DONE   │
├──────────┤ ├──────────┤ ├──────────┤ ├──────────┤ ├──────────┤ ├──────────┤
│ MOB-12   │ │ MOB-8    │ │ MOB-5    │ │ MOB-3    │ │ MOB-1    │ │ MOB-2    │
│ MOB-13   │ │ MOB-9    │ │ MOB-6    │ │          │ │          │ │ MOB-4    │
│ MOB-14   │ │          │ │          │ │          │ │          │ │ MOB-7    │
│ MOB-15   │ │          │ │          │ │          │ │          │ │          │
└──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘
```

### Personnalisation des colonnes
| Action | Description |
|--------|-------------|
| Ajouter une colonne | Créer un nouveau statut intermédiaire |
| Renommer | Changer le nom affiché |
| Réordonner | Déplacer la position d'une colonne |
| Supprimer | Retirer une colonne (tickets déplacés) |
| Limite WIP | Nombre max de tickets simultanés |

### Limites WIP (Work In Progress)
- Définir un nombre maximum de tickets par colonne
- Quand la limite est atteinte, la colonne est visuellement marquée
- Objectif : éviter le multitasking excessif

### Interactions
- **Glisser-déposer** : Déplacer un ticket d'une colonne à l'autre (= transition de statut)
- **Clic sur un ticket** : Ouvrir le détail
- **Filtrer** : Par assigné, type, priorité, sprint

---

## Backlog

### User Story
> En tant que Product Owner, je veux gérer le backlog pour prioriser les tickets.

### Description
Le backlog est la liste ordonnée de tous les tickets non terminés d'un projet.

### Vue backlog
| Colonne | Description |
|---------|-------------|
| Clé | MOB-42 |
| Titre | Nom du ticket |
| Type | Story/Bug/Task |
| Priorité | Indicateur visuel |
| Points | Story points |
| Assigné | Avatar du membre |
| Sprint | Sprint associé (ou "Non planifié") |

### Actions
- **Créer un ticket** depuis le backlog
- **Réordonner** par drag-and-drop (définit la priorité)
- **Assigner à un sprint** (drag vers la zone sprint)
- **Estimation rapide** : modifier les story points en ligne

---

## Sprints

### User Story
> En tant que Scrum Master, je veux gérer les sprints pour organiser les itérations de développement.

### Cycle de vie d'un sprint

```
PLANNED ──────► ACTIVE ──────► COMPLETED
(configuration)  (en cours)    (terminé)
```

### Création d'un sprint
| Champ | Description | Obligatoire |
|-------|-------------|-------------|
| Nom | Ex: "Sprint 5 - Auth Module" | Oui |
| Objectif | But du sprint | Non |
| Date de début | Premier jour | Oui (au démarrage) |
| Date de fin | Dernier jour | Oui (au démarrage) |

### Planification (Sprint Planning)
1. Le sprint est en statut **PLANNED**
2. Le PO/SM déplace des tickets du backlog vers le sprint
3. L'équipe estime les tickets (story points)
4. La capacité est vérifiée (vélocité historique)

### Démarrage
1. Le SM clique "Démarrer le sprint"
2. Les dates de début/fin sont confirmées
3. Le statut passe à **ACTIVE**
4. Un seul sprint peut être actif à la fois par projet

### Déroulement
- Les tickets sont déplacés sur le board (colonne par colonne)
- Le burndown chart se met à jour quotidiennement
- Les membres loguent leur temps

### Clôture
1. Le SM clique "Terminer le sprint"
2. Les tickets DONE sont marqués comme livrés
3. Les tickets non terminés retournent au backlog
4. La vélocité est calculée (somme des points des tickets DONE)
5. Le statut passe à **COMPLETED**

---

## Burndown Chart

### Description
Graphique montrant la quantité de travail restant par rapport au temps.

```
Points
restants
  │\
  │ \
  │  \        ← Ligne idéale (droite)
  │   \
  │    ·····
  │         ·····  ← Réel (souvent en escalier)
  │              ·····
  │                   ····
  └──────────────────────────── Jours
  J1   J2   J3   J4   J5   ...  J10
```

### Données affichées
- **Ligne idéale** : Décroissance linéaire du début à la fin du sprint
- **Ligne réelle** : Points restants chaque jour (basé sur les tickets non DONE)
- **Variance** : Écart entre idéal et réel

### Interprétation
| Forme | Signification |
|-------|---------------|
| Réel sous l'idéal | En avance |
| Réel au-dessus | En retard |
| Plateau | Travail bloqué, pas de progression |
| Augmentation | Tickets ajoutés en cours de sprint (scope creep) |

---

## Vélocité

### Définition
La vélocité est le nombre de story points livrés (statut DONE) par sprint.

### Calcul
```
Vélocité Sprint N = Σ story_points des tickets DONE dans le Sprint N
```

### Usage
| Application | Comment |
|-------------|---------|
| Planification | Ne pas dépasser la vélocité moyenne dans le prochain sprint |
| Prédiction | Estimer les dates de livraison futures |
| Amélioration | Suivre l'évolution de la productivité |

### Graphique vélocité
```
Points    ┌───┐
livrés    │ 42│     ┌───┐
          │   │     │ 38│ ┌───┐     ┌───┐
          │   │┌───┐│   │ │ 35│┌───┐│ 40│
          │   ││ 30││   │ │   ││ 32││   │
          └───┘└───┘└───┘ └───┘└───┘└───┘
         Sprint Sprint Sprint Sprint Sprint Sprint
           1      2      3      4      5      6

Moyenne: 36 points/sprint
```

---

## Métriques de sprint

| Métrique | Description | Calcul |
|----------|-------------|--------|
| Vélocité | Points livrés | Σ points DONE |
| Engagement | Points planifiés | Σ points au début du sprint |
| Complétion | % de tickets terminés | DONE / Total * 100 |
| Scope change | Tickets ajoutés/retirés | Différence début vs fin |
| Carry-over | Tickets reportés | Tickets non-DONE en fin de sprint |
