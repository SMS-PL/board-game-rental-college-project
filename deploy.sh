#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "==> [1/7] Sprawdzanie minikube..."
if ! minikube status --format='{{.Host}}' 2>/dev/null | grep -q "Running"; then
  echo "    minikube nie działa – uruchamiam..."
  minikube start
else
  echo "    minikube już działa."
fi

echo "==> [2/7] Przekierowuję Dockera na rejestr minikube..."
eval "$(minikube docker-env)"

echo "==> [3/7] Buduję JAR backendu..."
cd "$ROOT_DIR/gamerental-api"
./gradlew bootJar -q
cd "$ROOT_DIR"

echo "==> [4/7] Buduję obrazy Docker..."
docker build -q -t rental-backend:latest "$ROOT_DIR/gamerental-api"
echo "    rental-backend:latest – OK"
docker build -q -t rental-frontend:latest "$ROOT_DIR/gamerental-app"
echo "    rental-frontend:latest – OK"

echo "==> [5/7] Aplikuję manifesty Kubernetes..."
kubectl apply -f "$ROOT_DIR/k8s/namespace.yaml"
kubectl apply -f "$ROOT_DIR/k8s/postgres/"
kubectl apply -f "$ROOT_DIR/k8s/backend/"
kubectl apply -f "$ROOT_DIR/k8s/frontend/"

echo "==> [6/7] Restartuję deploymenty (wymuszam pobranie nowych obrazów)..."
kubectl rollout restart deployment/rental-backend  -n rental-games
kubectl rollout restart deployment/rental-frontend -n rental-games

echo "==> [7/7] Czekam aż Pody będą gotowe..."
kubectl rollout status deployment/postgres         -n rental-games --timeout=120s
kubectl rollout status deployment/rental-backend   -n rental-games --timeout=120s
kubectl rollout status deployment/rental-frontend  -n rental-games --timeout=120s

echo ""
echo "Aplikacja gotowa! Otwieram w przeglądarce..."
minikube service rental-frontend -n rental-games

