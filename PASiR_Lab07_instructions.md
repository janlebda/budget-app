# PASiR Lab07 – Finalizacja, refaktoryzacja i jakość projektu

> Instrukcja dla GitHub Copilot / VS Code – wykonaj kolejno wszystkie sekcje.

---

## 1. Wymagania wstępne

Upewnij się, że zainstalowane są:
- JDK 25
- Maven 3.9.x+ (zmienne `M2_HOME` i `PATH` ustawione)
- MySQL 8.4.x
- Node.js 24.13+ (LTS)
- Git 2.53+
- Docker 4.62+
- VS Code z rozszerzeniami: `Debugger for Java`, `Maven for Java`, `Spring Boot Dashboard`, `Language Support for Java™ by Red Hat`, `Java Test Runner`

---

## 2. Struktura mono-repozytorium

### 2.1 Utwórz strukturę folderów

```
BudgetApp/
├── backend/      ← skopiuj tu istniejący projekt Spring Boot
├── frontend/     ← skopiuj tu istniejący projekt React
├── .gitignore
├── README.md
└── docker-compose.yml
```

### 2.2 Usuń stare repozytoria Git z podfolderów

```bash
rm -rf frontend/.git
rm -rf backend/.git
```

### 2.3 Utwórz `.gitignore` w katalogu głównym `BudgetApp/`

```gitignore
# --------------------------------
### For All ###
# --------------------------------
.DS_Store
*.tmp

# --------------------------------
### Backend ###
# --------------------------------
HELP.md
target/
.mvn/wrapper/maven-wrapper.jar
!**/src/main/**/target/
!**/src/test/**/target/

### STS ###
.apt_generated
.classpath
.factorypath
.project
.settings
.springBeans
.sts4-cache

### IntelliJ IDEA ###
.idea
*.iws
*.iml
*.ipr

### NetBeans ###
/nbproject/private/
/nbuild/
/dist/
/nbdist/
/.nb-gradle/
build/
!**/src/main/**/build/
!**/src/test/**/build/

### VS Code ###
.vscode/

### Environment Files ###
.env
.env.local
.env.*.local

### Docker ###
docker-compose.override.yml
.docker/
docker/.env

# --------------------------------
### Frontend ###
# --------------------------------
# Logs
logs
*.log
npm-debug.log*
yarn-debug.log*
yarn-error.log*
pnpm-debug.log*
lerna-debug.log*

node_modules
dist
dist-ssr
*.local

# Editor directories and files
.vscode/*
!.vscode/extensions.json
.idea
.DS_Store
*.suo
*.ntvs*
*.njsproj
*.sln
*.sw?
```

### 2.4 Utwórz `README.md` w `BudgetApp/`

```markdown
# BudgetApp

Aplikacja do zarządzania budżetem osobistym i grupowym.

## Autor
Imię Nazwisko

## Technologie
- Backend: Java 25, Spring Boot, MySQL 8.4
- Frontend: React + Vite + TypeScript
- Infrastruktura: Docker, Docker Compose
```

### 2.5 Zainicjalizuj mono-repozytorium

```bash
cd BudgetApp
git init
git add .
git commit -m "Initial monorepo setup: frontend + backend"
git remote add origin https://github.com/twoj-uzytkownik/fullstack-app.git
git push -u origin main
```

---

## 3. Konfiguracja Dockera

### 3.1 Sprawdź wersje Docker i Docker Compose

```bash
docker --version
docker compose version
```

> Wszystkie komendy `docker compose` uruchamiaj z katalogu `BudgetApp/`.

### 3.2 Utwórz `BudgetApp/docker-compose.yml`

