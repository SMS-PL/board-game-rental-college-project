# Kubernetes od zera – na przykładzie Twojej wypożyczalni gier

> Cel: po przeczytaniu tego dokumentu rozumiesz **co** robi każda komenda i **dlaczego**, bez uciekania się do "po prostu wklej i działa".

---

## Część 1 – Co to jest konteneryzacja i po co to komu

### Problem bez kontenerów

Napisałeś aplikację na swoim kompasie. Działa. Wysyłasz na serwer. Nie działa. Czemu? Bo serwer ma inną wersję Javy, inny system, inne zmienne środowiskowe, inne ścieżki.

**Kontener rozwiązuje to tak:** pakujesz aplikację razem ze wszystkim czego potrzebuje – JDK, plikami konfiguracyjnymi, zależnościami – w jeden hermetyczny "pojemnik". Gdziekolwiek uruchomisz ten pojemnik, zachowuje się tak samo.

### Docker = narzędzie do budowania kontenerów

Docker to program który:
1. Czyta `Dockerfile` (przepis na kontener)
2. Buduje z niego **obraz** (Image) – gotowy snapshot systemu plików
3. Z obrazu uruchamia **kontener** – działający proces izolowany od systemu

```
Dockerfile  →  docker build  →  Image  →  docker run  →  Kontener
(przepis)                      (snapshot)               (działający proces)
```

### Analogia do Javy

- **Dockerfile** = plik `pom.xml` / kod źródłowy
- **Image** = skompilowany `.jar`
- **Kontener** = uruchomiony proces JVM

---

## Część 2 – Dockerfile Twojej aplikacji krok po kroku

### Backend (Kotlin Spring Boot)

```dockerfile
FROM eclipse-temurin:21-jre
```
**Co robi:** zaczyna od gotowego obrazu bazowego który ma już zainstalowane JRE 21. Nie musisz instalować Javy ręcznie – ona już tu jest.

```dockerfile
WORKDIR /app
```
**Co robi:** ustawia katalog roboczy wewnątrz kontenera. Jak `cd /app` ale też tworzy folder jeśli nie istnieje.

```dockerfile
COPY build/libs/app.jar app.jar
```
**Co robi:** kopiuje Twój skompilowany jar z maszyny hosta do kontenera pod nazwą `app.jar`. To jest ta jedyna rzecz specyficzna dla Twojego projektu.

```dockerfile
ENTRYPOINT ["java", "-jar", "app.jar"]
```
**Co robi:** mówi "gdy kontener startuje, uruchom to". To jest komenda która odpala Twój Spring Boot.

**Efekt końcowy:** masz obraz który po uruchomieniu startuje Twoją aplikację Spring Boot z Java 21, niezależnie od tego co jest na hoście.

### Frontend (Angular + Nginx)

```dockerfile
FROM node:20-alpine AS build
```
**Co robi:** pierwszy etap budowania – startuje z obrazem Node.js. `AS build` to nazwa etapu (multi-stage build).

```dockerfile
RUN npm ci && RUN npm run build
```
**Co robi:** instaluje zależności i buduje produkcyjny bundle Angulara (pliki statyczne HTML/JS/CSS).

```dockerfile
FROM nginx:alpine
COPY --from=build /app/dist/... /usr/share/nginx/html
```
**Co robi:** **drugi etap** – startuje od nowa z czystym Nginx (serwer HTTP) i kopiuje TYLKO zbudowane pliki statyczne. Node.js, `node_modules` (setki MB) – wszystko zostaje wyrzucone.

**Dlaczego dwa etapy?** Finalny obraz waży ~20MB zamiast ~500MB. Nginx nie potrzebuje Node żeby serwować statyczne pliki.

---

## Część 3 – Kubernetes. Co to i po co

### Problem bez Kubernetesa

Masz 3 kontenery: backend, frontend, baza. Uruchamiasz je ręcznie przez `docker run`. Co gdy kontener padnie? Musisz sam go zrestartować. Co gdy chcesz 3 instancje backendu? Ręcznie uruchamiasz 3 razy. Co gdy chcesz je skomunikować? Ręcznie konfigurujesz sieć.

**Kubernetes (K8s) to system orkiestracji** – pilnuje żeby Twoje kontenery działały zgodnie z tym co zadeklarowałeś, sam je restartuje, skaluje i łączy ze sobą.

### Minikube = Kubernetes na laptopie

Prawdziwy Kubernetes działa na klastrze wielu maszyn. Minikube symuluje taki klaster na jednej maszynie (Twoim laptopie) jako wirtualna maszyna lub kontener.

```
Twój laptop
└── minikube (VM/kontener)
    └── Kubernetes cluster
        ├── Pod: backend
        ├── Pod: frontend
        └── Pod: postgres
```

