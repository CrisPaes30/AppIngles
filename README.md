# English Memory AI

An intelligent English vocabulary learning platform with spaced repetition (SM-2 algorithm), AI-generated exercises, and sentence analysis.

## Tech Stack

| Layer     | Technology                                                                  |
|-----------|-----------------------------------------------------------------------------|
| Backend   | Java 17, Spring Boot 3.2, Spring Data JPA, Spring Security, Liquibase      |
| Database  | Oracle Database (XE 21c / XEPDB1)                                          |
| AI Layer  | Pluggable interface (`AiExerciseService`) — only OpenAI implemented today  |
| Frontend  | React 18, TypeScript, Vite 5, Tailwind CSS 3, TanStack Query, React Router |
| Docs      | SpringDoc OpenAPI 2 (Swagger UI)                                            |
| Quality   | JaCoCo, Checkstyle, Maven Surefire                                          |

## Architecture

```
AppIngles/
├── backend/          ← Spring Boot (Clean Architecture)
│   └── src/main/java/com/englishmemory/
│       ├── controller/     ← REST layer (HTTP in/out)
│       ├── service/        ← Business logic + AI interface
│       │   ├── ai/         ← AiExerciseService + providers
│       │   └── impl/       ← Service implementations
│       ├── entity/         ← JPA entities (+ BaseEntity audit)
│       ├── repository/     ← Spring Data JPA repositories
│       ├── dto/            ← Request/Response DTOs
│       ├── mapper/         ← MapStruct mappers
│       ├── util/           ← Sm2Algorithm, JsonListConverter
│       └── config/         ← Security, OpenAPI, DataInitializer
└── frontend/         ← React SPA (Mobile-first, Dark theme)
    └── src/
        ├── pages/          ← Dashboard, Vocabulary, Review, Exercise, Progress
        ├── components/     ← ui/ (Button, Card, Modal…) + layout/ (Sidebar, Header)
        ├── hooks/          ← TanStack Query hooks per domain
        ├── services/       ← Axios service layer
        ├── types/          ← TypeScript interfaces matching backend DTOs
        └── contexts/       ← QueryProvider, ToastProvider
```

## Prerequisites

- Java 17+
- Maven 3.9+
- Node.js 20+ / npm 10+
- Oracle Database XE 21c (running locally on port 1521, service name `XEPDB1`)

### Oracle setup (first time)

```sql
-- Connect as SYSTEM or SYS
CREATE USER englishmemory IDENTIFIED BY senha123;
GRANT CONNECT, RESOURCE TO englishmemory;
GRANT UNLIMITED TABLESPACE TO englishmemory;
```

Update `backend/src/main/resources/application-dev.yml` with your credentials.

## Getting Started

### 1. Backend

```bash
cd backend

# Run with dev profile (Oracle local, DataInitializer)
# Requires OPENAI_API_KEY — there's no mock AI provider anymore, the app
# won't start without it (fail-fast, see "AI Provider" below)
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Liquibase runs automatically on startup and creates all tables (V001–V007).

The `DataInitializer` creates a default user:
- Email: `dev@englishmemory.ai`
- CEFR: B1

API available at: `http://localhost:8080/api`
Swagger UI: `http://localhost:8080/api/swagger-ui.html`

### 2. Frontend

```bash
cd frontend
npm install   # first time only
npm run dev
```

App available at: `http://localhost:5173`
The Vite dev proxy forwards `/api/*` → `http://localhost:8080/api/*` (no CORS issues).

### Production build

```bash
# Frontend
npm run build         # output: frontend/dist/

# Backend
mvn clean package -Dmaven.test.skip=true   # creates executable JAR
java -jar target/english-memory-*.jar
```

Real production deployment is Docker Compose based (backend + Oracle + Caddy + ngrok),
not a bare `java -jar`. See **`CLAUDE.md`** for the actual prod setup, env vars and
known infra gotchas — it's not derivable from the code alone.

## API Endpoints

| Resource        | Base path            | Operations                    |
|-----------------|----------------------|-------------------------------|
| Vocabulary      | `/api/vocabulary`    | CRUD, due-today, weak words   |
| Categories      | `/api/categories`    | CRUD                          |
| Review (SM-2)   | `/api/review`        | due cards, submit answer      |
| Exercises       | `/api/exercises`     | generate, answer              |
| Sentences       | `/api/sentences`     | analyze                       |
| Dashboard       | `/api/dashboard`     | stats + weekly chart          |
| Progress        | `/api/progress`      | per-word progress             |
| Health          | `/api/actuator/health` | health check                |

