# Releases, Roadmap et Intégration Git

## Gestion des Releases

### User Story
> En tant que release manager, je veux gérer les versions du produit avec leurs changelogs pour communiquer sur les livraisons.

### Cycle de vie d'une release

```
DRAFT ──────► PLANNED ──────► RELEASED
  │                              │
  └──────── CANCELLED ◄─────────┘
```

| Statut | Description |
|--------|-------------|
| DRAFT | En préparation, tickets non fixés |
| PLANNED | Périmètre défini, date prévue |
| RELEASED | Publiée, accessible aux utilisateurs |
| CANCELLED | Annulée |

### Attributs d'une release

| Champ | Description | Exemple |
|-------|-------------|---------|
| Version | Numéro sémantique | "2.3.0" |
| Nom | Nom de code (optionnel) | "Phoenix" |
| Description | Notes de release | "Ajout du dark mode et correction..." |
| Date prévue | Date de livraison cible | 2026-06-01 |
| Date effective | Date réelle de publication | 2026-06-03 |
| Tickets inclus | Liste des tickets livrés | MOB-42, MOB-43, MOB-45 |

### Changelog automatique
Le changelog est généré automatiquement à partir des tickets inclus :

```markdown
## v2.3.0 - Phoenix (2026-06-03)

### Nouvelles fonctionnalités
- [MOB-42] Implémenter le dark mode
- [MOB-43] Ajouter le support des notifications push

### Corrections de bugs
- [MOB-45] Corriger le crash au login iOS 17
- [MOB-47] Résoudre le memory leak sur la page board

### Améliorations
- [MOB-50] Optimiser le chargement du backlog (+40%)
```

Le groupement se fait par type de ticket (STORY → Features, BUG → Fixes, TASK → Improvements).

---

## Roadmap

### User Story
> En tant que Product Owner, je veux visualiser la roadmap pour communiquer le plan de livraison à l'équipe et aux parties prenantes.

### Vue Roadmap
Représentation temporelle des jalons et releases :

```
    Mai 2026        Juin 2026        Juil 2026       Août 2026
    ─────────────── ──────────────── ─────────────── ────────────
    [  Sprint 5  ]  [  Sprint 6   ] [  Sprint 7  ]
    ═══════════════════════════
    ▓▓ v2.3 Auth ▓▓
                    ═══════════════════════════════
                    ▓▓▓ v2.4 Analytics ▓▓▓
                                     ═══════════════════════════
                                     ▓▓▓▓ v3.0 Enterprise ▓▓▓▓
```

### Jalons (Milestones)

| Champ | Description |
|-------|-------------|
| Nom | Nom du jalon |
| Date cible | Quand il doit être atteint |
| Description | Critères de réussite |
| Tickets associés | Travail nécessaire |
| Progression | % des tickets terminés |

### Dépendances entre jalons
Un jalon peut dépendre d'un autre :
```
"API v2" ──bloque──► "Mobile App v2" ──bloque──► "Release publique"
```

Les dépendances sont visualisées sur la roadmap pour identifier le chemin critique.

---

## Intégration Git

### User Story
> En tant que développeur, je veux voir les branches, commits et pull requests liés à un ticket directement dans AgileForge.

### Connexion d'un repository

1. Aller dans les paramètres du projet
2. Section "Intégration Git"
3. Connecter un repository GitHub/GitLab
4. AgileForge récupère les métadonnées (branches, commits, PRs)

### Liaison automatique ticket ↔ code

La liaison se fait par convention de nommage :
- **Branche** : `feature/MOB-42-dark-mode` → liée au ticket MOB-42
- **Commit** : `fix(MOB-45): resolve iOS crash` → lié au ticket MOB-45
- **PR** : Titre contenant "MOB-42" → liée au ticket MOB-42

### Informations affichées sur un ticket

```
Ticket MOB-42: "Implémenter le dark mode"

──── Activité Git ────────────────────────────
📁 Branche: feature/MOB-42-dark-mode
   Créée il y a 3 jours par alice

📝 Commits (4):
   • abc1234 - feat: add theme toggle component
   • def5678 - feat: implement CSS variables switching
   • ghi9012 - test: add theme service tests
   • jkl3456 - fix: persist theme choice in localStorage

🔀 Pull Request: #127 "Implement dark mode"
   Status: En review (2 approvals, 0 rejections)
   CI: ✅ Tous les checks passent
```

### Statuts des Pull Requests

| Statut | Description |
|--------|-------------|
| OPEN | PR créée, en attente de review |
| IN_REVIEW | Reviews en cours |
| APPROVED | Approuvée, prête à merger |
| MERGED | Fusionnée dans la branche principale |
| CLOSED | Fermée sans merge |

### Transition automatique
Quand une PR liée à un ticket est mergée :
- Le ticket peut automatiquement passer à IN_REVIEW ou TESTING
- Un commentaire est ajouté au ticket avec le lien vers la PR
- Le code est associé à la release correspondante
