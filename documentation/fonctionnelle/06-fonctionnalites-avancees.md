# Fonctionnalités Avancées (v2.0)

## Moteur de Workflows

### User Story
> En tant que manager, je veux configurer les règles de transition des tickets pour refléter notre processus de développement.

### Fonctionnalités
- Définir les transitions autorisées entre statuts
- Ajouter des conditions (ex: un ticket ne peut passer en DONE que si les heures sont loggées)
- Configurer des actions automatiques (ex: notifier le reviewer quand un ticket passe en IN_REVIEW)

### Exemple de workflow personnalisé

```
BACKLOG ──► TODO ──► IN_PROGRESS ──► IN_REVIEW ──► DONE
                         │                            │
                         └──── BLOCKED ───────────────┘
                                                      │
                                                 CANCELLED
```

### Transitions configurables

| Depuis | Vers | Condition | Action automatique |
|--------|------|-----------|-------------------|
| BACKLOG | TODO | Priorité définie | - |
| TODO | IN_PROGRESS | Assigné requis | Notifier l'assigné |
| IN_PROGRESS | IN_REVIEW | - | Notifier le reviewer |
| IN_REVIEW | DONE | Review approuvée | Notifier le reporter |
| IN_REVIEW | IN_PROGRESS | Review refusée | Notifier l'assigné |
| * | CANCELLED | Rôle MANAGER+ | Notifier le reporter |

---

## Labels

### User Story
> En tant que membre de l'équipe, je veux étiqueter les tickets avec des labels colorés pour les catégoriser visuellement.

### Fonctionnalités
- Créer des labels avec nom + couleur
- Associer plusieurs labels à un ticket
- Filtrer les tickets par label
- Labels propres à chaque projet

### Exemples de labels

| Label | Couleur | Usage |
|-------|---------|-------|
| `frontend` | #58a6ff | Travail côté client |
| `backend` | #3fb950 | Travail côté serveur |
| `urgent` | #f85149 | À traiter immédiatement |
| `tech-debt` | #d29922 | Dette technique |
| `documentation` | #8b949e | Documentation à écrire |
| `design` | #bc8cff | Travail UI/UX |

---

## Pièces jointes

### User Story
> En tant que reporter de bug, je veux attacher des captures d'écran au ticket pour illustrer le problème.

### Fonctionnalités
- Upload de fichiers (images, PDF, documents)
- Aperçu des images directement dans le ticket
- Téléchargement des pièces jointes
- Suppression (par l'auteur ou un ADMIN)

### Contraintes
| Paramètre | Valeur |
|-----------|--------|
| Taille max par fichier | 10 Mo |
| Types autorisés | Images, PDF, texte, archives |
| Nombre max par ticket | 20 |

---

## Suivi du temps (Time Tracking)

### User Story
> En tant que développeur, je veux logger le temps passé sur un ticket pour le reporting.

### Flux
1. Ouvrir un ticket
2. Cliquer "Logger du temps"
3. Saisir le nombre d'heures
4. Optionnel : ajouter un commentaire
5. Le temps s'ajoute au cumul du ticket

### Vues
| Vue | Description |
|-----|-------------|
| Par ticket | Heures estimées vs heures passées |
| Par utilisateur | Répartition du temps sur la période |
| Par projet | Total des heures par sprint/semaine |
| Rapport | Export détaillé des entrées de temps |

### Indicateurs

```
Ticket MOB-42: "Implémenter le dark mode"
┌─────────────────────────────────────────────┐
│ Estimé: 8h                                   │
│ Passé:  10.5h  [████████████░░] 131%        │
│ Écart:  +2.5h (dépassement)                 │
└─────────────────────────────────────────────┘
```

---

## Invitations par email

### User Story
> En tant que manager, je veux inviter un nouveau collaborateur à rejoindre mon organisation et mes projets.

### Flux
1. Le manager va dans "Équipe" → "Inviter"
2. Saisit l'email du collaborateur
3. Choisit le rôle (DEVELOPER par défaut)
4. Sélectionne les projets à partager
5. L'invitation est envoyée

### Statuts d'invitation
| Statut | Description |
|--------|-------------|
| PENDING | En attente d'acceptation |
| ACCEPTED | Invité a rejoint |
| DECLINED | Invité a refusé |
| EXPIRED | Délai dépassé (7 jours) |

---

## Filtres sauvegardés

### User Story
> En tant qu'utilisateur fréquent, je veux sauvegarder mes critères de recherche pour y accéder en un clic.

### Fonctionnalités
- Combiner plusieurs critères (statut + priorité + assigné + labels + ...)
- Nommer et sauvegarder la combinaison
- Partager un filtre avec l'équipe (optionnel)
- Accès rapide depuis la sidebar ou le backlog

### Exemples de filtres

| Nom du filtre | Critères |
|---------------|----------|
| "Mes bugs critiques" | assignee = moi, type = BUG, priority = CRITICAL |
| "Sprint actif non estimé" | sprint = actif, storyPoints = null |
| "Bloqué depuis 3 jours" | status = IN_PROGRESS, updatedAt < 3 jours |
| "À reviewer" | status = IN_REVIEW, assignee = moi |

---

## Liens entre tickets

### User Story
> En tant que développeur, je veux lier des tickets entre eux pour montrer les dépendances.

### Types de liens

| Type | Signification | Utilisation |
|------|---------------|-------------|
| **BLOCKS** | A empêche B d'avancer | Dépendance technique |
| **RELATES_TO** | A et B sont liés thématiquement | Documentation |
| **DUPLICATES** | A est identique à B | Dédoublonnage |

### Visualisation
Les liens apparaissent dans le détail du ticket :
```
MOB-42: Implémenter le dark mode
├── Bloqué par: MOB-35 (Design system colors)
├── Lié à: MOB-40 (Thème clair/sombre switch)
└── Dupliqué par: MOB-48 (demande identique)
```