Full interactive docs: **http://localhost:8080/api/swagger-ui.html**

## Spaced Repetition (SM-2)

The SM-2 algorithm drives review scheduling. After each review the user rates recall quality 0–5:

| Quality | Meaning         | Effect                          |
|---------|-----------------|---------------------------------|
| 0       | Total blackout  | Reset: reps=0, next review=today |
| 1–2     | Incorrect       | Reset: reps=0, next review=today |
| 3       | Correct (hard)  | EF adjusted down, interval ×EF  |
| 4       | Correct (good)  | Normal progression               |
| 5       | Perfect recall  | EF adjusted up, longer interval  |

Intervals: first review → 1 day → 6 days → `round(prev × EF)`.
Ease Factor minimum: 1.3 (never drops below).

## AI Provider

The AI layer is decoupled via the `AiExerciseService` / `DictionaryProvider` interfaces,
but **OpenAI is the only implementation right now** — there used to be a `mock` provider
for offline/no-cost development, but it was removed after it accidentally shipped active
in production (users were getting fake `"(mock) ..."` responses). Without a valid
`OPENAI_API_KEY` the app **won't start** — this is intentional fail-fast behavior, not a bug.

```yaml
# application.yml
app:
  ai:
    provider: ${AI_PROVIDER:openai}   # only "openai" has a matching @Service today
  dictionary:
    provider: ${DICTIONARY_PROVIDER:openai}
```

To add a new provider, implement `AiExerciseService` (or `DictionaryProvider`) and annotate with:
```java
@Service
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "claude")  // e.g.
```

## Running Tests

```bash
cd backend

# Unit tests only
mvn test

# With coverage report (target/site/jacoco/index.html)
mvn verify

# Checkstyle report
mvn checkstyle:check
```

Coverage targets (JaCoCo):
- Line coverage: ≥ 70%
- Branch coverage: ≥ 60%

## Database Migrations

Migrations are managed by Liquibase. Never use `ddl-auto: create` or `update`.

```
V001 — USERS
V002 — CATEGORIES
V003 — VOCABULARY_WORDS (CLOB fields for lists)
V004 — REVIEW_SCHEDULES (SM-2 fields)
V005 — EXERCISES + EXERCISE_ATTEMPTS
V006 — SENTENCE_PRACTICES
V007 — STUDY_SESSIONS + PROGRESS + deferred FK constraints
```

To add a migration: create `V008__description.xml` in `src/main/resources/db/changelog/`
and add the include to `db.changelog-master.xml`.

## Quality Tools

### Checkstyle

Rules defined in `backend/checkstyle.xml`. Key constraints:
- Max line length: 120 chars
- No star imports
- Braces required on all blocks
- Naming conventions enforced

```bash
mvn checkstyle:check   # fail on violation
mvn checkstyle:checkstyle  # generate HTML report
```

### JaCoCo

Coverage gate configured in `pom.xml`. Report generated at `target/site/jacoco/index.html`.

```bash
mvn verify   # runs tests + generates coverage report
```

## Project Structure — Backend Tests

```
src/test/java/com/englishmemory/
├── AbstractIntegrationTest.java     ← @SpringBootTest base (requires Oracle)
├── util/
│   └── JsonListConverterTest.java   ← Pure unit tests
└── service/
    ├── Sm2AlgorithmTest.java        ← 12 algorithm unit tests (SM-2)
    ├── CategoryServiceTest.java     ← Service mock tests
    └── ReviewServiceTest.java       ← Streak logic tests
```

Unit tests use `@ExtendWith(MockitoExtension.class)` — no database required.
Integration tests extend `AbstractIntegrationTest` — require Oracle connection.

## Environment Variables

| Variable                  | Default (dev)                          | Description              |
|---------------------------|----------------------------------------|--------------------------|
| `SPRING_PROFILES_ACTIVE`  | `dev`                                  | Active Spring profile    |
| `DB_URL`                  | `jdbc:oracle:thin:@localhost:1521/XEPDB1` | JDBC URL              |
| `DB_USERNAME`             | `englishmemory`                        | Database user            |
| `DB_PASSWORD`             | —                                      | Database password        |
| `AI_PROVIDER`             | `openai`                               | Only `openai` has an implementation |
| `DICTIONARY_PROVIDER`     | `openai`                               | Only `openai` has an implementation |
| `OPENAI_API_KEY`          | —                                      | Required — app won't start without it |

## License

MIT
