# Déploiement Kubernetes

Manifestes pour faire tourner la plateforme sur un cluster mono-nœud (Kubernetes intégré à
Docker Desktop). C'est la traduction du `docker-compose.yml` en objets Kubernetes.

## Topologie

| Service | Objet | Exposition | DNS interne |
|---|---|---|---|
| MongoDB | StatefulSet + PVC | interne | `mongo:27017` |
| ml-service | Deployment | interne | `ml-service:8000` |
| backend | Deployment + PVC | LoadBalancer `:8081` | `backend:8081` |
| frontend | Deployment | LoadBalancer `:4200` | `frontend:4200` |

Le backend est publié en externe (pas seulement le frontend) parce que le navigateur appelle
`http://localhost:8081/api` en direct (voir `frontend/src/environments/environment.ts`).

## Prérequis

1. **Docker Desktop → Settings → Kubernetes → Enable Kubernetes** (cluster mono-nœud).
2. Les images sont construites localement — le cluster de Docker Desktop partage le magasin
   d'images du démon, donc `imagePullPolicy: IfNotPresent` les trouve sans registre :
   ```bash
   docker compose build
   ```
   Cela produit `atb-credit-platform-backend|frontend|ml-service:latest`.

## Déploiement

```bash
# 1. Namespace, config
kubectl apply -f k8s/00-namespace.yaml
kubectl apply -f k8s/01-config.yaml

# 2. Secret JWT — jamais commité, créé à la volée
kubectl create secret generic atb-jwt -n atb \
  --from-literal=JWT_SECRET="$(openssl rand -base64 48)"

# 3. Le reste de la stack
kubectl apply -f k8s/10-mongo.yaml
kubectl apply -f k8s/20-ml-service.yaml
kubectl apply -f k8s/30-backend.yaml
kubectl apply -f k8s/40-frontend.yaml
```

Ou tout d'un coup (après avoir créé le secret) : `kubectl apply -f k8s/`.

## Vérification

```bash
kubectl get pods -n atb -w        # attendre que tout soit Running/Ready
kubectl get svc -n atb            # les LoadBalancer montrent localhost
```

Puis ouvrir **http://localhost:4200**.

## Diagnostic

```bash
kubectl logs -n atb deploy/backend
kubectl describe pod -n atb -l app.kubernetes.io/name=backend
kubectl get events -n atb --sort-by=.lastTimestamp
```

## Suppression

```bash
kubectl delete namespace atb   # supprime tout, y compris les volumes
```