```yaml
services:
  mysql:
    image: mysql:8.4
    restart: unless-stopped
    env_file:
      - ./backend/.env
    ports:
      - "3306:3306"
    command:
      - mysqld
      - --character-set-server=utf8mb4
      - --collation-server=utf8mb4_0900_ai_ci
    volumes:
      - mysql_data:/var/lib/mysql
      - ./backend/docker/initdb:/docker-entrypoint-initdb.d:ro
    healthcheck:
      test: ["CMD-SHELL", "mysqladmin ping -h localhost -u root -p$${MYSQL_ROOT_PASSWORD} || exit 1"]
      interval: 30s
      timeout: 10s
      retries: 5
      start_period: 60s

  backend:
    build:
      context: ./backend
    restart: unless-stopped
    env_file:
      - ./backend/.env
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/$${MYSQL_DATABASE}?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC
    ports:
      - "8080:8080"
    depends_on:
      mysql:
        condition: service_healthy

  frontend:
    build:
      context: ./frontend
    restart: unless-stopped
    ports:
      - "5174:8080"
    depends_on:
      - backend

volumes:
  mysql_data:
```

> **Ważne:** W `SPRING_DATASOURCE_URL` podwójny `$$` jest celowy — Docker Compose przekazuje `${MYSQL_DATABASE}` do kontenera bez podstawiania wartości, a Spring Boot rozwiązuje zmienną ze swojego `.env`.

### 3.3 Utwórz `BudgetApp/backend/Dockerfile`

```dockerfile
FROM maven:3.9-eclipse-temurin-25 AS build

WORKDIR /app

COPY pom.xml mvnw ./
COPY .mvn .mvn
COPY src src

RUN chmod +x mvnw && ./mvnw -DskipTests package

FROM eclipse-temurin:25-jre

WORKDIR /app

RUN useradd --system --create-home --shell /usr/sbin/nologin appuser

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

USER appuser

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 3.4 Utwórz `BudgetApp/backend/.dockerignore`

```
target
.idea
.env
docker
docs
postman
```

### 3.5 Utwórz `BudgetApp/frontend/Dockerfile`

```dockerfile
FROM node:24-alpine AS build

WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY index.html ./
COPY tsconfig*.json ./
COPY vite.config.ts ./
COPY public public
COPY src src

RUN npm run build

