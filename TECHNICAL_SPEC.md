# Specyfikacja Techniczna – Wypożyczalnia Gier Planszowych

## 1. Przegląd Projektu

Aplikacja webowa do zarządzania wypożyczalnią gier planszowych. CRUD z autentykacją JWT. Uruchamiana w lokalnym klastrze Kubernetes (minikube).

---

## 2. Stack Technologiczny

| Warstwa       | Technologia                                      |
|---------------|--------------------------------------------------|
| Backend       | Kotlin + Spring Boot 3.x                         |
| Architektura  | CQRS + Vertical Slices (bez portów i adapterów)  |
| Frontend      | Angular (najnowszy) + Tailwind CSS + DaisyUI      |
| Baza danych   | PostgreSQL 16                                    |
| Autentykacja  | JWT (login/hasło, bez wymogów siły hasła)        |
| Konteneryzacja| Docker + Kubernetes (minikube)                   |

---

## 3. Autentykacja

- Rejestracja i logowanie po **loginie** (nie email)
- Brak wymogów co do hasła (np. login: `user`, hasło: `123` musi działać)
- JWT w headerze `Authorization: Bearer <token>`
- Brak ról – wszyscy zalogowani userzy mają te same uprawnienia
- Endpointy `/auth/register` i `/auth/login` są publiczne, reszta wymaga tokenu

---

## 4. Model Domenowy

### 4.1 Game (Gra)

| Pole        | Typ                        | Opis                        |
|-------------|----------------------------|-----------------------------|
| id          | UUID                       | PK                          |
| title       | String                     | Tytuł gry                   |
| description | String                     | Opis gry                    |
| tags        | Set\<GameTag\>             | Enum (patrz niżej)          |
| totalCopies | Int                        | Łączna liczba egzemplarzy   |
| createdAt   | LocalDateTime              |                             |

**GameTag (enum):**
`STRATEGY`, `FAMILY`, `PARTY`, `COOPERATIVE`, `DEXTERITY`, `CARD`, `DICE`, `ECONOMIC`, `ABSTRACT`, `THEMATIC`

### 4.2 GameCopy (Egzemplarz Gry)

| Pole        | Typ            | Opis                                          |
|-------------|----------------|-----------------------------------------------|
| id          | UUID           | PK                                            |
| game        | Game           | FK                                            |
| copyNumber  | Int            | Numer egzemplarza (1, 2, 3…)                  |
| condition   | CopyCondition  | Enum: `NEW`, `GOOD`, `WORN`, `DAMAGED`        |
| isAvailable | Boolean        | Czy dostępny do wypożyczenia                  |

### 4.3 Client (Klient)

| Pole        | Typ    | Opis           |
|-------------|--------|----------------|
| id          | UUID   | PK             |
| firstName   | String |                |
| lastName    | String |                |
| phone       | String | Opcjonalny     |
| email       | String | Opcjonalny     |
| createdAt   | LocalDateTime |        |

### 4.4 Rental (Wypożyczenie)

| Pole           | Typ           | Opis                                              |
|----------------|---------------|---------------------------------------------------|
| id             | UUID          | PK                                                |
| gameCopy       | GameCopy      | FK – konkretny egzemplarz                         |
| client         | Client        | FK                                                |
| rentedFrom     | LocalDate     | Data wydania                                      |
| dueTo          | LocalDate     | Planowana data zwrotu                             |
| returnedAt     | LocalDate?    | Faktyczna data zwrotu (null = aktywne)            |
| conditionOnReturn | CopyCondition? | Stan przy zwrocie (null = jeszcze nie zwrócone) |
| status         | RentalStatus  | Enum: `ACTIVE`, `RETURNED`, `OVERDUE`             |
| notes          | String?       | Opcjonalne uwagi                                  |
| createdAt      | LocalDateTime |                                                   |

> Status `OVERDUE` jest wyliczany dynamicznie: `ACTIVE` + `dueTo < today` → `OVERDUE`. Można też trzymać to jako computed property zamiast persystowanego pola.

---

## 5. API – Endpointy

### Auth
```
POST /api/auth/register   – rejestracja { username, password }
POST /api/auth/login      – logowanie   { username, password } → { token }
```

### Games
```
GET    /api/games              – lista gier (filtrowanie po tagu, sortowanie)
GET    /api/games/{id}         – szczegóły gry + lista egzemplarzy
POST   /api/games              – dodaj grę
PUT    /api/games/{id}         – edytuj grę
DELETE /api/games/{id}         – usuń grę
PATCH  /api/games/{id}/copies  – zarządzanie liczbą egzemplarzy { totalCopies }
```

### Clients
```
GET    /api/clients            – lista klientów (filtrowanie, sortowanie)
GET    /api/clients/{id}       – szczegóły klienta + historia wypożyczeń
POST   /api/clients            – dodaj klienta
PUT    /api/clients/{id}       – edytuj klienta
DELETE /api/clients/{id}       – usuń klienta
```

