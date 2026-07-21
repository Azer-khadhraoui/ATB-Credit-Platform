<div align="center">

<img src="frontend/public/images/logoatbicon.webp" alt="Arab Tunisian Bank" width="120" />

# ATB Credit Platform

**Plateforme web sécurisée d'aide à la décision pour la gestion des dossiers de crédit, intégrant un moteur de Machine Learning**

Projet de stage — Arab Tunisian Bank × ESPRIT

[![CI](https://github.com/Azer-khadhraoui/ATB-Credit-Platform/actions/workflows/ci.yml/badge.svg)](../../actions/workflows/ci.yml)
[![Security](https://github.com/Azer-khadhraoui/ATB-Credit-Platform/actions/workflows/security.yml/badge.svg)](../../actions/workflows/security.yml)

[![Angular](https://img.shields.io/badge/Angular-19-DD0031?logo=angular&logoColor=white)](frontend)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-6DB33F?logo=springboot&logoColor=white)](backend)
[![FastAPI](https://img.shields.io/badge/FastAPI-ML%20Service-009688?logo=fastapi&logoColor=white)](ml-service)
[![MongoDB](https://img.shields.io/badge/MongoDB-47A248?logo=mongodb&logoColor=white)](#)
[![Python](https://img.shields.io/badge/scikit--learn-Model-F7931E?logo=scikitlearn&logoColor=white)](ml-service)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)](docker-compose.yml)
[![Trivy](https://img.shields.io/badge/Trivy-Security%20Scan-1904DA?logo=aqua&logoColor=white)](.github/workflows/security.yml)
[![SonarCloud](https://sonarcloud.io/api/project_badges/measure?project=Azer-khadhraoui_ATB-Credit-Platform&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=Azer-khadhraoui_ATB-Credit-Platform)

</div>

---

## Sommaire

- [Aperçu](#aperçu)
- [Architecture](#architecture)
- [Stack technique](#stack-technique)
- [Modules fonctionnels](#modules-fonctionnels)
- [Démarrage](#démarrage)
- [Structure du dépôt](#structure-du-dépôt)
- [Sécurité applicative](#sécurité-applicative)
- [CI/CD & DevSecOps](#cicd--devsecops)
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
| Conteneurisation | Docker, Docker Compose, Nginx | Empaquetage et orchestration des quatre services |
| DevSecOps | GitHub Actions, Trivy, CodeQL, Gitleaks | Build automatisé et analyse de vulnérabilités |

## Modules fonctionnels

- **Authentification & sécurité** — connexion JWT, hashage BCrypt, rôles (`ADMIN`, `AGENT_CREDIT`), routes protégées par rôle.
- **Gestion des utilisateurs** — CRUD complet, photo de profil, gestion du mot de passe, page « Mon profil » en libre-service.
- **Gestion des clients** — CRUD complet, informations personnelles, coordonnées, situation professionnelle.
- **Gestion des dossiers de crédit** — création, modification, consultation, recherche, changement de statut.
- **Module Machine Learning** — analyse à la demande d'un dossier : score de risque, niveau de risque (`LOW`/`MEDIUM`/`HIGH`) et décision indicative (`ACCEPTABLE`/`RISKY`/`REJECTED`), calculés par un modèle de régression logistique entraîné sur le dataset public *Loan Prediction*.
- **Audit log** — journalisation des connexions, créations, modifications, suppressions et analyses IA ; consultation réservée aux administrateurs.
- **Tableau de bord** — statistiques en temps réel : volumétrie, répartition par statut, répartition des risques, répartition par type de crédit, activité récente.

## Démarrage

### Avec Docker (recommandé)

Une seule commande lance l'ensemble de la plateforme — aucun outil à installer hormis Docker.

```bash
cp .env.example .env      # ajuster le secret JWT si besoin
docker compose up --build -d
```

| Service | URL |
|---|---|
| Application web | http://localhost:4200 |
| API backend | http://localhost:8081 |
| Service ML | http://localhost:8000/docs |

Commandes utiles :

```bash
docker compose ps        # état des conteneurs
docker compose logs -f    # logs en continu
docker compose down       # tout arrêter
```

Le démarrage est ordonné par des *healthchecks* : le backend n'attend pas seulement que MongoDB et le service ML soient lancés, mais qu'ils répondent réellement. Les données MongoDB et les photos de profil sont conservées dans des volumes nommés.

### Sans Docker (développement)

Utile pour déboguer un service isolément. MongoDB doit tourner localement sur le port `27017`.

```bash
# Backend — http://localhost:8081
cd backend && ./mvnw spring-boot:run

# Service ML — http://localhost:8000
cd ml-service
pip install -r requirements-docker.txt
uvicorn app.main:app --port 8000

# Frontend — http://localhost:4200
cd frontend && npm install && npm start
```

Le modèle entraîné (`ml-service/models/model.pkl`) est chargé au démarrage du service ML — voir `notebooks/01_train_model.ipynb` pour reproduire l'entraînement.

## Structure du dépôt

```
ATB-Credit-Platform/
├── docker-compose.yml   Orchestration des quatre services
├── .env.example         Modèle de configuration (secrets hors dépôt)
├── .github/workflows/   Pipelines CI et DevSecOps
├── frontend/            Application Angular + Dockerfile + config Nginx
├── backend/             API Spring Boot + Dockerfile
├── ml-service/          Service FastAPI + Dockerfile
│   ├── app/              Code de l'API (endpoints, chargement du modèle)
│   ├── data/raw/         Dataset d'entraînement (Loan Prediction)
│   ├── models/           Modèle entraîné et artefacts (.pkl)
│   └── notebooks/        Notebook de préparation des données et d'entraînement
└── docs/                Documentation et maquettes de conception
```

## Sécurité applicative

- Mots de passe hashés avec **BCrypt**, jamais stockés en clair.
- Authentification **stateless** via **JWT**, vérifié à chaque requête protégée.
- Autorisations par rôle appliquées **côté backend** (pas seulement dans l'interface) : par exemple, `/api/audit-logs` renvoie `403` pour un compte non-administrateur, indépendamment de ce que montre l'interface.
- Chaque action sensible (connexion, création, modification, suppression, analyse IA) est tracée dans le journal d'audit avec l'identité de l'auteur.
- Secrets (secret JWT, base de données) injectés par variables d'environnement, jamais versionnés — seul `.env.example` figure dans le dépôt.

## CI/CD & DevSecOps

Deux workflows GitHub Actions s'exécutent à chaque `push` et `pull request` sur `main`.

**`ci.yml` — build, tests & qualité**
Compile et teste chaque service (Spring Boot avec un conteneur MongoDB, Angular en configuration production, FastAPI avec vérification du chargement du modèle), lance l'analyse de qualité **SonarCloud**, puis construit les trois images Docker.

**`security.yml` — analyse de sécurité**

| Outil | Type | Portée |
|---|---|---|
| **Gitleaks** | Secrets | Détection de secrets sur l'intégralité de l'historique Git |
| **CodeQL** | SAST | Analyse statique du code — Java, TypeScript, Python |
| **Trivy** | SCA | Vulnérabilités des dépendances du projet |
| **Trivy** | Image | Vulnérabilités des trois images Docker |
| **OWASP ZAP** | DAST | Scan dynamique de l'application **en cours d'exécution** |
| **SonarCloud** | Qualité | Duplication, complexité, maintenabilité |

L'ensemble couvre les analyses **statiques** (code et images au repos) et **dynamique** (application réellement démarrée via Docker Compose, pour détecter ce que l'analyse statique ne voit pas : en-têtes mal configurés, endpoints exposés, XSS).

**Dependabot** surveille par ailleurs les mises à jour des dépendances Maven, npm, pip, des images de base Docker et des actions GitHub elles-mêmes. Les mises à jour sont regroupées par écosystème pour éviter une avalanche de pull requests.

Les résultats sont publiés au format SARIF dans l'onglet *Security* du dépôt. Une analyse hebdomadaire est également planifiée, de nouvelles vulnérabilités pouvant être publiées contre du code inchangé.

Les scans **rapportent sans bloquer** le pipeline : une CVE transitive sans correctif disponible ne doit pas empêcher la livraison.

**Durcissement des conteneurs**
- Processus exécutés sous un **utilisateur non-root** dans chaque image
- **Images de base minimales** (Alpine, slim) pour réduire la surface d'attaque
- **Builds multi-étapes** : les outils de compilation (Maven, Node) n'apparaissent pas dans les images finales

## Roadmap

- [x] Authentification JWT & gestion des rôles
- [x] Gestion des utilisateurs, clients, dossiers de crédit
- [x] Module Machine Learning (scoring de risque)
- [x] Journal d'audit
- [x] Tableau de bord statistique
- [x] Conteneurisation Docker & orchestration Compose
- [x] Analyse de vulnérabilités — SAST, SCA, images, secrets (CodeQL, Trivy, Gitleaks)
- [x] Analyse dynamique (OWASP ZAP) & qualité de code (SonarCloud)
- [x] Veille automatisée des dépendances (Dependabot)
- [x] Pipeline CI/CD (GitHub Actions)
- [ ] Déploiement Kubernetes

---

<div align="center">
<sub>Projet réalisé dans le cadre d'un stage à l'Arab Tunisian Bank — ESPRIT, Cloud Computing & Cybersécurité</sub>
</div>