---

## Część 4 – Podstawowe pojęcia K8s (wyjaśnione przez Twoją aplikację)

### Pod

Najmniejsza jednostka w K8s. Jeden lub więcej kontenerów które działają razem na tym samym hoście i dzielą sieć.

**W Twoim projekcie:** każda aplikacja (backend, frontend, postgres) to osobny Pod.

```
Pod "rental-backend"
└── kontener: rental-backend:latest (Twój Spring Boot)
```

### Deployment

Mówi Kubernetesowi **jak** uruchamiać Pody. Deklarujesz: "chcę 1 instancję backendu, z tego obrazu, z tymi zmiennymi środowiskowymi". Deployment pilnuje żeby zawsze taka liczba Podów działała.

```yaml
# k8s/backend/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: rental-backend
  namespace: rental-games
spec:
  replicas: 1                    # chcę 1 Pod
  selector:
    matchLabels:
      app: rental-backend        # dotyczy Podów z tym labelem
  template:
    metadata:
      labels:
        app: rental-backend      # każdy tworzony Pod dostanie ten label
    spec:
      containers:
        - name: rental-backend
          image: rental-backend:latest
          imagePullPolicy: Never  # nie pobieraj z internetu, użyj lokalnego
          ports:
            - containerPort: 8080
          env:
            - name: DB_HOST
              value: postgres-service  # nazwa Service bazy danych (patrz niżej)
            - name: DB_PORT
              value: "5432"
```

**Co się dzieje gdy Pod padnie?** Deployment to wykrywa i automatycznie tworzy nowy. Ty nic nie robisz.

### Service

Problem: Pody mają losowe adresy IP które zmieniają się przy restarcie. Jak backend ma trafić do bazy skoro jej IP się zmienia?

**Service to stabilny endpoint** – daje stałą nazwę DNS i balansuje ruch do Podów pasujących do selectora.

```yaml
# k8s/backend/service.yaml
apiVersion: v1
kind: Service
metadata:
  name: rental-backend-service
  namespace: rental-games
spec:
  selector:
    app: rental-backend     # kieruj ruch do Podów z tym labelem
  ports:
    - port: 8080
      targetPort: 8080
  type: ClusterIP           # dostępny tylko wewnątrz klastra
```

```yaml
# k8s/frontend/service.yaml (dla dostępu z zewnątrz)
spec:
  type: NodePort            # dostępny z zewnątrz klastra przez port na node
```

**Typy Service:**
- `ClusterIP` – tylko wewnątrz klastra (backend ↔ baza)
- `NodePort` – dostępny z zewnątrz przez konkretny port (frontend)
- `LoadBalancer` – chmurowy load balancer (nas nie dotyczy)

### ConfigMap i Secret

Zmienne środowiskowe dla kontenerów. 

**ConfigMap** – niesekretne dane (np. `DB_HOST=postgres-service`, `DB_PORT=5432`).
**Secret** – dane sekretne zakodowane Base64 (np. hasło do bazy).

```yaml
# k8s/postgres/secret.yaml
apiVersion: v1
kind: Secret
metadata:
  name: postgres-secret
  namespace: rental-games
type: Opaque
data:
  POSTGRES_PASSWORD: cGFzc3dvcmQxMjM=  # "password123" w base64
  POSTGRES_USER: cmVudGFsX3VzZXI=      # "rental_user" w base64
```

> Base64 to **nie** szyfrowanie – to tylko enkodowanie. W produkcji używa się np. Vault. Na zaliczenie wystarczy.

### PersistentVolumeClaim (PVC)

Kontenery są efemeryczne – gdy Pod pada, dane w nim znikają. Postgres potrzebuje miejsca na dysku które **przeżyje** restart Poda.

PVC to "zamówienie na dysk" – mówisz "potrzebuję 1GB miejsca", K8s przydziela go i montuje do kontenera.

```yaml
# k8s/postgres/pvc.yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: postgres-pvc
  namespace: rental-games
spec:
  accessModes:
    - ReadWriteOnce      # tylko jeden Pod naraz może pisać
  resources:
    requests:
      storage: 1Gi
```

### Namespace

Logiczna izolacja zasobów w klastrze. Twoja aplikacja żyje w namespace `rental-games` żeby nie mieszać się z innymi rzeczami.

```yaml
# k8s/namespace.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: rental-games
```

---

## Część 5 – Jak to wszystko się komunikuje