FROM nginxinc/nginx-unprivileged:1.27-alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 8080
```

### 3.6 Utwórz `BudgetApp/frontend/nginx.conf`

```nginx
server {
    listen 8080;
    server_name _;

    root /usr/share/nginx/html;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

### 3.7 Utwórz `BudgetApp/frontend/.dockerignore`

```
node_modules
dist
.idea
*.log
```

### 3.8 Zbuduj i uruchom aplikację

```bash
docker compose up -d --build
```

### 3.9 Sprawdź status kontenerów

```bash
docker compose ps
```

Oczekiwany wynik — trzy usługi `mysql`, `backend`, `frontend` ze statusem `running`.

### 3.10 Adresy aplikacji

| Usługa   | Adres                        |
|----------|------------------------------|
| Frontend | http://localhost:5174        |
| Backend  | http://localhost:8080        |
| MySQL    | `mysql:3306` (wewnątrz sieci Docker) |

### 3.11 Podgląd logów

```bash
docker compose logs -f            # wszystkie usługi
docker compose logs -f backend    # tylko backend
docker compose logs -f frontend   # tylko frontend
docker compose logs -f mysql      # tylko MySQL
# Wyjście: Ctrl+C
```

### 3.12 Zatrzymanie i czyszczenie

```bash
# Zatrzymanie bez usuwania danych
docker compose down

# Ponowne uruchomienie
docker compose up -d

# Pełne wyczyszczenie (usuwa dane bazy!)
docker compose down -v
docker compose up -d --build
```

### 3.13 Weryfikacja działania (obowiązkowe przed commitem)

1. Zarejestruj dwóch użytkowników
2. Zaloguj się na każdego i dodaj przynajmniej jedną transakcję — sprawdź listę
3. Dodaj grupę, dodaj do niej drugiego użytkownika
4. Dodaj kilka długów w grupie, zatwierdź je sprawdzając na dwóch przeglądarkach jednocześnie (jedna w trybie incognito)

```bash
git add .
git commit -m "feat: full Docker containerization"
git push
```

---

## 4. Integracja SonarCloud z GitHub Actions

### 4.1 Utwórz nowe repozytorium na swoim prywatnym GitHubie

```bash
git remote add mygithub https://github.com/twoj-uzytkownik/BudgetApp.git
git push mygithub main
```

### 4.2 Zarejestruj się i skonfiguruj SonarCloud

1. Wejdź na https://sonarcloud.io i zaloguj przez GitHub
2. Wybierz organizację → wybierz repozytorium → kliknij **Set Up**
3. Wybierz **GitHub Actions** jako metodę implementacji
4. Zapisz wygenerowany `SONAR_TOKEN` jako sekret w repozytorium GitHub:
   `Settings → Secrets and variables → Actions → New repository secret`
   Nazwa: `SONAR_TOKEN`

### 4.3 Utwórz `.github/workflows/sonar.yml`

```yaml
name: SonarCloud CI

on:
  push:
    branches:
      - master
  pull_request:
    types: [opened, synchronize, reopened]

jobs:
  build-and-analyze:
    name: Build & Analyze Monorepo
    runs-on: ubuntu-latest
    steps:
      - name: Checkout repository
        uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - name: Set up Java
        uses: actions/setup-java@v4
        with:
          java-version: 21
          distribution: "zulu"

      - name: Cache Maven
        uses: actions/cache@v4
        with:
          path: ~/.m2
          key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
          restore-keys: ${{ runner.os }}-m2

      - name: Build backend
        working-directory: backend/PASiR_Nazwisko_Imie   # <- zmień na swój folder
        run: mvn clean install -DskipTests

      - name: Set up Node.js
        uses: actions/setup-node@v4
        with:
          node-version: 22

      - name: Install frontend deps
        working-directory: frontend
        run: |
          npm install
          npm run build

      - name: Cache SonarCloud
        uses: actions/cache@v4
        with:
          path: ~/.sonar/cache
          key: ${{ runner.os }}-sonar
          restore-keys: ${{ runner.os }}-sonar

      - name: SonarCloud Scan
        uses: SonarSource/sonarqube-scan-action@v5
        env:
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
```

### 4.4 Utwórz `BudgetApp/sonar-project.properties`

```properties
# ogólne
sonar.projectKey=Nazwisko_BudgetApp
sonar.organization=nazwisko
sonar.host.url=https://sonarcloud.io

# moduły
sonar.modules=backend,frontend

# backend
backend.sonar.projectBaseDir=backend/PASiR_Nazwisko_Imie
backend.sonar.sources=src/main
backend.sonar.java.binaries=target/classes

# frontend (bez testów)
frontend.sonar.projectBaseDir=frontend
frontend.sonar.sources=src
frontend.sonar.exclusions=**/*.spec.ts,**/*.test.tsx,**/__tests__/**
```

> Zastąp `Nazwisko_Imie`, `Nazwisko_BudgetApp`, `nazwisko` swoimi danymi.

### 4.5 Oczekiwana struktura projektu (`WorkTree`)

```
BUDGETAPP/
├── .github/
│   └── workflows/
│       └── sonar.yml
├── backend/
├── frontend/
├── .gitignore
└── sonar-project.properties
```

---

## 5. Bugfixing – praca samodzielna

Przejdź do zakładki **GitHub → Actions** i sprawdź wyniki pipeline'u SonarCloud.

Napraw wszystkie błędy zgłoszone przez Sonara (zarówno w backendzie jak i frontendzie) aż pipeline będzie zielony.

```bash
git add .
git commit -m "fix: resolve SonarCloud issues"
git push
```

---

## 6. Zakończenie

### 6.1 Pull Request

1. Utwórz Pull Request na GitHubie
2. **Nie usuwaj gałęzi** po merge
3. Zaktualizuj lokalną gałąź `main`:

```bash
git checkout main
git pull
```

### 6.2 Spakowanie projektu do oddania

```bash
git archive -o PASiR_Lab07_Nazwisko_Imie.zip main
```

Jeśli komenda nie działa — spakuj ręcznie folder projektu jako ZIP i nazwij go `PASiR_Lab07_Nazwisko_Imie.zip`.

Prześlij ZIP na **Delta** oraz wyślij projekt na **GitHub Classroom**.

---

## Źródła

- https://www.jetbrains.com/idea
- https://spring.io/projects/spring-boot
- https://maven.apache.org/install.html
- https://www.mysql.com/
- https://sonarcloud.io
- https://docs.docker.com/compose/