### Rentals
```
GET    /api/rentals            – lista wypożyczeń (filtrowanie: status, klient, gra, po terminie)
GET    /api/rentals/{id}       – szczegóły wypożyczenia
POST   /api/rentals            – Wydaj grę (utwórz wypożyczenie)
PATCH  /api/rentals/{id}/return – Przyjmij grę (zwrot, podaj stan egzemplarza)
```

### Dostępność
```
GET /api/games/{id}/available-copies – egzemplarze dostępne do wypożyczenia
```

---

## 6. Architektura Backend – CQRS + Vertical Slices

Brak portów, adapterów i hexagonalnej abstrakcji. Prosta struktura slice'ów per feature.

### Struktura pakietów

```
com.rentalgames
├── auth/
│   ├── RegisterCommand.kt
│   ├── RegisterCommandHandler.kt
│   ├── LoginCommand.kt
│   ├── LoginCommandHandler.kt
│   ├── AuthController.kt
│   └── JwtService.kt
├── game/
│   ├── Game.kt                    (entity)
│   ├── GameCopy.kt                (entity)
│   ├── GameTag.kt                 (enum)
│   ├── CopyCondition.kt           (enum)
│   ├── GameRepository.kt
│   ├── GameCopyRepository.kt
│   ├── commands/
│   │   ├── CreateGameCommand.kt
│   │   ├── CreateGameCommandHandler.kt
│   │   ├── UpdateGameCommand.kt
│   │   ├── UpdateGameCommandHandler.kt
│   │   ├── DeleteGameCommandHandler.kt
│   │   └── UpdateCopiesCommandHandler.kt
│   ├── queries/
│   │   ├── GetGamesQuery.kt
│   │   ├── GetGamesQueryHandler.kt
│   │   └── GetGameQueryHandler.kt
│   └── GameController.kt
├── client/
│   └── (analogicznie jak game/)
├── rental/
│   ├── Rental.kt
│   ├── RentalStatus.kt
│   ├── RentalRepository.kt
│   ├── commands/
│   │   ├── IssueGameCommand.kt
│   │   ├── IssueGameCommandHandler.kt
│   │   ├── ReturnGameCommand.kt
│   │   └── ReturnGameCommandHandler.kt
│   ├── queries/
│   │   ├── GetRentalsQuery.kt
│   │   └── GetRentalsQueryHandler.kt
│   └── RentalController.kt
└── shared/
    ├── BaseEntity.kt              (id, createdAt)
    └── ErrorHandler.kt
```

### Zasady

- Handler przyjmuje Command/Query i zwraca DTO
- Controller wywołuje Handler bezpośrednio (bez magistrali komend)
- Każda operacja biznesowa to osobny Handler
- Repository to interfejsy Spring Data JPA
- Brak `@Service` z 20 metodami – każdy handler robi jedną rzecz

---

## 7. Frontend – Angular + DaisyUI

### Setup

```bash
ng new rental-games-frontend --routing --style=scss
npm install -D tailwindcss postcss autoprefixer @tailwindcss/typography
npm install daisyui
npx tailwindcss init
```

**tailwind.config.js:**
```js
module.exports = {
  content: ['./src/**/*.{html,ts}'],
  plugins: [require('daisyui')],
  daisyui: {
    themes: ['bumblebee'],
    darkTheme: 'bumblebee',  // wymuszamy bumblebee bez względu na tryb systemu
  }
}
```

**index.html** `<html data-theme="bumblebee">` – hardkodujemy temat.

### Struktura

```
src/app/
├── core/
│   ├── auth/
│   │   ├── auth.service.ts
│   │   ├── auth.guard.ts
│   │   └── jwt.interceptor.ts
│   ├── http/
│   │   └── api.service.ts         (bazowy serwis HTTP)
│   └── models/                    (interfejsy DTO)
├── features/
│   ├── auth/
│   │   ├── login/
│   │   │   ├── login.component.ts
│   │   │   ├── login.component.html
│   │   │   └── login.component.scss
│   │   └── register/
│   │       ├── register.component.ts
│   │       ├── register.component.html
│   │       └── register.component.scss
│   ├── games/
│   │   ├── game-list/
│   │   ├── game-detail/
│   │   └── game-form/
│   ├── clients/
│   │   ├── client-list/
│   │   └── client-form/
│   └── rentals/
│       ├── rental-list/
│       ├── issue-game/            (Wydaj grę)
│       └── return-game/           (Przyjmij grę)
└── shared/
    └── components/
        ├── navbar/
        ├── table/                  (reużywalny komponent tabeli)
        ├── badge/
        └── confirm-modal/
```

### Zasady frontendowe

