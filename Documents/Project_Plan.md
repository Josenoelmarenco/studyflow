# Project Plan — StudyFlow

> Ohjelmistotuotantoprojekti 1 (TX00EY27-3013) · Metropolia UAS · Autumn 2026 · Product Owner: Amir Dirin (lecturer)

## 1. Project summary

StudyFlow is a personalized study-planner desktop application. It lets a student manage courses, assignments, exams, sub-tasks and reminders, and shows a dashboard of what is due, overdue and how far along the semester is. This repository is an independent, personal implementation that follows the same course requirements end to end, built to understand the full stack (data, logic, UI, testing, CI/CD).

## 2. Methodology

The project follows **Agile / Scrum**, delivered in **four two-week sprints**. The lecturer acts as **Product Owner**; the **Scrum Master** role rotates per sprint. Each sprint ends with a mandatory **Sprint Review** over Zoom where every member presents their individual contribution. The course's emphasis is the **process** (Agile, CI/CD, testing, containerization), not merely the finished product.

| Sprint | Weeks | Dates | Focus |
|--------|-------|-------|-------|
| 1 | 1–2 | 18.08–31.08 | Project foundation (vision, backlog, repo, design) |
| 2 | 3–4 | 01.09–14.09 | Database + initial UI + testing + coverage |
| 3 | 5–6 | 15.09–28.09 | Feature extension + Jenkins CI/CD + Docker image |
| 4 | 7–8 | 29.09–12.10 | Containerization + final GUI + final presentation (demo 06.10) |

## 3. Scope

**In scope:** course/assignment/exam CRUD, sub-tasks, reminders, deadline & progress tracking, dashboard and schedule views, MariaDB persistence, unit + integration testing with enforced coverage, Jenkins pipeline, Docker image, UML/ER documentation.

**Out of scope (for this course iteration):** multi-user accounts and real authentication (a single local user is used), cloud deployment, mobile clients, push/email notification delivery (reminders are surfaced in-app).

## 4. Technology stack and rationale

| Layer | Technology | Why |
|-------|-----------|-----|
| Language | Java 21 | Course requirement; modern language features |
| UI | JavaFX 21 | Native desktop UI for a single-user planner |
| Database | MariaDB | Relational model fits the structured, related data |
| Build | Maven | Reproducible build lifecycle and dependency management |
| Testing | JUnit 5 + Mockito | Unit tests and isolation of the service layer |
| Test DB | H2 (MariaDB mode) | DAO integration tests with no server to install |
| Coverage | JaCoCo | Coverage measurement + enforced 70% gate |
| CI/CD | Jenkins | Automated build/test/coverage on every commit |
| Containerization | Docker | Reproducible build/test environment and runtime image |
| Version control | Git + GitHub | Source control and collaboration |
| Design | Figma | UI prototype |

## 5. Architecture

A strict layered architecture (`ui → service → dao → model`, with `config` supplying connections) and dependency injection so every layer is testable. Full detail and diagrams are in [`Diagrams/`](./Diagrams) and the root `README.md`.

## 6. Team and roles

| Member | Role |
|--------|------|
| Indrek Lind | Developer |
| Armin Khorami | Developer |
| José Noel Marenco | UI/UX Design |
| Maria | Developer, Scrum Master *(left the group during the course)* |

## 7. Collaboration & tools

- **Communication:** Discord.
- **Backlog / board:** Trello (team), mirrored in [`Backlog.md`](./Backlog.md).
- **Repository:** GitHub, Agile methodology, per-sprint commits.
- **Hour logging:** each member logs hours per task, reported in the sprint reports.

## 8. Risks and mitigations

| Risk | Mitigation |
|------|------------|
| A member leaving mid-course (Maria left) | Redistribute tasks; keep documentation current so anyone can pick up work |
| Environment drift ("works on my machine") | H2-based tests + Docker image so build/test run identically anywhere |
| Missed in-class assignments (zero project points) | Track in-class status in every sprint report |
| Scope creep on the UI | Prioritize graded technical deliverables first; keep advanced UI optional |

## 9. Definition of Done

A backlog item is *done* when: the code is written and reviewed, unit/integration tests cover it, the coverage gate still passes, it is committed to GitHub, and (for features) it is reachable from the UI.
