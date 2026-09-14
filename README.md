# StudyFlow

A personalized study planner desktop application built with **Java 21 + JavaFX + MariaDB**.

StudyFlow helps students keep courses, assignments, exams, deadlines and reminders in one place, with a dashboard that shows what is due, what is overdue and how far along the semester is.

> **About this repository**
> This is my personal implementation, built to understand the full stack end to end — data layer, business logic, UI, testing and CI/CD — rather than only the slice I own in the team project. It follows the same requirements as the coursework but is developed independently, with every layer written and tested by me.

---

## Architecture

The application is layered, and each layer only knows about the one directly below it:

```
  ┌─────────────────────────────────────────┐
  │  ui/          JavaFX views & dialogs    │  ← asks the service, renders answers
  ├─────────────────────────────────────────┤
  │  service/     Business rules            │  ← "what is overdue?", "what's upcoming?"
  ├─────────────────────────────────────────┤
  │  dao/         Persistence (interfaces)  │  ← CRUD contracts
  │               Jdbc*Dao                  │     JDBC implementations
  ├─────────────────────────────────────────┤
  │  model/       Domain objects            │  ← User, Course, Task, Subtask, Reminder
  ├─────────────────────────────────────────┤
  │  config/      ConnectionProvider        │  ← MariaDB in prod, H2 in tests
  └─────────────────────────────────────────┘
```

See [`Documents/Diagrams/`](./Documents/Diagrams) for the architecture, class, ER, use-case and sequence diagrams.

### Why this structure

**Dependency injection is the load-bearing decision.** A DAO that calls a static `getConnection()` can only ever be tested against a real database, which in practice means it never gets tested at all. Here, every `Jdbc*Dao` receives a `ConnectionProvider` through its constructor. In production that provider returns a MariaDB connection; in tests it returns an in-memory H2 database loaded with the *same* `db/schema.sql`. One interface, two environments, real coverage.

**`TaskStatus` is an enum, not a `String`.** `"Pendng"` becomes a compile error instead of a row that quietly never matches a filter.

**`LocalDateTime`, not `java.sql.Timestamp`.** JDBC types stay in the DAO. The domain model has no idea a database exists.

**Time is injected, never read from the clock.** `isOverdue(now)` and `isDue(now)` take the reference time as a parameter, so "what happens the day after a deadline" is a test, not a guess.

---

## Domain model

| Entity | Belongs to | Notable rules |
|--------|-----------|---------------|
| **User** | — | Single local user; email is validated and unique; password hash never logged. |
| **Course** | User | Name is required; `displayName()` combines code + name for the UI. |
| **Task** | Course | Title required; `isOverdue(now)`; status is a `TaskStatus` enum. |
| **Subtask** | Task | Checklist item; `toggle()`; feeds the completion rate. |
| **Reminder** | Task | `remindAt` required; `isDue(now)` when pending and past-due. |

The relationships and columns are shown in the ER diagram; the schema itself lives in [`db/schema.sql`](./db/schema.sql).

---

## Features

- **Dashboard** — counters (total, pending, in-progress, overdue, % complete) and a combined overdue + upcoming table.
- **Courses** — full create / edit / delete.
- **Assignments** — full create / edit / delete, plus one-click *mark done*.
- **Schedule** — every dated task in chronological order.
- **Reminders** — schedule nudges, see which are due, mark them sent.
- **Progress** — overall and per-course completion bars.

---

## Getting started

### Prerequisites

- JDK 21 or newer
- Maven 3.9+
- MariaDB 10.6+ running locally

### 1. Set up the database

```bash
mysql -u root -p < db/init-local.sql
mysql -u root -p studyflow < db/schema.sql
mysql -u root -p studyflow < db/seed.sql   # optional sample data
```

### 2. Configure credentials

```bash
cp src/main/resources/config.properties.example src/main/resources/config.properties
```

Edit the copy with your own password. `config.properties` is git-ignored, so it never leaves your machine. Environment variables (`STUDYFLOW_DB_URL`, `STUDYFLOW_DB_USER`, `STUDYFLOW_DB_PASSWORD`) take precedence over the file.

### 3. Build and run

```bash
mvn clean verify      # compile + tests + coverage gate
mvn javafx:run        # launch the application
```

On first launch StudyFlow creates a single local user automatically, so you can go straight to adding courses and assignments.

---

## Testing

```bash
mvn test              # run all tests, generate the coverage report
mvn verify            # also enforce the 70% coverage gate
open target/site/jacoco/index.html
```

### Testing strategy

| Layer | Approach | Needs a database? |
|-------|----------|-------------------|
| `model/` | Plain unit tests (validation, `isOverdue`, `isDue`, `toggle`, …) | No |
| `service/` | Unit tests with a **Mockito** mock DAO | No |
| `dao/` | Integration tests against **H2** in MariaDB mode, real schema | No — in-memory |
| `ui/` | Excluded from the coverage gate | — |

Nothing in the suite requires MariaDB to be installed, which is what lets the exact same `mvn test` run on a laptop and on a Jenkins agent. The coverage gate in `pom.xml` fails the build below **70% line coverage** on the model, service and DAO layers.

---

## Diagrams