- Każdy komponent = 3 pliki: `.ts`, `.html`, `.scss`
- `private readonly` na serwisach w konstruktorze
- Odsubskrybowanie przez `takeUntilDestroyed()` (Angular 16+) lub `DestroyRef` – **z wyjątkiem** wywołań HTTP (one-shot observables)
- Brak logiki w templateach – obliczenia w komponencie lub pipe'ach
- Serwisy per feature (nie jeden globalny `AppService`)
- Typowanie: brak `any`, wszystkie DTO jako interfejsy w `core/models`

### Panele i funkcje UI

| Widok            | Funkcje                                                                 |
|------------------|-------------------------------------------------------------------------|
| Lista gier       | Filtrowanie po tagach, sortowanie po tytule/dacie, licznik egzemplarzy  |
| Szczegóły gry    | Edycja, lista egzemplarzy z dostępnością, zarządzanie liczbą kopii      |
| Lista klientów   | Sortowanie, filtrowanie, badge z liczbą aktywnych wypożyczeń            |
| Szczegóły klienta| Historia wypożyczeń, aktywne wypożyczenia                               |
| Lista wypożyczeń | Filtrowanie: status (ACTIVE/RETURNED/OVERDUE), klient, gra, po terminie |
| Wydaj grę        | Wybierz klienta + dostępny egzemplarz + daty → utwórz rental            |
| Przyjmij grę     | Wybierz aktywne wypożyczenie → podaj stan egzemplarza → zwróć          |

---

## 8. Baza Danych

- PostgreSQL 16
- Flyway do migracji (`src/main/resources/db/migration/V1__init.sql` itd.)
- Połączenie przez Spring Data JPA / Hibernate
- UUID jako PK (generowane po stronie aplikacji lub `gen_random_uuid()`)

---

## 9. Bezpieczeństwo

- Hasła hashowane BCrypt
- JWT HS256, expiration 24h
- CORS skonfigurowany dla frontendu (`localhost:4200` dev, serwis Kubernetes prod)
- `SecurityFilterChain` w Spring Security – whitelist: `/api/auth/**`

---

## 10. Konteneryzacja

### Pliki Docker

**Backend – Dockerfile:**
```dockerfile
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY build/libs/app.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Frontend – Dockerfile:**
```dockerfile
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build -- --configuration=production

FROM nginx:alpine
COPY --from=build /app/dist/rental-games-frontend/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
```

**nginx.conf** – wymagane dla Angular routing:
```nginx
server {
  listen 80;
  location / {
    root /usr/share/nginx/html;
    try_files $uri $uri/ /index.html;
  }
}
```

### Kubernetes Manifesty (k8s/)

```
k8s/
├── namespace.yaml
├── postgres/
│   ├── deployment.yaml
│   ├── service.yaml
│   ├── pvc.yaml
│   └── secret.yaml             (dane logowania do bazy)
├── backend/
│   ├── deployment.yaml
│   ├── service.yaml
│   └── configmap.yaml          (zmienne env: DB_HOST, DB_PORT itp.)
├── frontend/
│   ├── deployment.yaml
│   └── service.yaml
└── ingress.yaml                (opcjonalnie – nginx ingress controller)
```

Każdy Deployment: `replicas: 1`, `imagePullPolicy: Never` (obrazy ładowane lokalnie do minikube).

---

## 11. Uruchomienie w Minikube

### Wymagania

- `minikube` zainstalowane i uruchomione
- `kubectl` skonfigurowane
- `Docker` dostępny

### Krok po kroku

```bash
# 1. Uruchom minikube
minikube start

# 2. Wskaż Dockera na rejestr minikube
eval $(minikube docker-env)

# 3. Zbuduj obrazy (z katalogów projektów)
docker build -t rental-backend:latest ./backend
docker build -t rental-frontend:latest ./frontend

# 4. Zastosuj manifesty
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/postgres/
kubectl apply -f k8s/backend/
kubectl apply -f k8s/frontend/

# 5. Sprawdź status
kubectl get pods -n rental-games

# 6. Dostęp do frontendu
minikube service rental-frontend -n rental-games
```

> Szczegółowa instrukcja z wyjaśnieniem każdego kroku znajduje się w `README.md` projektu.

---

## 12. README.md – Wymagana Zawartość

1. Krótki opis aplikacji
2. Stack technologiczny
3. Instrukcja uruchomienia lokalnie (dev mode)
4. Instrukcja uruchomienia w minikube (krok po kroku)
5. Opis struktury projektu
6. Sekcja: **Fragmenty wygenerowane przez AI** – z linkiem do rozmowy i opisem modyfikacji

---

## 13. Standardy Kodu

- SOLID, DRY, KISS
- Brak komentarzy – kod ma być samodokumentujący się
- `private`/`protected`/`public` na wszystkich polach i metodach
- Kotlin: `data class` dla DTO, `val` wszędzie gdzie możliwe
- Brak magicznych stringów – stałe w companion object lub enum
- Brak `TODO` i `FIXME` w kodzie wysyłanym na zaliczenie
- Obsługa błędów przez globalny `@RestControllerAdvice`
- Walidacja wejścia przez `@Valid` + Bean Validation (jakarta)
