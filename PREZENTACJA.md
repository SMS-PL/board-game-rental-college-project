# Dokumentacja Projektu – Wypożyczalnia Gier Planszowych
### Materiał do prezentacji zaliczeniowej

---

## 1. Co robi ta aplikacja?

Aplikacja webowa do zarządzania wypożyczalnią gier planszowych. Umożliwia:

- Rejestrację i logowanie użytkowników (JWT)
- Zarządzanie grami i ich egzemplarzami
- Zarządzanie klientami
- Tworzenie i obsługę wypożzyczeń (wydanie + zwrót z oceną stanu)

Składa się z trzech elementów:

```
Przeglądarka → Angular (frontend) → Spring Boot (backend) → PostgreSQL (baza)
```

---

## 2. Stack technologiczny – co i dlaczego

| Warstwa | Technologia | Dlaczego |
|--------|------------|----------|
| Backend | Kotlin + Spring Boot 4 | Kotlin = zwięzły, null-safe; Spring Boot = gotowa infrastruktura HTTP, security, JPA |
| Architektura | CQRS + Vertical Slices | Każda operacja to osobna klasa, łatwo znaleźć co robi konkretna funkcja |
| Frontend | Angular 19 + Tailwind + DaisyUI | Angular = framework SPA z silnym typowaniem; DaisyUI = gotowe komponenty UI |
| Baza danych | PostgreSQL 16 | Relacyjna baza, UUID jako klucze główne |
| Migracje | Liquibase (YAML) | Wersjonowanie struktury bazy – przy każdym starcie aplikacja sprawdza czy tabele istnieją |
| Autentykacja | JWT (HS256) | Bezstanowy token – serwer nie musi pamiętać sesji |
| Konteneryzacja | Docker + Kubernetes (minikube) | Pełna izolacja środowiska, działa tak samo u każdego |

---

## 3. Architektura backendu – CQRS + Vertical Slices

### Co to jest CQRS?

**CQRS = Command Query Responsibility Segregation** – rozdzielenie odczytu od zapisu.

- **Command** = operacja zmieniająca stan (stwórz grę, zwróć wypożyczenie)
- **Query** = operacja odczytująca dane (pobierz listę gier, pobierz klienta)

Każda operacja ma **osobną klasę handlera** który robi **jedną rzecz**. Zamiast jednego serwisu `GameService` z 10 metodami – mamy 7 osobnych handlerów.

### Co to są Vertical Slices?

Zamiast dzielić kod warstwami (wszystkie kontrolery razem, wszystkie serwisy razem) – dzielimy go **funkcjonalnościami**:

```
game/
├── Game.kt                           ← encja JPA (mapowanie na tabelę)
├── GameCopy.kt                       ← encja JPA
├── controller/
│   └── GameController.kt             ← przyjmuje HTTP, deleguje do handlerów
├── commands/
│   ├── CreateGameCommand.kt          ← dane wejściowe do tworzenia gry
│   └── UpdateGameCommand.kt
├── handlers/
│   ├── CreateGameCommandHandler.kt   ← logika tworzenia gry
│   ├── GetGamesQueryHandler.kt       ← logika pobierania listy
│   └── ...
├── queries/
│   ├── GameDto.kt                    ← dane wyjściowe (co zwracamy w API)
│   └── GameDetailDto.kt
├── repository/
│   └── GameRepository.kt             ← interfejs Spring Data JPA
└── enums/
    ├── GameTag.kt
    └── CopyCondition.kt
```

**Zaleta:** żeby zrozumieć jak działa "tworzenie gry" – patrzysz tylko na `CreateGameCommand.kt` + `CreateGameCommandHandler.kt`.

### Jak przepływa żądanie HTTP? (przykład: POST /api/games)

```
HTTP POST /api/games
        ↓
GameController.createGame()
        ↓  deserializuje body do CreateGameCommand
CreateGameCommandHandler.handle()
        ↓  tworzy encję Game, zapisuje przez GameRepository
        ↓  tworzy N egzemplarzy GameCopy (tyle ile totalCopies)
GameRepository.save()  →  PostgreSQL
        ↓
zwraca { id: UUID }  →  HTTP 201 Created
```

---

## 4. Encje (model domenowy)

### BaseEntity – wspólna baza dla wszystkich encji

```kotlin
@MappedSuperclass
abstract class BaseEntity(
    @Id val id: UUID = UUID.randomUUID(),
    val createdAt: LocalDateTime = LocalDateTime.now()
)
```

