# Intelligence Artificielle

## Assistant IA

### User Story
> En tant que membre de l'équipe, je veux accéder à un assistant IA pour m'aider à rédiger des tickets, analyser les blocages et obtenir des suggestions.

### Fonctionnalités

| Capacité | Description | Exemple |
|----------|-------------|---------|
| Suggestions de titre | Améliorer la formulation | "fix bug" → "Corriger le crash au login sur les appareils iOS 17+" |
| Analyse de sprint | Identifier les risques | "3 tickets bloqués depuis 5 jours, risque de non-livraison" |
| Score qualité | Évaluer la rédaction d'un ticket | "Score 45/100 : manque description et estimation" |
| Décomposition | Découper un epic en stories | Epic → 5-10 stories estimées |
| Détection de doublons | Trouver les tickets similaires | "Ce ticket ressemble à MOB-23 (résolu)" |

### Interface
L'assistant IA est accessible :
- Via une page dédiée (chat interactif)
- Via un bouton contextuel sur chaque ticket
- Via l'analyse automatique à la création d'un ticket

---

## Score Qualité des Tickets

### Principe
Chaque ticket reçoit un score de 0 à 100 calculé automatiquement selon la complétude de ses informations.

### Critères de scoring

| Critère | Points | Condition |
|---------|--------|-----------|
| Titre descriptif | +20 | Plus de 10 caractères |
| Description détaillée | +30 | Plus de 50 caractères |
| Priorité définie | +15 | Champ non vide |
| Estimation (points) | +15 | Story points renseignés |
| Assigné | +20 | Quelqu'un est responsable |

### Interprétation

| Score | Qualité | Action |
|-------|---------|--------|
| 0-30 | Insuffisant | Ticket à compléter avant de commencer |
| 31-60 | Acceptable | Peut être traité mais manque d'infos |
| 61-80 | Bon | Ticket bien rédigé |
| 81-100 | Excellent | Ticket complet et actionnable |

### Affichage
Le score est affiché sur chaque carte ticket (badge coloré) et dans la vue détail.

---

## Moteur de Prédiction

### User Story
> En tant que Product Owner, je veux savoir quand une fonctionnalité sera probablement livrée, basé sur les données historiques.

### Algorithme
Le moteur utilise les données historiques pour prédire les dates de livraison :

1. **Calcul de la vélocité** : Moyenne des 3-5 derniers sprints
2. **Estimation du travail restant** : Somme des story points non réalisés
3. **Prédiction** : Nombre de sprints restants = Travail / Vélocité
4. **Intervalle de confiance** : Basé sur la variance de la vélocité

### Exemple de prédiction

```
Epic: "Module de paiement"
├── Points restants: 45 story points
├── Vélocité moyenne: 35 pts/sprint (± 5)
├── Durée sprint: 2 semaines
│
├── Estimation optimiste: 1 sprint (2 semaines) → 02/06/2026
├── Estimation réaliste:  1.5 sprints (3 semaines) → 09/06/2026
└── Estimation pessimiste: 2 sprints (4 semaines) → 16/06/2026
```

### Facteurs pris en compte
- Vélocité historique (tendance)
- Taille de l'équipe (capacité)
- Tickets bloqués (risques)
- Scope creep historique (tickets ajoutés en cours de sprint)

---

## Générateur de Prompts Claude Code

### User Story
> En tant que développeur, je veux générer des prompts contextuels pour Claude Code afin d'accélérer mon développement.

### Fonctionnalités
- Templates de prompts prédéfinis (fix bug, add feature, refactor, test)
- Contexte automatique : inclut les infos du ticket (titre, description, composant)
- Personnalisation : ajuster le prompt avant de le copier
- Historique des prompts générés

### Templates disponibles

| Template | Usage | Contexte inclus |
|----------|-------|-----------------|
| Implémenter une feature | Nouveau développement | Titre, description, composant, tech stack |
| Corriger un bug | Debugging | Titre, environnement, étapes de reproduction |
| Écrire des tests | Testing | Composant, classes à tester |
| Refactorer | Amélioration | Fichiers concernés, problème identifié |
| Documenter | Documentation | Module, API endpoints |

### Exemple de prompt généré

```
Contexte: Projet AgileForge, module backend Java 21 / Spring Boot 3.4
Ticket: MOB-42 "Implémenter le dark mode"
Composant: frontend/components
Tech: Angular 21, standalone components, Signals

Objectif: Implémenter un toggle dark/light mode dans la barre de navigation
qui persiste le choix dans localStorage et applique les CSS variables appropriées.

Contraintes:
- Utiliser les Angular Signals pour l'état du thème
- Variables CSS existantes: --bg-primary, --bg-secondary, --text-primary
- Composant standalone avec inline template
```

---

## Base de Connaissances (Mémoire Projet)

### User Story
> En tant que membre de l'équipe, je veux documenter les décisions techniques et les leçons apprises pour que l'équipe puisse les retrouver.

### Types d'entrées

| Type | Usage | Exemple |
|------|-------|---------|
| Décision | Choix technique documenté | "Pourquoi on a choisi PostgreSQL plutôt que MongoDB" |
| Leçon apprise | Retour d'expérience | "Les migrations Flyway doivent être testées sur une copie" |
| Convention | Règle d'équipe | "Tous les DTOs sont des Java records" |
| Architecture | Choix structurel | "Architecture hexagonale pour isoler le domaine" |

### Fonctionnalités
- Recherche full-text dans la base de connaissances
- Tags pour catégoriser (backend, frontend, infra, process)
- Liens vers les tickets associés
- Historique des modifications

### Intégration IA
L'assistant IA consulte automatiquement la base de connaissances pour :
- Suggérer des solutions basées sur les décisions passées
- Alerter quand une proposition va à l'encontre d'une convention
- Enrichir le contexte des prompts générés
