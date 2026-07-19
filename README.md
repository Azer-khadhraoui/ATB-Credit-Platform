<div align="center">

<img src="frontend/public/images/logoatbbackgrouge.jpg" alt="Arab Tunisian Bank" width="140" />

# ATB Credit Platform

**Plateforme web sécurisée d'aide à la décision pour la gestion des dossiers de crédit, intégrant un moteur de Machine Learning**

Projet de stage — Arab Tunisian Bank × ESPRIT

[![Angular](https://img.shields.io/badge/Angular-19-DD0031?logo=angular&logoColor=white)](frontend)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-6DB33F?logo=springboot&logoColor=white)](backend)
[![FastAPI](https://img.shields.io/badge/FastAPI-ML%20Service-009688?logo=fastapi&logoColor=white)](ml-service)
[![MongoDB](https://img.shields.io/badge/MongoDB-47A248?logo=mongodb&logoColor=white)](#)
[![Python](https://img.shields.io/badge/scikit--learn-Model-F7931E?logo=scikitlearn&logoColor=white)](ml-service)

</div>

---

## Sommaire

- [Aperçu](#aperçu)
- [Architecture](#architecture)
- [Stack technique](#stack-technique)
- [Modules fonctionnels](#modules-fonctionnels)
- [Démarrage](#démarrage)
- [Structure du dépôt](#structure-du-dépôt)
- [Sécurité](#sécurité)
- [Roadmap](#roadmap)

## Aperçu

ATB Credit Platform est une application interne destinée aux **agents bancaires** et **administrateurs** pour gérer les clients et les dossiers de crédit d'une banque, avec une analyse de risque assistée par un **modèle de Machine Learning** entraîné sur un jeu de données public (Loan Prediction). L'objectif n'est pas de remplacer la décision humaine, mais de fournir un **score de risque indicatif** pour assister l'agent dans sa décision finale.

## Architecture

Le projet suit une architecture **microservices**, avec une séparation claire entre l'interface, la logique métier et le moteur d'analyse :

```
┌─────────────┐      ┌──────────────┐      ┌──────────────┐
│  Frontend   │ ───▶ │   Backend    │ ───▶ │  ML Service  │
│  Angular 19 │      │ Spring Boot 4│      │   FastAPI    │
└─────────────┘      └──────┬───────┘      └──────┬───────┘
                             │                      │
                             ▼                      ▼
                       ┌──────────┐          ┌─────────────┐
                       │ MongoDB  │          │ scikit-learn│
                       └──────────┘          │   modèle    │
                                              └─────────────┘
```

- **Frontend** : consomme l'API REST du backend, aucune logique métier côté client.
- **Backend** : porte l'authentification, les règles métier, la persistance, et orchestre l'appel au service ML.
- **ML Service** : service Python indépendant, ne connaît rien de MongoDB ni de l'authentification — il expose uniquement un modèle de scoring entraîné.

## Stack technique

| Composant | Technologie | Rôle |
|---|---|---|
| Frontend | Angular 19, TypeScript, SCSS | Interface web (signals, standalone components) |
| Backend | Spring Boot 4.1, Java 21, Spring Security | API REST, logique métier, authentification JWT |
| Base de données | MongoDB | Utilisateurs, clients, dossiers, résultats IA, audit logs |
| Service ML | Python, FastAPI, scikit-learn, pandas | Entraînement et exposition du modèle de scoring |
| Auth | JWT + BCrypt | Authentification stateless, rôles Admin / Agent de crédit |

## Modules fonctionnels

- **Authentification & sécurité** — connexion JWT, hashage BCrypt, rôles (`ADMIN`, `AGENT_CREDIT`), routes protégées par rôle.
- **Gestion des utilisateurs** — CRUD complet, photo de profil, gestion du mot de passe, page « Mon profil » en libre-service.
- **Gestion des clients** — CRUD complet, informations personnelles, coordonnées, situation professionnelle.
- **Gestion des dossiers de crédit** — création, modification, consultation, recherche, changement de statut.
- **Module Machine Learning** — analyse à la demande d'un dossier : score de risque, niveau de risque (`LOW`/`MEDIUM`/`HIGH`) et décision indicative (`ACCEPTABLE`/`RISKY`/`REJECTED`), calculés par un modèle de régression logistique entraîné sur le dataset public *Loan Prediction*.
- **Audit log** — journalisation des connexions, créations, modifications, suppressions et analyses IA ; consultation réservée aux administrateurs.
- **Tableau de bord** — statistiques en temps réel : volumétrie, répartition par statut, répartition des risques, répartition par type de crédit, activité récente.

## Démarrage

Chaque service se lance indépendamment. Trois terminaux sont nécessaires en développement.

### 1. Base de données

MongoDB doit tourner en local sur le port par défaut (`27017`).

### 2. Backend (Spring Boot)

```bash
cd backend
./mvnw spring-boot:run
```

L'API est exposée sur `http://localhost:8081`. Configuration dans `backend/src/main/resources/application.properties` (URI MongoDB, secret JWT, URL du service ML).

### 3. Service ML (FastAPI)

```bash
cd ml-service
pip install fastapi "uvicorn[standard]" joblib pydantic pandas scikit-learn
uvicorn app.main:app --port 8000
```

Expose `POST /predict` et `GET /health` sur `http://localhost:8000`. Le modèle entraîné (`models/model.pkl`) est chargé au démarrage — voir `notebooks/01_train_model.ipynb` pour reproduire l'entraînement.

### 4. Frontend (Angular)

```bash
cd frontend
npm install
npm start
```

Application disponible sur `http://localhost:4200`.

## Structure du dépôt

```
ATB-Credit-Platform/
├── frontend/          Application Angular (interface utilisateur)
├── backend/            API Spring Boot (logique métier, sécurité, persistance)
├── ml-service/         Service FastAPI (scoring de risque)
│   ├── app/             Code de l'API (endpoints, chargement du modèle)
│   ├── data/raw/         Dataset d'entraînement (Loan Prediction)
│   ├── models/           Modèle entraîné et artefacts (.pkl)
│   └── notebooks/        Notebook de préparation des données et d'entraînement
└── docs/               Documentation et maquettes de conception
```

## Sécurité

- Mots de passe hashés avec **BCrypt**, jamais stockés en clair.
- Authentification **stateless** via **JWT**, vérifié à chaque requête protégée.
- Autorisations par rôle appliquées **côté backend** (pas seulement dans l'interface) : par exemple, `/api/audit-logs` renvoie `403` pour un compte non-administrateur, indépendamment de ce que montre l'interface.
- Chaque action sensible (connexion, création, modification, suppression, analyse IA) est tracée dans le journal d'audit avec l'identité de l'auteur.

## Roadmap

- [x] Authentification JWT & gestion des rôles
- [x] Gestion des utilisateurs, clients, dossiers de crédit
- [x] Module Machine Learning (scoring de risque)
- [x] Journal d'audit
- [x] Tableau de bord statistique
- [ ] Conteneurisation Docker des trois services

---

<div align="center">
<sub>Projet réalisé dans le cadre d'un stage à l'Arab Tunisian Bank — ESPRIT, Cloud Computing & Cybersécurité</sub>
</div>