Każda encja dziedziczy `id` i `createdAt` – nie trzeba ich powtarzać w każdej klasie.

### Game (Gra)

```kotlin
@Entity @Table(name = "games")
class Game(
    var title: String,
    var description: String,
    var tags: MutableSet<GameTag>,   // STRATEGY, FAMILY, PARTY, COOPERATIVE...
    var totalCopies: Int
) : BaseEntity()
```

Tagi są w osobnej tabeli `game_tags` (adnotacja `@ElementCollection`).

### GameCopy (Egzemplarz gry)

```kotlin
class GameCopy(
    var game: Game,
    var copyNumber: Int,             // 1, 2, 3...
    var condition: CopyCondition,    // NEW, GOOD, WORN, DAMAGED
    var isAvailable: Boolean
) : BaseEntity()
```

Gdy tworzysz grę z `totalCopies = 3`, powstają 3 wiersze w tabeli `game_copies`.

### Client (Klient)

Proste dane: `firstName`, `lastName`, `phone?`, `email?`.

### Rental (Wypożyczenie)

```kotlin
class Rental(
    var gameCopy: GameCopy,          // konkretny egzemplarz, nie gra
    var client: Client,
    var rentedFrom: LocalDate,
    var dueTo: LocalDate,
    var returnedAt: LocalDate?,      // null = jeszcze nie zwrócone
    var conditionOnReturn: CopyCondition?,
    var status: RentalStatus,        // ACTIVE, RETURNED, OVERDUE
    var notes: String?
) : BaseEntity()
```

Status `OVERDUE` jest wyliczany dynamicznie: jeśli `ACTIVE` i `dueTo < today` → `OVERDUE`.

---

## 5. Autentykacja – JWT

### Jak działa logowanie?

```
POST /api/auth/login  { username, password }
         ↓
LoginCommandHandler sprawdza hasło przez BCrypt
         ↓
JwtService.generateToken(username)  →  token HS256, ważny 24h
         ↓
zwraca { token: "eyJ..." }
```

### Jak działa autoryzacja przy każdym żądaniu?

```
GET /api/games
Authorization: Bearer eyJ...
         ↓
JwtAuthenticationFilter (OncePerRequestFilter)
         ↓  wyciąga token z headera Authorization
JwtService.isTokenValid()  →  sprawdza podpis + datę wygaśnięcia
         ↓  jeśli OK → ustawia użytkownika w SecurityContextHolder
GameController.getGames()  ← request dociera do kontrolera
```

### Co to jest JWT?

Format: `header.payload.signature` – trzy człony zakodowane Base64, oddzielone kropką.

- **Header** – algorytm (HS256)
- **Payload** – dane (username, issued at, expiration)
- **Signature** – HMAC podpis kluczem tajnym

Serwer **nie przechowuje sesji** – weryfikuje token matematycznie przy każdym żądaniu.

### Które endpointy są publiczne?

```kotlin
.requestMatchers("/api/auth/**").permitAll()
.anyRequest().authenticated()
```

Tylko `POST /api/auth/register` i `POST /api/auth/login`. Reszta wymaga tokenu.

---

## 6. Frontend – Angular

### Jak Angular komunikuje się z backendem?

Angular działa jako **SPA (Single Page Application)** – nie przeładowuje strony.

```
Użytkownik klika "Dodaj grę"
        ↓
GamesListComponent.onSubmit()
        ↓
GameService.createGame(form)  →  HttpClient.post('/api/games', body)
        ↓ (AuthInterceptor dodaje Bearer token automatycznie)
Backend zwraca { id: UUID }
        ↓
Komponent odświeża listę gier
```

### AuthInterceptor

Przechwytuje **każde** wychodzące żądanie HTTP i dokłada token:

```typescript
const token = localStorage.getItem('token');
req = req.clone({
  headers: req.headers.set('Authorization', `Bearer ${token}`)
});
```

Zarejestrowany globalnie w `app.config.ts` – działa dla wszystkich serwisów bez zmian w nich.

### AuthGuard

Chroni trasy Angular Router. Jeśli użytkownik nie jest zalogowany i próbuje wejść na `/games` → przekierowanie na `/login`.

### Struktura frontendu

