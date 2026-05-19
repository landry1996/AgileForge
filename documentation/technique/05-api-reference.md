# Référence API REST

## Informations générales

- **Base URL** : `http://localhost:8080/api`
- **Format** : JSON
- **Authentification** : Bearer Token (JWT) sur tous les endpoints sauf `/auth/**`
- **Documentation interactive** : `http://localhost:8080/swagger-ui.html`

---

## Authentification

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/auth/register` | Inscription d'un nouvel utilisateur |
| POST | `/auth/login` | Connexion (retourne access + refresh token) |
| POST | `/auth/refresh` | Renouvellement du access token |

### POST /auth/register
```json
// Request
{ "firstName": "John", "lastName": "Doe", "email": "john@example.com", "password": "SecurePass123!" }
// Response 201
{ "token": "eyJ...", "refreshToken": "eyJ...", "user": { "id": "uuid", "email": "...", "firstName": "..." } }
```

### POST /auth/login
```json
// Request
{ "email": "john@example.com", "password": "SecurePass123!" }
// Response 200
{ "token": "eyJ...", "refreshToken": "eyJ..." }
```

---

## Tickets

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/tickets/project/{projectId}` | Créer un ticket |
| GET | `/tickets/{id}` | Récupérer un ticket |
| GET | `/tickets/project/{projectId}` | Lister les tickets d'un projet |
| GET | `/tickets/project/{projectId}/status/{status}` | Filtrer par statut |
| GET | `/tickets/my` | Mes tickets assignés |
| GET | `/tickets/sprint/{sprintId}` | Tickets d'un sprint |
| GET | `/tickets/epic/{epicId}` | Tickets d'un epic |
| PUT | `/tickets/{id}` | Mettre à jour un ticket |
| PATCH | `/tickets/{id}/transition/{status}` | Transition de statut |
| POST | `/tickets/{id}/comments` | Ajouter un commentaire |
| GET | `/tickets/{id}/comments` | Lister les commentaires |
| DELETE | `/tickets/comments/{commentId}` | Supprimer un commentaire |
| GET | `/tickets/{id}/history` | Historique du ticket |
| POST | `/tickets/{id}/log-time` | Logger du temps |

### POST /tickets/project/{projectId}
```json
// Request
{
  "title": "Implement dark mode",
  "description": "Add dark theme support across all components",
  "type": "STORY",
  "priority": "HIGH",
  "assigneeId": "uuid-optional",
  "storyPoints": 5,
  "estimatedHours": 8.0,
  "dueDate": "2026-06-15",
  "labels": "frontend,ui"
}
// Response 201
{
  "id": "uuid",
  "projectId": "uuid",
  "fullKey": "PROJ-42",
  "key": "PROJ",
  "number": 42,
  "title": "Implement dark mode",
  "status": "BACKLOG",
  "priority": "HIGH",
  "qualityScore": 85,
  "createdAt": "2026-05-19T10:30:00"
}
```

---

## Projets

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/projects` | Créer un projet |
| GET | `/projects/{id}` | Récupérer un projet |
| GET | `/projects/organization/{orgId}` | Projets d'une organisation |
| PUT | `/projects/{id}` | Mettre à jour |
| POST | `/projects/{id}/members` | Ajouter un membre |
| DELETE | `/projects/{id}/members/{userId}` | Retirer un membre |

---

## Organisations

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/organizations` | Créer une organisation |
| GET | `/organizations/{id}` | Récupérer |
| GET | `/organizations/my` | Mes organisations |
| PUT | `/organizations/{id}` | Mettre à jour |
| GET | `/organizations/{id}/members` | Lister les membres |

---

## Sprints

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/sprints/project/{projectId}` | Créer un sprint |
| GET | `/sprints/{id}` | Récupérer |
| GET | `/sprints/project/{projectId}` | Sprints d'un projet |
| GET | `/sprints/project/{projectId}/active` | Sprint actif |
| PUT | `/sprints/{id}` | Mettre à jour |
| PATCH | `/sprints/{id}/start` | Démarrer un sprint |
| PATCH | `/sprints/{id}/complete` | Terminer un sprint |

---

## Board (Kanban)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/board/project/{projectId}/columns` | Colonnes du board |
| POST | `/board/project/{projectId}/columns` | Créer une colonne |
| PUT | `/board/columns/{id}` | Modifier une colonne |
| DELETE | `/board/columns/{id}` | Supprimer une colonne |

