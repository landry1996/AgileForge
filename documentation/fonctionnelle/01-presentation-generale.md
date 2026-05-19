# Présentation Générale d'AgileForge

## Vision

AgileForge est une **plateforme de gestion de projets agiles** conçue pour les équipes de développement logiciel. Elle combine les fonctionnalités d'outils comme Jira, Linear et Notion dans une solution unifiée, enrichie par l'intelligence artificielle.

---

## Objectifs

1. **Simplifier la gestion de projet** — Interface intuitive, workflows personnalisables
2. **Améliorer la visibilité** — Tableaux de bord, métriques en temps réel
3. **Accélérer les livraisons** — Prédictions IA, détection des goulots d'étranglement
4. **Favoriser la collaboration** — Commentaires, notifications, portail client
5. **Garantir la traçabilité** — Historique complet, audit trail, conformité

---

## Public cible

| Persona | Besoins |
|---------|---------|
| **Chef de projet / Scrum Master** | Vue d'ensemble, sprint planning, burndown, vélocité |
| **Développeur** | Backlog clair, intégration Git, suivi du temps |
| **Product Owner** | Roadmap, priorisation, communication client |
| **Direction / CTO** | Portfolio, DORA metrics, OKR, rapports |
| **Client externe** | Portail de suivi, feedback |

---

## Versions du produit

### v1.0 — Core (Fondations)
- Authentification et gestion des utilisateurs
- Organisations et projets
- Gestion complète des tickets (CRUD, commentaires, historique)
- Tableaux Kanban et sprints
- Notifications et recherche
- Assistant IA

### v2.0 — Advanced (Productivité)
- Moteur de workflows personnalisables
- Labels et catégorisation
- Gestion des pièces jointes
- Suivi du temps (time tracking)
- Invitations par email
- Filtres sauvegardés

### v3.0 — Intelligence (IA & Analytics)
- Gestion des releases et changelogs
- Roadmap et jalons
- Intégration Git (branches, commits, PRs)
- Dashboard analytics (vélocité, cycle time)
- Générateur de prompts Claude Code
- Module documentation (wiki)
- Base de connaissances (mémoire projet)
- OKR (Objectifs et Résultats Clés)
- Métriques DORA
- Moteur de prédiction (dates de livraison)

### v4.0 — Enterprise (Gouvernance)
- Audit trail renforcé avec alertes
- Portail client (stakeholders externes)
- Gestion de portfolio multi-projets
- Planification de capacité
- Gestion des incidents
- Webhooks (intégrations événementielles)
- API Keys (accès programmatique)

---

## Valeur ajoutée

| Fonctionnalité | Avantage concurrentiel |
|----------------|----------------------|
| Assistant IA intégré | Suggestions automatiques, détection d'anomalies |
| Score qualité des tickets | Encourage la rédaction de tickets exploitables |
| Prédictions de livraison | Dates réalistes basées sur l'historique |
| Portail client | Communication transparente sans accès au backlog interne |
| Métriques DORA | Mesure objective de la performance DevOps |
| Générateur de prompts | Productivité développeurs avec les LLMs |

---

## Architecture fonctionnelle

```
┌─────────────────────────────────────────────────────────────┐
│                    UTILISATEURS                               │
│  Développeurs | Managers | POs | Direction | Clients         │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│                    AGILEFORGE                                 │
│                                                              │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │ Planning │  │  Develo- │  │  Outils  │  │  Manage- │   │
│  │          │  │  ppement │  │          │  │  ment    │   │
│  │• Board   │  │• Tickets │  │• Analyt. │  │• Settings│   │
│  │• Backlog │  │• Releases│  │• Time    │  │• Audit   │   │
│  │• Sprint  │  │• Git     │  │• Docs    │  │• Portail │   │
│  │• Roadmap │  │• AI      │  │• Knowl.  │  │• Portf.  │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐   │
│  │              INTELLIGENCE ARTIFICIELLE                │   │
│  │  Suggestions | Prédictions | Qualité | Prompts       │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

---

## Glossaire

| Terme | Définition |
|-------|-----------|
| **Ticket** | Unité de travail (story, bug, task, epic) |
| **Sprint** | Itération de développement (1-4 semaines) |
| **Backlog** | Liste priorisée de tickets à réaliser |
| **Epic** | Grand ensemble de travail regroupant plusieurs tickets |
| **Vélocité** | Nombre de story points livrés par sprint |
| **Burndown** | Graphique montrant le travail restant dans un sprint |
| **DORA** | DevOps Research and Assessment — 4 métriques clés |
| **OKR** | Objectives and Key Results — framework de gestion des objectifs |
| **Cycle time** | Temps entre le début et la fin du travail sur un ticket |
| **WIP** | Work In Progress — nombre de tickets en cours simultanément |