```
core/
├── models/        ← interfejsy TypeScript (Game, Rental, Customer...)
├── services/      ← komunikacja z API (GameService, RentalService...)
├── guards/        ← AuthGuard
└── interceptors/  ← AuthInterceptor

features/
├── auth/          ← login, rejestracja
├── games/         ← lista gier, szczegóły gry
├── customers/     ← lista klientów, historia wypożzyczeń
├── rentals/       ← lista wypożzyczeń, tworzenie, zwrót
└── dashboard/     ← statystyki
```

---

## 7. Baza danych – PostgreSQL + Liquibase

### Tabele i relacje

```
app_users     (id, username, password, created_at)
games         (id, title, description, total_copies, created_at)
game_tags     (game_id FK, tag)
game_copies   (id, game_id FK, copy_number, condition, is_available, created_at)
clients       (id, first_name, last_name, phone, email, created_at)
rentals       (id, game_copy_id FK, client_id FK, rented_from, due_to,
               returned_at, condition_on_return, status, notes, created_at)
```

### Co to jest Liquibase i dlaczego nie `ddl-auto: create`?

`ddl-auto: create` kasuje i odtwarza wszystkie tabele przy każdym restarcie → **utrata danych**.

**Liquibase** wersjonuje strukturę bazy:
1. Sprawdza tabelę `databasechangelog`
2. Jeśli changeset `001-init` nie był wykonany → tworzy tabele
3. Jeśli był → pomija

Nowe migracje to nowy plik `002_add_column.yaml` – Liquibase wykona go tylko raz.

### Dlaczego UUID zamiast auto-increment?

1. UUID generowany w aplikacji – znamy ID przed zapisem do bazy
2. Globalnie unikalny – bezpieczne łączenie danych z różnych źródeł
3. Trudniejszy do odgadnięcia niż `id=1, 2, 3`

---

## 8. Konteneryzacja – Docker + Kubernetes

### Docker

Obraz zawiera aplikację + wszystkie zależności. Działa tak samo na każdej maszynie.

**Backend:**
```dockerfile
FROM eclipse-temurin:21-jre
COPY build/libs/gamerental-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Frontend** (multi-stage build – Node buduje Angular, nginx serwuje wynik):
```dockerfile
FROM node:20-alpine AS build
RUN npm ci && npm run build -- --configuration=production
FROM nginx:alpine
COPY --from=build /app/dist/game-rental-frontend/browser /usr/share/nginx/html
```

Finalny obraz waży ~20MB zamiast ~500MB – `node_modules` są wyrzucane po buildzie.

### Kubernetes – kluczowe pojęcia

**Pod** – jeden działający kontener. Najmniejsza jednostka K8s.

**Deployment** – deklaracja: "chcę 1 Pod z tym obrazem". Jeśli Pod padnie → K8s automatycznie go odtworzy.

**Service** – stały adres DNS dla Podów. Pody mają losowe IP. Service (`postgres-service`, `backend-service`) ma stałą nazwę – inne Pody zawsze trafiają pod właściwy adres.

**PersistentVolumeClaim (PVC)** – rezerwacja dysku. Postgres potrzebuje miejsca które przeżyje restart kontenera. PVC montuje dysk hosta do kontenera pod `/var/lib/postgresql/data`.

**ConfigMap** – niesekretne zmienne środowiskowe (np. `DB_HOST=postgres-service`).

**Secret** – sekretne dane zakodowane Base64 (hasła do bazy).

**initContainer** – kontener uruchamiany przed głównym. Nasz `db-migrate` czeka aż PostgreSQL odpowie, potem tworzy tabele przez psql.

### Jak komponenty się komunikują w K8s?

```
Przeglądarka
    | NodePort :30080
    v