```
                    [ Twoja przeglądarka ]
                           │
                     NodePort :30080
                           │
                    ┌──────▼───────┐
                    │   frontend   │  (nginx serwuje Angular)
                    │   Service    │
                    └──────────────┘
                           │  API calls → /api/*
                    ┌──────▼───────────────┐
                    │  backend-service     │  (ClusterIP)
                    │  rental-backend Pod  │  (Spring Boot :8080)
                    └──────────────────────┘
                           │  JDBC
                    ┌──────▼───────────────┐
                    │  postgres-service    │  (ClusterIP)
                    │  postgres Pod        │  (:5432)
                    │  + PVC (dane na dysk)│
                    └──────────────────────┘
```

**Jak backend trafia do bazy?**  
W `application.properties` ustawiasz `spring.datasource.url=jdbc:postgresql://postgres-service:5432/rentaldb`. `postgres-service` to nazwa Service bazy – K8s wbudowany DNS rozwiązuje to na właściwy IP.

**Jak frontend trafia do backendu?**  
Nginx na froncie proxy'uje `/api/*` do `rental-backend-service:8080`. Albo Angular environment ma ustawiony URL backendu.

---

## Część 6 – Krok po kroku: co się dzieje przy `minikube start`

1. **minikube start** – tworzy VM (lub kontener Docker) która będzie udawać node Kubernetesa. Wewnątrz niej startuje cały K8s (API server, scheduler, etcd – baza danych stanu K8s).

2. **eval $(minikube docker-env)** – to magia. Normalnie `docker build` buduje obraz na Twoim laptopie. Po tej komendzie Docker SDK wskazuje na Dockera **wewnątrz minikube**. Dzięki temu zbudowany obraz jest widoczny dla K8s który tam działa.

3. **docker build** – budujesz obrazy. Trafiają do rejestru Docker wewnątrz minikube.

4. **kubectl apply -f k8s/namespace.yaml** – wysyłasz manifest do API servera K8s. On zapisuje to w etcd i tworzy namespace.

5. **kubectl apply -f k8s/postgres/** – K8s tworzy PVC (rezerwuje dysk), Secret, Deployment (który tworzy Pod z Postgresem) i Service (stabilny endpoint do Poda).

6. **kubectl apply -f k8s/backend/** – to samo dla backendu. Pod backendu startuje, łączy się z `postgres-service` (DNS wewnątrz K8s), Spring Boot się inicjuje, Flyway robi migracje.

7. **kubectl apply -f k8s/frontend/** – Pod frontendu startuje, nginx serwuje pliki Angulara.

8. **minikube service rental-frontend -n rental-games** – otwiera tunel do NodePort Service frontendu i daje Ci URL w przeglądarce.

---

## Część 7 – Przydatne komendy do debugowania

```bash
# Co się dzieje z Podami?
kubectl get pods -n rental-games

# Pod się nie startuje? Sprawdź logi
kubectl logs <nazwa-poda> -n rental-games

# Pod crashuje w pętli? Opisz go
kubectl describe pod <nazwa-poda> -n rental-games

# Wejdź do kontenera (jak SSH)
kubectl exec -it <nazwa-poda> -n rental-games -- /bin/sh

# Status wszystkich zasobów
kubectl get all -n rental-games

# Usuń wszystko i zacznij od nowa
kubectl delete namespace rental-games
kubectl apply -f k8s/namespace.yaml
# (itd.)
```

---

## Część 8 – Typowe błędy i co oznaczają

| Status Poda          | Co to znaczy                                              | Jak naprawić                                       |
|----------------------|-----------------------------------------------------------|----------------------------------------------------|
| `ImagePullBackOff`   | K8s nie może pobrać obrazu                               | Czy zrobiłeś `eval $(minikube docker-env)` przed `docker build`? |
| `CrashLoopBackOff`   | Kontener startuje i pada w kółko                         | `kubectl logs <pod>` – co wyrzuca aplikacja?       |
| `Pending`            | Pod czeka na zasoby lub PVC nie może być przydzielone    | `kubectl describe pod <pod>` – sekcja Events       |
| `Error`              | Kontener wystartował ale zakończył się z błędem          | Sprawdź logi                                        |
| `Running`            | Wszystko gra                                             | 🎉                                                  |

---

## Podsumowanie: przepływ pracy

```
1. Piszesz kod
      ↓
2. docker build (z eval minikube docker-env)
      ↓
3. kubectl apply -f k8s/
      ↓
4. K8s czyta manifesty, tworzy zasoby
      ↓
5. Scheduler przydziela Pody do node (tu jest jeden node = minikube)
      ↓
6. Kubelet na node uruchamia kontenery z obrazów Docker
      ↓
7. Service tworzy DNS i balansuje ruch
      ↓
8. minikube service daje Ci URL do aplikacji
```

Tyle. Nic magicznego – K8s to system który czyta Twoje YAML-e i pilnuje żeby stan faktyczny (działające Pody) zgadzał się z zadeklarowanym (replicas: 1).
