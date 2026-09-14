# Product Backlog — StudyFlow

> Ohjelmistotuotantoprojekti 1 (TX00EY27-3013) · Metropolia UAS · Autumn 2026
>
> User stories follow the course format: **As a [user type], I want [functionality] so that [benefit].**
> Priority: 🔴 High · 🟡 Medium · 🟢 Low. Status reflects this personal reference implementation.
> Detailed acceptance criteria live in [`Acceptance_Criteria.md`](./Acceptance_Criteria.md).

## Epics

| # | Epic | Description |
|---|------|-------------|
| E1 | Course management | Create and maintain the courses tasks are grouped under |
| E2 | Assignment & exam management | Full lifecycle of tasks and exams |
| E3 | Task breakdown (sub-tasks) | Split large tasks into checklist items |
| E4 | Deadline & progress tracking | Know what is overdue, upcoming and how far along the semester is |
| E5 | Reminders | Scheduled nudges for upcoming deadlines |
| E6 | Schedule & dashboard | Consolidated views of the semester |
| E7 | Quality & delivery (non-functional) | Testing, coverage, CI/CD, containerization |

## User stories

| ID | Epic | User story | Priority | Sprint | Status |
|----|------|-----------|----------|--------|--------|
| US-1 | E1 | As a student, I want to add a course so that I can group my work by course. | 🔴 | 1–2 | ✅ Done |
| US-2 | E1 | As a student, I want to edit or delete a course so that my course list stays accurate. | 🟡 | 2 | ✅ Done |
| US-3 | E2 | As a student, I want to create an assignment or exam under a course so that I don't lose track of it. | 🔴 | 2 | ✅ Done |
| US-4 | E2 | As a student, I want to set a deadline on a task so that I know when it is due. | 🔴 | 2 | ✅ Done |
| US-5 | E2 | As a student, I want to edit or delete a task so that my list reflects reality. | 🟡 | 2 | ✅ Done |
| US-6 | E2 | As a student, I want to mark a task as done so that I can see my progress. | 🔴 | 2 | ✅ Done |
| US-7 | E3 | As a student, I want to break a task into sub-tasks so that a big assignment feels manageable. | 🟡 | 3 | 🟠 Data + service done; UI pending |
| US-8 | E4 | As a student, I want to see which tasks are overdue so that I can catch up. | 🔴 | 2 | ✅ Done |
| US-9 | E4 | As a student, I want to see what is due soon so that I can plan ahead. | 🔴 | 2 | ✅ Done |
| US-10 | E4 | As a student, I want to see my completion rate so that I know how far along I am. | 🟡 | 2–3 | ✅ Done |
| US-11 | E5 | As a student, I want to schedule a reminder for a task so that I don't forget it. | 🟡 | 3 | ✅ Done |
| US-12 | E5 | As a student, I want to see which reminders are due so that I can act on them. | 🟡 | 3 | ✅ Done |
| US-13 | E6 | As a student, I want a schedule view of all dated tasks so that I see everything in order. | 🟡 | 2–3 | ✅ Done |
| US-14 | E6 | As a student, I want a dashboard so that I get an overview of my semester at a glance. | 🔴 | 2 | ✅ Done |
| US-15 | E2 | As a student, I want to search and filter my assignments so that I find things quickly. | 🟢 | 3 | ⬜ Planned |
| US-16 | E1 | As a student, I want to log in so that my data is tied to my account. | 🟢 | 3 | 🟠 Single-user model in place; login flow planned |
| US-17 | E7 | As a developer, I want unit tests and a coverage gate so that the code stays reliable. | 🔴 | 2 | ✅ Done |
| US-18 | E7 | As a developer, I want a CI pipeline so that every commit is built and tested automatically. | 🔴 | 3 | ✅ Done (`Jenkinsfile`) |
| US-19 | E7 | As a developer, I want a Docker image so that the app runs the same everywhere. | 🟡 | 3–4 | ✅ Done (`Dockerfile`) |

## Notes on prioritization

The backlog is intentionally flexible and refined each sprint. High-priority stories (course + assignment CRUD, deadlines, dashboard, testing, CI) were pulled first because they form the core value and the course's graded technical deliverables. Search/filter and a full login flow are lower priority for a single-user desktop planner and are staged for later refinement.