rental-frontend Service
    | nginx proxy_pass /api/* →
    v
backend-service (ClusterIP)
    | Spring Boot :8080
    v
rental-backend Pod
    | jdbc:postgresql://postgres-service:5432/rental-game-db
    v
postgres-service (ClusterIP)
    v
postgres Pod  <→>  PVC (dysk, dane trwałe)
```

### Dane w Kubernetes – kiedy znikają?

| Sytuacja | Dane w bazie |
|----------|-------------|
| `./deploy.sh` (ponowne uruchomienie) | zachowane – Postgres nie jest restartowany |
| Restart Poda backendu | zachowane – dane są na PVC |
| `kubectl delete namespace rental-games` | USUNIETE – PVC też jest usuwany |

---

## 9. Przepływ danych – scenariusze

### Wypożyczenie gry

```
1. GET /api/games                          → lista gier
2. GET /api/games/{id}/available-copies    → dostępne egzemplarze
3. GET /api/clients                        → lista klientów
4. POST /api/rentals
   { gameCopyId, clientId, rentedFrom, dueTo }
   Backend:
     - sprawdza isAvailable == true
     - tworzy Rental (status: ACTIVE)
     - ustawia GameCopy.isAvailable = false
```

### Zwrót gry

```
PATCH /api/rentals/{id}/return
{ conditionOnReturn: "GOOD" }
Backend:
  - ustawia returnedAt = today
  - ustawia status = RETURNED
  - ustawia GameCopy.isAvailable = true
  - aktualizuje GameCopy.condition
```

---

## 10. Potencjalne pytania prowadzącego

**Q: Co to jest Spring Boot i czym różni się od zwykłego Springa?**
Spring to framework IoC. Spring Boot dodaje auto-konfigurację – nie trzeba pisać XML, mamy wbudowany Tomcat. Wystarczy zależność `spring-boot-starter-web` żeby mieć działający serwer HTTP.

**Q: Co to jest JPA / Hibernate?**
JPA = Jakarta Persistence API – standard ORM. Hibernate to implementacja. Zamiast pisać SQL – operujesz na obiektach Kotlin, Hibernate generuje SQL automatycznie. `@Entity`, `@Table`, `@Column`, `@ManyToOne` to adnotacje JPA.

**Q: Co to jest `@Transactional`?**
Wszystkie operacje bazodanowe w metodzie są jedną transakcją. Albo wszystko się uda, albo nic (rollback). Przykład: tworzenie gry + tworzenie egzemplarzy – jeśli tworzenie egzemplarzy się nie powiedzie, gra też nie jest zapisana.

**Q: Co to jest BCrypt?**
Algorytm hashowania haseł – jednokierunkowy. Hasła nigdy nie są w plaintext. Przy logowaniu hash wpisanego hasła jest porównywany z hashem w bazie.

**Q: Co to jest CORS?**
Cross-Origin Resource Sharing – polityka bezpieczeństwa przeglądarki. Frontend na `port:4200` i backend na `port:8080` to różne origin. Przeglądarka blokuje żądania między nimi domyślnie. Backend musi zezwolić przez header `Access-Control-Allow-Origin`.

**Q: Skąd Angular wie żeby dodać token do żądań?**
`AuthInterceptor` – zarejestrowany globalnie w `app.config.ts`. Każde wychodzące żądanie przechodzi przez interceptor który dodaje `Authorization: Bearer <token>`.

**Q: Co to jest Liquibase i po co, skoro można `ddl-auto: create`?**
`ddl-auto: create` niszczy dane przy każdym restarcie. Liquibase śledzi historię changesetów – każdy wykonuje się dokładnie raz. Kolejne migracje to nowe pliki YAML.

**Q: Dlaczego UUID a nie Long jako ID?**
Long jest generowany przez bazę – znamy ID dopiero po INSERT. UUID jest generowany w aplikacji przed zapisem. Dodatkowo UUID jest globalnie unikalny i nie ujawnia liczby rekordów.

**Q: Co to jest minikube?**
Kubernetes normalnie działa na klastrze wielu maszyn. Minikube symuluje klaster na jednej maszynie – tworzy VM z pełnym K8s w środku. Manifesty YAML są identyczne jak dla produkcji.

**Q: Dlaczego nginx w kontenerze frontendu?**
Angular kompiluje się do statycznych plików (HTML/JS/CSS). Nginx je serwuje i proxy'uje `/api/*` do `backend-service:8080` – dzięki temu przeglądarka wysyła wszystko na jeden host, brak problemu z CORS.

**Q: Co to jest initContainer i dlaczego go mamy?**
InitContainer uruchamia się przed głównym kontenerem i musi zakończyć się sukcesem. Nasz `db-migrate` czeka aż PostgreSQL będzie gotowy (`pg_isready`), potem tworzy tabele. Gwarantuje że Spring Boot nie wystartuje zanim baza jest gotowa.

**Q: Co to jest PVC?**
PersistentVolumeClaim to rezerwacja dysku w Kubernetes. Kontenery są efemeryczne. PVC montuje dysk hosta do kontenera. Dane Postgresa żyją na tym dysku, przetrwają restarty Podów.