---

## Notifications

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/notifications` | Mes notifications |
| GET | `/notifications/unread-count` | Compteur non lues |
| PATCH | `/notifications/{id}/read` | Marquer comme lue |
| PATCH | `/notifications/read-all` | Tout marquer comme lu |

---

## Recherche

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/search?q={query}&projectId={id}` | Recherche full-text |

---

## IA Assistant

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/ai/suggest` | Suggestions IA pour un ticket |
| POST | `/ai/analyze-sprint` | Analyse de sprint |
| POST | `/ai/quality-check/{ticketId}` | Vérification qualité |

---

## Workflow

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/workflows/project/{projectId}` | Workflow d'un projet |
| POST | `/workflows/project/{projectId}` | Créer/configurer un workflow |
| GET | `/workflows/{id}/transitions` | Transitions possibles |

---

## Labels

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/labels/project/{projectId}` | Labels d'un projet |
| POST | `/labels/project/{projectId}` | Créer un label |
| PUT | `/labels/{id}` | Modifier |
| DELETE | `/labels/{id}` | Supprimer |

---

## Pièces jointes

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/attachments/ticket/{ticketId}` | Uploader un fichier |
| GET | `/attachments/ticket/{ticketId}` | Lister les pièces jointes |
| GET | `/attachments/{id}/download` | Télécharger |
| DELETE | `/attachments/{id}` | Supprimer |

---

## Suivi du temps

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/time-tracking/entries` | Logger du temps |
| GET | `/time-tracking/ticket/{ticketId}` | Entrées d'un ticket |
| GET | `/time-tracking/my` | Mes entrées |
| GET | `/time-tracking/report` | Rapport de temps |
| DELETE | `/time-tracking/entries/{id}` | Supprimer une entrée |

---

## Invitations

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/invitations` | Inviter par email |
| GET | `/invitations/pending` | Invitations en attente |
| PATCH | `/invitations/{id}/accept` | Accepter |
| PATCH | `/invitations/{id}/decline` | Refuser |

---

## Filtres sauvegardés

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/filters` | Créer un filtre |
| GET | `/filters/my` | Mes filtres |
| GET | `/filters/{id}/execute` | Exécuter un filtre |
| DELETE | `/filters/{id}` | Supprimer |

---

## Releases

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/releases/project/{projectId}` | Créer une release |
| GET | `/releases/project/{projectId}` | Lister les releases |
| GET | `/releases/{id}` | Détail |
| PUT | `/releases/{id}` | Modifier |
| PATCH | `/releases/{id}/publish` | Publier |
| GET | `/releases/{id}/changelog` | Changelog généré |

---

## Roadmap

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/roadmap/project/{projectId}` | Vue roadmap |
| POST | `/roadmap/milestones` | Créer un jalon |
| PUT | `/roadmap/milestones/{id}` | Modifier |
| POST | `/roadmap/dependencies` | Ajouter une dépendance |

---

## Intégration Git

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/git/repositories` | Connecter un repo |
| GET | `/git/repositories/project/{projectId}` | Repos d'un projet |
| GET | `/git/branches/{repoId}` | Branches |
| GET | `/git/commits/{repoId}` | Commits |
| GET | `/git/pull-requests/{repoId}` | Pull requests |

---

## Analytics

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/analytics/project/{projectId}/velocity` | Vélocité |
| GET | `/analytics/project/{projectId}/burndown` | Burndown chart |
| GET | `/analytics/project/{projectId}/cycle-time` | Cycle time |
| GET | `/analytics/project/{projectId}/cumulative-flow` | CFD |

---

## Prompt Generator

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/prompts/templates` | Templates disponibles |
| POST | `/prompts/generate` | Générer un prompt |

---

