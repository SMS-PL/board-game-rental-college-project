# Wypożyczalnia Gier Planszowych

Aplikacja webowa do zarządzania wypożyczalnią gier planszowych z autentykacją JWT. Obsługuje CRUD gier, klientów i wypożyczeń.

---

## Stack technologiczny

| Warstwa        | Technologia                                     |
|----------------|-------------------------------------------------|
| Backend        | Kotlin + Spring Boot 4 + CQRS + Vertical Slices |
| Frontend       | Angular 19 + Tailwind CSS + DaisyUI             |
| Baza danych    | PostgreSQL 16                                   |
| Autentykacja   | JWT (HS256, 24h)                                |
| Konteneryzacja | Docker + Kubernetes (minikube)                  |

---

## Uruchomienie lokalne (tryb developerski)

### Wymagania

- JDK 21
- Node.js 20+
- PostgreSQL 16 (lokalnie lub przez Docker)

### Baza danych

```bash
# Uruchom PostgreSQL przez Docker (jeśli nie masz lokalnie)
docker run -d \
  --name rental-postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=rental-game-db \
  -p 5432:5432 \
  postgres:16-alpine
```

### Backend

```bash
cd gamerental-api
./gradlew bootRun
# Dostępny na http://localhost:8080
```

### Frontend

```bash
cd gamerental-app
npm install
npm start
# Dostępny na http://localhost:4200
```

Proxy w `proxy.conf.json` przekierowuje `/api/*` → `http://localhost:8080`.

---

## Uruchomienie w minikube (Kubernetes)

### Wymagania

- [minikube](https://minikube.sigs.k8s.io/docs/start/)
- [kubectl](https://kubernetes.io/docs/tasks/tools/)
- Docker

### Uruchomienie (pierwsze i każde kolejne)

```bash
./deploy.sh
```

Skrypt automatycznie:
1. Uruchamia minikube (jeśli jeszcze nie działa)
2. Przekierowuje Dockera na rejestr wewnątrz minikube
3. Buduje JAR backendu (`./gradlew bootJar`)
4. Buduje obrazy Docker (`rental-backend:latest`, `rental-frontend:latest`)
5. Aplikuje manifesty Kubernetes (`k8s/`)
6. Restartuje Pody (wymusza załadowanie nowych obrazów)
7. Czeka aż wszystkie Pody będą `Ready`, a następnie otwiera przeglądarkę

> **Po zmianach w kodzie** wystarczy ponownie uruchomić `./deploy.sh` — skrypt wykryje działające minikube, odbuduje tylko to co potrzebne i wdroży nową wersję.

### Przydatne komendy diagnostyczne

```bash
# Status wszystkich zasobów
kubectl get all -n rental-games

# Logi backendu
kubectl logs -l app=rental-backend -n rental-games

# Logi bazy danych
kubectl logs -l app=postgres -n rental-games

# Wejdź do kontenera backendu
kubectl exec -it deployment/rental-backend -n rental-games -- /bin/sh


# Usuń wszystko i zacznij od nowa
kubectl delete namespace rental-games
```

---

## Struktura projektu

```
board-game-rental-college-project/
├── gamerental-api/       # Backend – Kotlin + Spring Boot
│   ├── Dockerfile
│   └── src/main/kotlin/sms/pollub/gamerental/
│       ├── auth/         # Rejestracja, logowanie, JWT
│       ├── game/         # Gry i egzemplarze (CQRS slices)
│       ├── client/       # Klienci
│       ├── rental/       # Wypożyczenia
│       └── shared/       # BaseEntity, ErrorHandler
├── gamerental-app/       # Frontend – Angular + DaisyUI
│   ├── Dockerfile
│   ├── nginx.conf
│   └── src/app/
│       ├── core/         # Modele, serwisy, guards, interceptory
│       └── features/     # auth, games, customers, rentals, dashboard
├── k8s/                  # Manifesty Kubernetes
│   ├── namespace.yaml
│   ├── postgres/         # Secret, PVC, Deployment, Service
│   ├── backend/          # ConfigMap, Deployment, Service
│   └── frontend/         # Deployment, Service (NodePort)
├── TECHNICAL_SPEC.md
└── KUBERNETES_GUIDE.md   # Szczegółowe wyjaśnienie pojęć K8s
```

---

## Użycie AI przy realizacji projektu
1. rozmowa z Claude w celu utworzenia Design Documents https://claude.ai/share/b33a1c5b-f3fb-461d-9b89-48964d073059
2. Prompty do agenta Copilot:
- backend: 
"Zapoznaj sie z technical spec.
Zaimplementuj backend
Jest jedna różnica względem pliku - użyj liqubase a nie flyway.
Utworzyłem baze postgresa: rental-game-db
login XXXX haslo XXXX."
- frontend:
  "/Users/michalt/github_projects/board-game-rental-college-project/TECHNICAL_SPEC.md
zapoznaj sie z tym plikiem. Backend jest w trakcie implementacji.
ty teraz zaimplementuj frontend"
3. Ręczne zmiany w projekcie, bez użycia AI lub korzystając tylko z autocomplete

PLIKI WYGENEROWANE PRZEZ AI:
[KUBERNETES_GUIDE.md](https://github.com/user-attachments/files/27793028/KUBERNETES_GUIDE.md)

[TECHNICAL_SPEC.md](https://github.com/user-attachments/files/27793041/TECHNICAL_SPEC.md)