Rendered images (with their Mermaid sources) are in [`Documents/Diagrams/`](./Documents/Diagrams):

| Diagram | File |
|---------|------|
| Architecture / packages | `architecture-diagram.png` |
| Class diagram | `class-diagram.png` |
| ER diagram | `er-diagram.png` |
| Use-case diagram | `use-case-diagram.png` |
| Sequence — create assignment | `sequence-create-assignment.png` |

---

## Project documentation

All course process artifacts live under [`Documents/`](./Documents):

| Document | File |
|----------|------|
| Product vision (1 page) | [`Product_Vision.md`](./Documents/Product_Vision.md) |
| Product backlog (user stories) | [`Backlog.md`](./Documents/Backlog.md) |
| Project plan | [`Project_Plan.md`](./Documents/Project_Plan.md) |
| Acceptance criteria | [`Acceptance_Criteria.md`](./Documents/Acceptance_Criteria.md) |
| Sprint reports | [`sprint-reports/`](./Documents/sprint-reports) (Sprint 1–4) |
| Diagrams | [`Diagrams/`](./Documents/Diagrams) |

---

## CI/CD & Docker

**Jenkins** — the [`Jenkinsfile`](./Jenkinsfile) defines a declarative pipeline that runs on every commit:

```
Checkout → Build → Test (JUnit) → Coverage gate (JaCoCo) → Package
```

Test results and the coverage report are published as build artifacts. Because the tests use in-memory H2, the pipeline needs no database.

**Docker** — the [`Dockerfile`](./Dockerfile) is a multi-stage build that compiles, runs the full suite and enforces the coverage gate inside the image, then produces a runtime image:

```bash
docker build -t studyflow .

# Run against a MariaDB, forwarding the display (Linux):
docker run --rm \
  -e DISPLAY=$DISPLAY -v /tmp/.X11-unix:/tmp/.X11-unix \
  -e STUDYFLOW_DB_URL=jdbc:mariadb://host.docker.internal:3306/studyflow \
  -e STUDYFLOW_DB_USER=studyflow \
  -e STUDYFLOW_DB_PASSWORD=your-password \
  studyflow
```

---

## Project layout

```
.
├── README.md
├── pom.xml
├── Dockerfile
├── Jenkinsfile
├── .gitignore
├── db/
│   ├── schema.sql              # portable DDL — used by MariaDB and by H2 tests
│   ├── init-local.sql          # one-time local database bootstrap
│   └── seed.sql                # sample data
├── src/
│   ├── main/
│   │   ├── java/com/studyflow/
│   │   │   ├── Main.java               # JavaFX entry point, wires the layers
│   │   │   ├── config/                 # ConnectionProvider, DatabaseConfig
│   │   │   ├── model/                  # User, Course, Task, Subtask, Reminder, TaskStatus
│   │   │   ├── dao/                    # *Dao interfaces + Jdbc*Dao implementations
│   │   │   ├── service/                # User/Course/Task/Subtask/Reminder services
│   │   │   └── ui/                     # MainView shell + views + dialogs
│   │   └── resources/
│   │       ├── config.properties.example
│   │       └── com/studyflow/ui/styles.css
│   └── test/java/com/studyflow/
│       ├── model/                      # unit tests
│       ├── dao/                        # H2 integration tests
│       ├── service/                    # Mockito unit tests
│       └── support/InMemoryDatabase.java
└── Documents/
    ├── Acceptance_Criteria.md
    └── Diagrams/                       # architecture, class, ER, use-case, sequence
```

---

## Tech stack

| Layer | Technology | Why |
|-------|-----------|-----|
| Language | Java 21 | Course requirement; modern language features (records, text blocks, pattern matching) |
| UI | JavaFX 21 | Native desktop UI for a single-user planner |
| Database | MariaDB | Relational model fits structured, related data |
| Build | Maven | Dependency management and a reproducible build lifecycle |
| Testing | JUnit 5 | Nested tests, parameterised tests, clear failure messages |
| Mocking | Mockito | Isolates the service layer from persistence |
| Test DB | H2 (MariaDB mode) | DAO tests with no server to install |
| Coverage | JaCoCo | Coverage measurement plus an enforced quality gate |
| CI/CD | Jenkins | Automated build, test and coverage on every commit |
| Packaging | Docker | Reproducible build/test environment and runtime image |

---

## Roadmap

- [x] Layered architecture with injected dependencies
- [x] Domain model: User, Course, Task, Subtask, Reminder
- [x] Full CRUD DAOs for every entity, tested against H2
- [x] Service layer with dashboard, schedule, reminder and progress logic
- [x] Functional JavaFX UI: dashboard, courses, assignments, schedule, reminders, progress
- [x] Create / edit / delete dialogs with input validation
- [x] JaCoCo coverage gate at 70%
- [x] Jenkins pipeline (`Jenkinsfile`)
- [x] Docker image (`Dockerfile`)
- [x] Diagrams and acceptance criteria
- [ ] Subtask checklist panel in the UI (service + DAO already implemented and tested)
- [ ] Background scheduler that surfaces due reminders as desktop notifications

---

## License

Coursework project, Metropolia University of Applied Sciences, Autumn 2026.
