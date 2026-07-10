# English Memory AI — Referência do Projeto

## Stack

| Camada | Tecnologias |
|---|---|
| Backend | Java 17, Spring Boot 3.2.5, Spring Data JPA, Hibernate 6, Liquibase, MapStruct, Lombok |
| Banco | Oracle XE — url: `jdbc:oracle:thin:@//localhost:1521/XE`, user: `system`, pass: `lecos` |
| Frontend | React 18, TypeScript, Vite 5, Tailwind CSS 3, TanStack Query v5, React Router v6 |
| Bibliotecas UI | Lucide React, clsx + tailwind-merge, Axios |

## Estrutura de Diretórios

```
AppIngles/
├── backend/src/main/java/com/englishmemory/
│   ├── controller/     — REST controllers (@RequestMapping sem /v1)
│   ├── service/        — interfaces + impl/
│   ├── entity/         — JPA entities (extends BaseEntity)
│   ├── repository/     — Spring Data JPA
│   ├── dto/            — request/ e response/
│   ├── mapper/         — MapStruct
│   ├── enums/          — CefrLevel, PartOfSpeech, ExerciseType, SessionType
│   ├── util/           — Sm2Algorithm, JsonListConverter
│   └── config/         — Security, CORS, DataInitializer
│
├── backend/src/main/resources/
│   ├── application.yml        — context-path: /api, porta 8080
│   ├── application-dev.yml    — Oracle credentials
│   └── db/changelog/          — Liquibase V001–V007
│
└── frontend/src/
    ├── pages/          — Dashboard, Vocabulary, Review, Exercise, Progress, Categories
    ├── components/     — layout/ (Sidebar, Header) + ui/ (Button, Card, Badge, Modal…)
    ├── hooks/          — useVocabulary, useCategories, useReview, useExercise, useProgress, useDashboard
    ├── services/       — api.ts (baseURL:/api, proxy Vite→8080) + *.service.ts
    ├── types/          — api.ts, vocabulary.ts, dashboard.ts, review.ts, exercise.ts
    └── utils/          — cn.ts (clsx+twMerge), format.ts
```

## Banco de Dados — Tabelas

| Tabela | Sequência | Notas |
|---|---|---|
| USERS | SEQ_USERS | streak_days, last_study_date |
| CATEGORIES | SEQ_CATEGORIES | UQ(user_id, name) |
| VOCABULARY_WORDS | SEQ_VOCABULARY_WORDS | UQ(user_id, word), CLOB: examples/synonyms/antonyms |
| REVIEW_SCHEDULES | SEQ_REVIEW_SCHEDULES | SM-2: ease_factor(BigDecimal), repetitions, interval_days |
| EXERCISES | SEQ_EXERCISES | tipos: MULTIPLE_CHOICE, FILL_BLANK, WORD_ORDER, TRANSLATION, TRUE_FALSE, SENTENCE_BUILDING |
| EXERCISE_ATTEMPTS | SEQ_EXERCISE_ATTEMPTS | FK p/ EXERCISES e STUDY_SESSIONS |
| SENTENCE_PRACTICES | SEQ_SENTENCE_PRACTICES | AI feedback, score 0-100 |
| STUDY_SESSIONS | SEQ_STUDY_SESSIONS | session_type: REVIEW/EXERCISE/CONVERSATION/READING/MIXED |
| PROGRESS | SEQ_PROGRESS | mastery_level 0-100, UQ(user_id, vocabulary_word_id) |

**Regra Oracle:** UNIQUE constraints criam índices implícitos — nunca criar `CREATE INDEX` na mesma coluna.

## Endpoints REST (base: /api)

| Método | Path | Descrição |
|---|---|---|
| GET | /categories | Listar categorias |
| POST | /categories | Criar categoria |
| PUT | /categories/{id} | Atualizar categoria |
| DELETE | /categories/{id} | Soft-delete categoria |
| GET | /vocabulary | Listar palavras (paginado) |
| POST | /vocabulary | Criar palavra |
| PUT | /vocabulary/{id} | Atualizar palavra |
| DELETE | /vocabulary/{id} | Soft-delete palavra |
| GET | /reviews/today | Palavras para revisar hoje (SM-2) |
| POST | /reviews/{wordId}/answer | Submeter resposta (quality 0-5) |
| GET | /dashboard | Stats consolidados do usuário |
| GET | /progress | Progresso por nível/categoria/palavra |
| POST | /exercises/generate | Gerar exercício via AI (mock) |
| POST | /exercises/{id}/answer | Responder exercício |
| POST | /sentences/analyze | Analisar frase com AI |
| GET | /actuator/health | Health check |

**Usuário dev padrão:** id=1, email=dev@englishmemory.ai (criado pelo DataInitializer)

## Padrões de Código

### Backend
- Entities estendem `BaseEntity` (id, createdAt, updatedAt, active)
- Soft-delete: `entity.setActive(false)` + `repository.save(entity)`
- MapStruct com `@BeanMapping(builder = @Builder(disableBuilder = true))` no `toEntity` para herança Lombok
- `easeFactor` é `BigDecimal` (não `Double`) — Hibernate 6 exige para `scale`
- SM-2: `Sm2Algorithm.calculate(easeFactor.doubleValue(), ...)` retorna `Sm2Result(double, int, int)`
- JsonListConverter para campos CLOB que armazenam listas JSON

### Frontend
- `cn()` de `@/utils/cn` para className condicional
- TanStack Query: `staleTime: 2min, retry: 1`
- Mutations: `queryClient.invalidateQueries` após sucesso
- Toast: `useToast()` do `ToastContext`
- Tema: dark, cores Tailwind `indigo-*`
- Mobile-first: Sidebar como overlay no mobile, hamburger no Header

## Funcionalidades por Página

| Página | Rota | Funcionalidades |
|---|---|---|
| Dashboard | `/` | 6 stat cards, gráfico de barras semanal, banner de alerta, ações rápidas |
| Vocabulário | `/vocabulary` | Search+debounce, grid paginado, CRUD modal completo, ConfirmDialog exclusão |
| Review | `/review` | FlashCard 3D flip (CSS preserve-3d), 4 botões de qualidade SM-2, sessão completa |
| Exercícios | `/exercise` | 6 tipos de exercício, geração via AI, análise de frases, resultado com score |
| Progresso | `/progress` | Barra de distribuição por nível, gráfico semanal horizontal, lista de palavras fracas |
| Categorias | `/categories` | ColorPicker (14 cores), IconPicker (23 ícones Lucide), CRUD com card colorido |

## Como Iniciar

```bash
# Backend (terminal 1)
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev -Dmaven.test.skip=true

# Frontend (terminal 2)
cd frontend
npm run dev
```

URLs: Frontend → http://localhost:5173 | Backend → http://localhost:8080/api | Swagger → http://localhost:8080/api/swagger-ui.html

---

## Agentes Disponíveis

| Agente | Arquivo | Quando usar |
|---|---|---|
| `dev-backend` | `.claude/agents/dev-backend.md` | Criar/corrigir endpoints, entities, migrations, services |
| `dev-frontend` | `.claude/agents/dev-frontend.md` | Criar/corrigir componentes React, pages, hooks, services TS |
| `qa` | `.claude/agents/qa.md` | Testar endpoints, validar comportamento, encontrar bugs |
| `task-manager` | `.claude/agents/task-manager.md` | Planejar features, decompor tarefas, coordenar agentes |