## Documents

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/documents/project/{projectId}` | Créer un document |
| GET | `/documents/project/{projectId}` | Lister |
| GET | `/documents/{id}` | Lire |
| PUT | `/documents/{id}` | Modifier |
| GET | `/documents/{id}/versions` | Historique versions |

---

## Knowledge Base

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/knowledge/project/{projectId}` | Ajouter une entrée |
| GET | `/knowledge/project/{projectId}` | Lister |
| GET | `/knowledge/search?q={query}` | Rechercher |
| PUT | `/knowledge/{id}` | Modifier |

---

## OKR

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/okr/objectives` | Créer un objectif |
| GET | `/okr/objectives/project/{projectId}` | Lister |
| POST | `/okr/objectives/{id}/key-results` | Ajouter un KR |
| PATCH | `/okr/key-results/{id}/progress` | Mettre à jour progression |

---

## DORA Metrics

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/dora/project/{projectId}/metrics` | Métriques DORA |
| GET | `/dora/project/{projectId}/deployment-frequency` | Fréquence déploiement |
| GET | `/dora/project/{projectId}/lead-time` | Lead time |
| GET | `/dora/project/{projectId}/mttr` | Temps moyen de rétablissement |
| GET | `/dora/project/{projectId}/change-failure-rate` | Taux d'échec |

---

## Audit Trail (Enterprise)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/audit/events` | Rechercher les événements d'audit |
| GET | `/audit/events/summary` | Résumé par période |
| POST | `/audit/alert-rules` | Créer une règle d'alerte |
| GET | `/audit/alert-rules` | Lister les règles |
| DELETE | `/audit/alert-rules/{id}` | Supprimer une règle |

---

## Client Portal (Enterprise)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/client-portal/project/{projectId}/configure` | Configurer le portail |
| GET | `/client-portal/project/{projectId}` | Voir la config |
| POST | `/client-portal/project/{projectId}/users` | Ajouter un client |
| GET | `/client-portal/project/{projectId}/view` | Vue client |
| POST | `/client-portal/project/{projectId}/feedback` | Soumettre feedback |

---

## Portfolio (Enterprise)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/portfolios` | Créer un portfolio |
| GET | `/portfolios` | Lister |
| GET | `/portfolios/{id}/dashboard` | Dashboard portfolio |
| GET | `/portfolios/{id}/risk-heatmap` | Heatmap des risques |
| POST | `/portfolios/{id}/projects/{projectId}` | Ajouter un projet |

---

## Capacity Planning (Enterprise)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/capacity/entries` | Enregistrer une entrée |
| GET | `/capacity/team/{projectId}` | Capacité de l'équipe |
| GET | `/capacity/forecast/{projectId}` | Prévisions |

---

## Incidents (Enterprise)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/incidents/project/{projectId}` | Déclarer un incident |
| GET | `/incidents/project/{projectId}` | Lister |
| GET | `/incidents/{id}` | Détail |
| PUT | `/incidents/{id}` | Mettre à jour |
| POST | `/incidents/{id}/events` | Ajouter un événement |
| GET | `/incidents/{id}/timeline` | Timeline |
| PATCH | `/incidents/{id}/resolve` | Résoudre |

---

## Webhooks (Enterprise)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/webhooks` | Créer un abonnement |
| GET | `/webhooks/project/{projectId}` | Lister |
| DELETE | `/webhooks/{id}` | Supprimer |
| POST | `/webhooks/{id}/test` | Tester |

---

## API Keys (Enterprise)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api-keys` | Créer une clé |
| GET | `/api-keys/my` | Mes clés |
| DELETE | `/api-keys/{id}` | Révoquer |

---

## Codes de retour HTTP

| Code | Signification |
|------|--------------|
| 200 | Succès |
| 201 | Créé avec succès |
| 204 | Supprimé (pas de contenu) |
| 400 | Requête invalide (validation) |
| 401 | Non authentifié (token manquant/invalide) |
| 403 | Non autorisé (droits insuffisants) |
| 404 | Ressource non trouvée |
| 409 | Conflit (doublon) |
| 500 | Erreur serveur |
