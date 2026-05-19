# Gestion des Utilisateurs

## Inscription

### User Story
> En tant que nouvel utilisateur, je veux créer un compte afin d'accéder à la plateforme.

### Flux
1. L'utilisateur accède à la page d'inscription
2. Il remplit : prénom, nom, email, mot de passe
3. Le système vérifie que l'email n'est pas déjà utilisé
4. Le compte est créé et l'utilisateur est automatiquement connecté
5. Il est redirigé vers le dashboard

### Règles de validation
- Email : format valide, unique dans le système
- Mot de passe : minimum 8 caractères
- Prénom/Nom : obligatoires, 2-100 caractères

---

## Connexion

### User Story
> En tant qu'utilisateur existant, je veux me connecter avec mon email et mot de passe.

### Flux
1. L'utilisateur saisit email + mot de passe
2. Le système vérifie les identifiants
3. En cas de succès : token JWT retourné, redirection vers le dashboard
4. En cas d'échec : message d'erreur générique ("Identifiants invalides")

### Sécurité
- Le message d'erreur ne distingue pas "email inconnu" de "mauvais mot de passe" (prévention de l'énumération)
- Après 5 tentatives échouées : délai progressif (rate limiting)

---

## Profil utilisateur

### User Story
> En tant qu'utilisateur connecté, je veux consulter et modifier mon profil.

### Informations modifiables
- Prénom et nom
- Avatar (URL)
- Mot de passe (nécessite l'ancien mot de passe)

### Informations en lecture seule
- Email (identifiant, non modifiable)
- Date de création du compte
- Organisations et projets associés

---

## Rôles et permissions

### Hiérarchie des rôles

```
ADMIN
  └── MANAGER
       └── DEVELOPER
            └── VIEWER
```

Chaque rôle hérite des permissions des rôles inférieurs.

### Détail des permissions

| Fonctionnalité | ADMIN | MANAGER | DEVELOPER | VIEWER |
|----------------|-------|---------|-----------|--------|
| **Organisation** | | | | |
| Créer/supprimer une organisation | x | | | |
| Modifier les paramètres org | x | | | |
| Inviter des membres | x | x | | |
| Changer les rôles | x | | | |
| **Projets** | | | | |
| Créer un projet | x | x | | |
| Modifier un projet | x | x | | |
| Supprimer un projet | x | | | |
| Voir un projet | x | x | x | x |
| **Tickets** | | | | |
| Créer un ticket | x | x | x | |
| Modifier un ticket | x | x | x | |
| Supprimer un ticket | x | x | | |
| Voir les tickets | x | x | x | x |
| Commenter | x | x | x | |
| Changer le statut | x | x | x | |
| **Sprints** | | | | |
| Créer/gérer les sprints | x | x | | |
| Démarrer/terminer un sprint | x | x | | |
| **Releases** | | | | |
| Créer une release | x | x | | |
| Publier une release | x | x | | |
| **Administration** | | | | |
| Voir l'audit trail | x | | | |
| Gérer les webhooks | x | x | | |
| Gérer les API keys | x | x | | |
| Configurer les workflows | x | x | | |

---

## Invitations

### User Story
> En tant que manager, je veux inviter un collègue par email à rejoindre mon organisation.

### Flux
1. Le manager saisit l'email de la personne à inviter
2. Il choisit le rôle (DEVELOPER par défaut)
3. Le système crée une invitation en attente
4. L'invité reçoit un email avec un lien
5. L'invité clique sur le lien :
   - S'il a un compte → rejoint directement l'organisation
   - S'il n'a pas de compte → redirigé vers l'inscription, puis rejoint

### États d'une invitation

```
PENDING → ACCEPTED
PENDING → DECLINED
PENDING → EXPIRED (après 7 jours)
```

### Règles
- Une invitation expire après 7 jours
- On ne peut pas inviter un email déjà membre
- Le rôle est défini à l'invitation et peut être modifié ensuite

---

## Session et déconnexion

### Gestion de session
- Token d'accès : valide 24h
- Token de rafraîchissement : valide 7 jours
- L'application renouvelle automatiquement le token d'accès via le refresh token

### Déconnexion
- Suppression du token côté client (localStorage)
- Redirection vers la page de connexion
- Le token reste techniquement valide jusqu'à son expiration (stateless)

---

## Cas limites

| Situation | Comportement |
|-----------|-------------|
| Email déjà utilisé à l'inscription | Erreur "Cet email est déjà associé à un compte" |
| Token expiré | Tentative de refresh ; si échec → redirection login |
| Compte désactivé | Connexion refusée avec message |
| Membre retiré d'une organisation | Perd l'accès aux projets de cette org |
| Suppression de compte | Soft delete (is_active = false), données conservées |
