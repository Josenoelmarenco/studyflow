# Sprint 2 Report — Database, UI and Testing

> StudyFlow · Ohjelmistotuotantoprojekti 1 (TX00EY27-3013) · Metropolia UAS
> **Sprint 2** · Weeks 3–4 · 01.09–14.09.2026 · Review 15.09 · Scrum Master: (current rotation)
>
> *Personal-reference edition. Fill "Time Spent" with your real hours before submitting.*

## Sprint goal

Lay the technical foundation of the application: implement the database and CRUD, start the JavaFX user interface, and integrate the essential development tools — unit testing, Maven and code-coverage reporting.

## Tasks and outcomes

| # | Task (from Sprint 2 requirements) | Points | Status | Evidence |
|---|-----------------------------------|--------|--------|----------|
| 1 | Implement the database (schema, tables, relationships) + test CRUD | 2 | ✅ | [`db/schema.sql`](../../db/schema.sql); DAOs tested against H2 |
| 2 | Start the user interface (initial views, layout, interactivity) referencing Figma | 2 | ✅ | `src/main/java/com/studyflow/ui/` — dashboard, courses, assignments, schedule, reminders, progress |
| 3 | Integrate unit testing (JUnit), tests in the repo | 3 | ✅ | `src/test/java/com/studyflow/` — model, DAO (H2), service (Mockito) |
| 4 | Use Maven for build management | — | ✅ | [`pom.xml`](../../pom.xml) |
| 5 | Configure code coverage (JaCoCo) + export & publish the HTML report | 3 | ✅ generated / ⏳ publish | `target/site/jacoco/index.html` — publish to a public folder (GitHub Pages `/docs`) |

**Coverage result:** all coverage checks pass; the JaCoCo gate is enforced at **70% line coverage** on the model, DAO and service layers (`mvn verify` fails below that).

## Demonstration readiness (Sprint Review)

- Working database — schema and seed data shown via MariaDB / H2.
- UI progress — six functional screens with create/edit/delete dialogs.
- Unit tests in action — `mvn test` runs model, DAO and service suites.
- Maven setup — dependency management and build lifecycle.
- Public JaCoCo report — coverage metrics (publish before the review).

## Team contribution table

| Team Member Name | Assigned Tasks | Time Spent (hrs) | In-class tasks |
|------------------|----------------|------------------|----------------|
| José Noel Marenco | UI views + dialogs, model/DAO/service, JUnit tests, JaCoCo setup | — *(fill in)* | Submitted (In-class 1 JUnit, In-class 2 Code Coverage) |
| Indrek Lind | (team) | — *(fill in)* | *(fill in)* |
| Armin Khorami | (team) | — *(fill in)* | *(fill in)* |

## Submission summary

- **Individual (commits):** full layered implementation committed to GitHub (`main`).
- **Backlog update:** Trello updated with Sprint 2 task status.
- **GitHub update:** all Sprint 2 code and documentation pushed.

## Retrospective

- **What went well:** the H2 + dependency-injection design made the DAO layer fully testable; the coverage gate passes; the UI already exceeds an "initial" prototype.
- **To improve:** publish the coverage report earlier in the sprint, not right before the review.
- **Next sprint:** Jenkins CI/CD pipeline, extend features (search/filter, sub-task UI), and build the Docker image.
