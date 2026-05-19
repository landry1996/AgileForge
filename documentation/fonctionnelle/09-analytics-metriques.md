# Analytics et Métriques

## Dashboard Analytics

### User Story
> En tant que manager, je veux accéder à des tableaux de bord avec des métriques en temps réel pour prendre des décisions éclairées.

### Widgets disponibles

| Widget | Données | Visualisation |
|--------|---------|---------------|
| Vélocité | Points livrés par sprint | Graphique en barres |
| Burndown | Travail restant vs temps | Courbe |
| Cycle Time | Temps moyen par ticket | Graphique en ligne |
| Cumulative Flow | Tickets par statut au fil du temps | Graphique en aires empilées |
| Throughput | Tickets terminés par semaine | Barres |
| Distribution | Répartition par type/priorité | Camembert |

---

## Métriques Agiles

### Vélocité
```
Définition: Story points livrés par sprint
Formule: Σ(story_points) des tickets DONE dans le sprint
Utilité: Planification de la capacité future
```

### Cycle Time
```
Définition: Temps entre IN_PROGRESS et DONE
Formule: date(DONE) - date(IN_PROGRESS)
Utilité: Mesurer la productivité de l'équipe
Objectif: Le réduire sans sacrifier la qualité
```

### Lead Time
```
Définition: Temps entre la création et la livraison
Formule: date(DONE) - date(création)
Utilité: Mesurer la réactivité globale (inclut le temps en backlog)
```

### Throughput
```
Définition: Nombre de tickets terminés par unité de temps
Formule: Count(tickets DONE) par semaine
Utilité: Mesurer la capacité de livraison
```

### Cumulative Flow Diagram (CFD)

```
Tickets
cumulés
    │  ┌─────────────────────── DONE
    │  │     ┌────────────────── TESTING
    │  │     │    ┌───────────── IN_REVIEW
    │  │     │    │   ┌───────── IN_PROGRESS
    │  │     │    │   │  ┌────── TODO
    │  │     │    │   │  │  ┌─── BACKLOG
    │  │     │    │   │  │  │
    └──┴─────┴────┴───┴──┴──┴───── Temps
```

Le CFD montre :
- **Largeur verticale** d'une bande = WIP de ce statut
- **Largeur horizontale** entre deux bandes = Lead Time approximatif
- **Pente** de la bande DONE = Throughput

---

## Métriques DORA

Les 4 métriques clés de la performance DevOps (Google DORA Research) :

### 1. Deployment Frequency (Fréquence de déploiement)
```
Question: À quelle fréquence déployons-nous en production ?
Mesure: Nombre de déploiements par période
```

| Niveau | Fréquence |
|--------|-----------|
| Elite | Plusieurs fois par jour |
| High | Entre 1/jour et 1/semaine |
| Medium | Entre 1/semaine et 1/mois |
| Low | Moins d'une fois par mois |

### 2. Lead Time for Changes (Délai de livraison)
```
Question: Combien de temps entre un commit et sa mise en production ?
Mesure: Durée commit → déploiement
```

| Niveau | Durée |
|--------|-------|
| Elite | < 1 heure |
| High | 1 jour - 1 semaine |
| Medium | 1 semaine - 1 mois |
| Low | > 1 mois |

### 3. MTTR (Mean Time To Recovery / Temps moyen de rétablissement)
```
Question: Combien de temps pour se remettre d'un incident ?
Mesure: Durée entre détection et résolution
```

| Niveau | Durée |
|--------|-------|
| Elite | < 1 heure |
| High | < 1 jour |
| Medium | 1 jour - 1 semaine |
| Low | > 1 semaine |

### 4. Change Failure Rate (Taux d'échec des changements)
```
Question: Quel pourcentage de déploiements cause un incident ?
Mesure: Déploiements avec incident / Total déploiements
```

| Niveau | Taux |
|--------|------|
| Elite | 0-15% |
| High | 16-30% |
| Medium | 31-45% |
| Low | > 45% |

### Dashboard DORA dans AgileForge

```
┌─────────────────────────────────────────────────────────┐
│                   DORA Metrics - Mai 2026                │
├──────────────┬──────────────┬────────────┬──────────────┤
│  Deployment  │  Lead Time   │    MTTR    │ Change Fail  │
│  Frequency   │  for Changes │            │    Rate      │
│              │              │            │              │
│   3x/jour    │   4 heures   │  45 min    │    8%        │
│   ▲ ELITE    │   ▲ ELITE    │  ▲ ELITE   │  ▲ ELITE    │
│   +20% vs    │   -30% vs    │  -50% vs   │  -5% vs     │
│   mois préc. │   mois préc. │  mois préc.│  mois préc.  │
└──────────────┴──────────────┴────────────┴──────────────┘
```

---

## OKR (Objectives and Key Results)

### User Story
> En tant que direction, je veux définir des objectifs mesurables et suivre leur progression.

### Structure

```
Objectif: "Améliorer la satisfaction client"
├── Key Result 1: "NPS > 50" (actuel: 42, cible: 50)
├── Key Result 2: "Temps de réponse < 2h" (actuel: 3h, cible: 2h)
└── Key Result 3: "0 bug critique en prod" (actuel: 1, cible: 0)

Progression globale: 65%
```

### Fonctionnalités
- Créer des objectifs par trimestre
- Définir 2-5 key results par objectif
- Suivre la progression (mise à jour manuelle ou automatique)
- Lier des tickets aux key results
- Vue synthétique de tous les OKR

### Progression automatique
Certains KR peuvent être calculés automatiquement :
- "Livrer 50 tickets" → progression = tickets DONE / 50
- "Vélocité > 40 pts/sprint" → progression basée sur les derniers sprints
- "0 incident critique" → basé sur le module incidents

---

## Rapports

### Types de rapports

| Rapport | Audience | Contenu |
|---------|----------|---------|
| Sprint Report | Équipe | Vélocité, complétion, carry-over |
| Velocity Trend | SM/Manager | Évolution de la vélocité |
| Time Report | Manager/RH | Heures par personne/projet |
| DORA Dashboard | CTO/Direction | 4 métriques DORA |
| OKR Progress | Direction | Avancement des objectifs |
| Release Notes | Client/Externe | Changelog des livraisons |

### Export
- Format PDF pour les rapports formels
- Format CSV pour l'analyse dans un tableur
- API pour l'intégration avec d'autres outils
