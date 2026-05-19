# Documentation AgileForge

## Structure

Ce dossier contient trois types de documentation complémentaires :

---

### Documentation Technique
> Pour les développeurs et architectes qui travaillent sur le code.

| Fichier | Contenu |
|---------|---------|
| [01-architecture.md](technique/01-architecture.md) | Architecture hexagonale, couches, flux de données |
| [02-backend.md](technique/02-backend.md) | Spring Boot, services, repositories, configuration |
| [03-frontend.md](technique/03-frontend.md) | Angular 21, composants, services, routing |
| [04-base-de-donnees.md](technique/04-base-de-donnees.md) | Schéma BDD, tables, migrations Flyway |
| [05-api-reference.md](technique/05-api-reference.md) | Référence API REST (tous les endpoints) |
| [06-deploiement.md](technique/06-deploiement.md) | Docker, Docker Compose, CI/CD |
| [07-securite.md](technique/07-securite.md) | JWT, CORS, RBAC, audit |

---

### Documentation Fonctionnelle
> Pour les Product Owners, chefs de projet et utilisateurs qui veulent comprendre les fonctionnalités.

| Fichier | Contenu |
|---------|---------|
| [01-presentation-generale.md](fonctionnelle/01-presentation-generale.md) | Vision, objectifs, public cible |
| [02-gestion-utilisateurs.md](fonctionnelle/02-gestion-utilisateurs.md) | Auth, profils, rôles, invitations |
| [03-gestion-projets.md](fonctionnelle/03-gestion-projets.md) | Organisations, projets, équipes |
| [04-gestion-tickets.md](fonctionnelle/04-gestion-tickets.md) | Tickets, statuts, commentaires, historique |
| [05-tableaux-sprints.md](fonctionnelle/05-tableaux-sprints.md) | Kanban, sprints, burndown, vélocité |
| [06-fonctionnalites-avancees.md](fonctionnelle/06-fonctionnalites-avancees.md) | Workflows, labels, time tracking, filtres |
| [07-intelligence-ia.md](fonctionnelle/07-intelligence-ia.md) | Assistant IA, prédictions, prompts |
| [08-releases-roadmap.md](fonctionnelle/08-releases-roadmap.md) | Releases, roadmap, intégration Git |
| [09-analytics-metriques.md](fonctionnelle/09-analytics-metriques.md) | DORA, OKR, vélocité, rapports |
| [10-fonctionnalites-enterprise.md](fonctionnelle/10-fonctionnalites-enterprise.md) | Audit, portail client, portfolio, incidents |

---

### Documentation Pédagogique
> Pour apprendre les technologies et patterns utilisés dans le projet.

| Fichier | Contenu |
|---------|---------|
| [01-guide-demarrage.md](pedagogique/01-guide-demarrage.md) | Installation, configuration, premier lancement |
| [02-architecture-hexagonale.md](pedagogique/02-architecture-hexagonale.md) | Tutoriel architecture hexagonale |
| [03-spring-boot-securite.md](pedagogique/03-spring-boot-securite.md) | Tutoriel JWT et Spring Security |
| [04-angular-moderne.md](pedagogique/04-angular-moderne.md) | Standalone components, Signals, inject() |
| [05-patterns-design.md](pedagogique/05-patterns-design.md) | Repository, DTO, Adapter, Strategy, Observer |
| [06-tests-qualite.md](pedagogique/06-tests-qualite.md) | JUnit, Mockito, Karma, Cypress, CI/CD |
| [07-bonnes-pratiques.md](pedagogique/07-bonnes-pratiques.md) | Conventions, erreurs, logging, validation |
| [08-exercices-pratiques.md](pedagogique/08-exercices-pratiques.md) | 4 exercices progressifs (débutant → expert) |

---

## Comment utiliser cette documentation

| Je suis... | Je commence par... |
|-----------|-------------------|
| Nouveau sur le projet | Pédagogique > Guide de démarrage |
| Développeur backend | Technique > Backend + API Reference |
| Développeur frontend | Technique > Frontend + Pédagogique > Angular moderne |
| Product Owner | Fonctionnelle > Présentation + Gestion tickets |
| Architecte | Technique > Architecture + Pédagogique > Patterns |
| DevOps | Technique > Déploiement + Sécurité |
| Client | Fonctionnelle > Enterprise > Portail client |
