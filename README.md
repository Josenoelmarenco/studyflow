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
  │  ui/          JavaFX views              │  ← asks the service, renders answers
  ├─────────────────────────────────────────┤
  │  service/     Business rules            │  ← "what is overdue?", "what's upcoming?"
  ├─────────────────────────────────────────┤
  │  dao/         Persistence (interface)   │  ← CRUD contract
  │               JdbcTaskDao               │     JDBC implementation
  ├─────────────────────────────────────────┤
  │  model/       Domain objects            │  ← Task, Course, TaskStatus
  ├─────────────────────────────────────────┤
  │  config/      ConnectionProvider        │  ← MariaDB in prod, H2 in tests
  └─────────────────────────────────────────┘
```

### Why this structure

**Dependency injection is the load-bearing decision.** A DAO that calls a static `getConnection()` can only ever be tested against a real database, which in practice means it never gets tested at all. Here, `JdbcTaskDao` receives a `ConnectionProvider` through its constructor. In production that provider returns a MariaDB connection; in tests it returns an in-memory H2 database loaded with the *same* `db/schema.sql`. One interface, two environments, real coverage.

**`TaskStatus` is an enum, not a `String`.** `"Pendng"` becomes a compile error instead of a row that quietly never matches a filter.

**`LocalDateTime`, not `java.sql.Timestamp`.** JDBC types stay in the DAO. The domain model has no idea a database exists.

**Time is injected, never read from the clock.** `isOverdue(now)` takes the reference time as a parameter, so "what happens the day after a deadline" is a test, not a guess.

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

Edit the copy with your own password. `config.properties` is git-ignored, so it never leaves your machine.

Alternatively, set environment variables, which take precedence over the file:

```bash
export STUDYFLOW_DB_URL="jdbc:mariadb://localhost:3306/studyflow"
export STUDYFLOW_DB_USER="studyflow"
export STUDYFLOW_DB_PASSWORD="your-password"
```

### 3. Build and run

```bash
mvn clean install     # compile + run tests + coverage report
mvn javafx:run        # launch the application
```

---

## Testing

```bash
mvn test              # run all tests, generate coverage
mvn verify            # also enforce the 70% coverage gate
open target/site/jacoco/index.html
```

### Testing strategy

| Layer | Approach | Needs a database? |
|-------|----------|-------------------|
| `model/` | Plain unit tests | No |
| `service/` | Unit tests with a **Mockito** mock DAO | No |
| `dao/` | Integration tests against **H2** in MariaDB mode | No — in-memory |
| `ui/` | Excluded from the coverage gate | — |

Nothing in the suite requires MariaDB to be installed, which is what lets the exact same `mvn test` run on a laptop and on a Jenkins agent.

The coverage gate in `pom.xml` fails the build below **70% line coverage** on the model, service and DAO layers. A number nobody enforces is a number nobody improves.

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
| Packaging | Docker | Reproducible runtime environment |

---

## Project layout

```
.
├── README.md
├── pom.xml
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
│   │   │   ├── model/                  # Task, Course, TaskStatus
│   │   │   ├── dao/                    # TaskDao + JdbcTaskDao
│   │   │   ├── service/                # TaskService
│   │   │   └── ui/                     # DashboardView
│   │   └── resources/
│   │       ├── config.properties.example
│   │       └── com/studyflow/ui/styles.css
│   └── test/java/com/studyflow/
│       ├── model/                      # unit tests
│       ├── dao/                        # H2 integration tests
│       ├── service/                    # Mockito unit tests
│       └── support/InMemoryDatabase.java
└── Documents/
    └── Diagrams/                       # ER diagram, use case diagram
```

---

## Roadmap

- [x] Layered architecture with injected dependencies
- [x] Task model with validation and business rules
- [x] Full CRUD DAO, tested against H2
- [x] Service layer with dashboard queries
- [x] JavaFX dashboard shell
- [x] JaCoCo coverage gate at 70%
- [ ] Course and Reminder DAOs
- [ ] Create / edit / delete task dialogs
- [ ] Schedule and Progress screens
- [ ] Jenkins pipeline (`Jenkinsfile`)
- [ ] Docker image (`Dockerfile`)

---

## License

Coursework project, Metropolia University of Applied Sciences, Autumn 2026.
