# Sprint 3 Report — CI/CD, Feature Extension and Docker

> StudyFlow · Ohjelmistotuotantoprojekti 1 (TX00EY27-3013) · Metropolia UAS
> **Sprint 3** · Weeks 5–6 · 15.09–28.09.2026 · Review ~29.09 · Scrum Master: (current rotation)
>
> *Personal-reference edition. This sprint is upcoming; items already implemented ahead of schedule are marked ✅, the rest are planned. Fill "Time Spent" before submitting.*

## Sprint goal

Extend the functional prototype with more complex features, establish a Jenkins CI/CD pipeline, strengthen automated testing with coverage, and build a first local Docker image.

## Tasks and outcomes

| # | Task (from Sprint 3 requirements) | Points | Status | Evidence / plan |
|---|-----------------------------------|--------|--------|-----------------|
| 1 | Extend functional prototype (auth, data validation, search/filter, reporting dashboard) | 3 | 🟠 Partial | Data validation ✅ (model constructors); reporting/progress dashboard ✅; **search/filter** and **login/auth** planned |
| 2 | Integrate Jenkins CI/CD (checkout → build → JUnit → JaCoCo) | 5 | ✅ | [`Jenkinsfile`](../../Jenkinsfile) — pipeline stages defined; capture screenshots of a run |
| 3 | Automated unit & coverage testing (expanded) | — | ✅ | Test suite covers model, DAO and service; gate enforced |
| 4 | Build a local Docker image (created and tested) | 2 | ✅ | [`Dockerfile`](../../Dockerfile) — multi-stage build + run; run `docker build` and capture evidence |
| 5 | Sub-task UI (task breakdown) | — | 🟠 | `Subtask` model + `SubtaskService` + DAO done and tested; UI panel planned |

## Core functionality validation (planned/ongoing)

- End-to-end testing of the main user workflows (create course → add assignment → mark done).
- Basic performance sanity of the queries.
- Data validation and sanitization via parameterized statements (SQL injection is impossible by design).

## Team contribution table

| Team Member Name | Assigned Tasks | Time Spent (hrs) | In-class tasks |
|------------------|----------------|------------------|----------------|
| José Noel Marenco | Jenkinsfile, Dockerfile, UI extension, expanded tests | — *(fill in)* | In-class 3 (Jenkins) — *(confirm status)* |
| Indrek Lind | (team) | — *(fill in)* | *(fill in)* |
| Armin Khorami | (team) | — *(fill in)* | *(fill in)* |

## Submission summary

- **Individual (commits):** `Jenkinsfile`, `Dockerfile` and feature commits on GitHub.
- **Backlog update:** Trello updated with Sprint 3 status.
- **GitHub update:** pipeline and Docker configuration pushed; pipeline run screenshots attached at review.

## Retrospective (to complete at sprint end)

- **What went well:** _tbd_
- **To improve:** _tbd_
- **Next sprint:** finalize the Docker image, polish the GUI, prepare the final presentation.
