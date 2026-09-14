# StudyFlow — Acceptance Criteria

This document lists the acceptance criteria for StudyFlow as user stories in
Given / When / Then form. Each criterion maps to behaviour that is either
enforced by an automated test (model, DAO or service layer) or exercised through
the JavaFX user interface.

The single actor is the **Student** — StudyFlow is a single-user desktop planner.

---

## 1. Manage courses

**US-1** — *As a student, I want to add my courses so I can group my work by course.*

- **Given** the Courses page, **when** I add a course with a name, **then** it appears in my course list.
- **Given** a course, **when** I edit its name, code or semester, **then** the change is saved and shown.
- **Given** a course, **when** I delete it, **then** it and all of its assignments are removed (cascading delete).
- **Given** the new-course form, **when** I leave the name blank, **then** the form refuses to save and tells me why.

## 2. Manage assignments

**US-2** — *As a student, I want to create, edit and delete assignments so my task list stays current.*

- **Given** at least one course, **when** I add an assignment with a title and course, **then** it appears in the assignments table.
- **Given** an assignment, **when** I edit any field, **then** the change is persisted.
- **Given** an assignment, **when** I delete it, **then** it is removed after I confirm.
- **Given** the assignment form, **when** I omit the title or the course, **then** the form refuses to save.
- **Given** a deadline field, **when** I enter a time in an invalid format, **then** the form refuses to save and shows the expected `HH:mm` format.

## 3. Track status and deadlines

**US-3** — *As a student, I want to see what is overdue and what is coming up so I can prioritise.*

- **Given** an unfinished task whose deadline has passed, **when** I view the dashboard, **then** it is counted as overdue.
- **Given** a task exactly at its deadline moment, **then** it is **not** considered overdue (the boundary is not past).
- **Given** a finished task past its deadline, **then** it is **not** overdue.
- **Given** a task with no deadline, **then** it is never overdue and never on the schedule.
- **Given** an assignment, **when** I mark it done, **then** its status becomes *Done* and it stops counting as pending or overdue.

## 4. Schedule reminders

**US-4** — *As a student, I want to schedule reminders for tasks so nothing slips through.*

- **Given** a task, **when** I schedule a reminder for a future moment, **then** it is saved and listed as *Scheduled*.
- **Given** a reminder whose time has arrived and that has not been sent, **then** it is reported as *Due*.
- **Given** the reminder form, **when** I choose a moment in the past, **then** scheduling is refused with a clear message.
- **Given** a due reminder, **when** I mark it sent, **then** it stops being reported as due.

## 5. Dashboard overview

**US-5** — *As a student, I want a single overview of my semester.*

- **Given** the dashboard, **then** I see counters for total, pending, in-progress, overdue and percentage completed.
- **Given** the dashboard, **then** I see a table combining overdue tasks and tasks due within the next two weeks.
- **Given** an empty planner, **then** the completion percentage is 0% (never an error).

## 6. Follow progress

**US-6** — *As a student, I want to see how far along I am, overall and per course.*

- **Given** the progress page, **then** I see an overall completion bar.
- **Given** each course, **then** I see a bar and an "x / y done" count.
- **Given** a course with no tasks, **then** its progress reads 0% rather than an undefined value.

## 7. Persistence and quality (non-functional)

**US-7** — *As a developer, I want the app to be reliable and testable.*

- **Given** any data operation, **then** it uses a parameterised statement (no SQL injection) and releases its connection.
- **Given** the DAO layer, **then** it is tested against an in-memory H2 database loaded with the production `db/schema.sql`.
- **Given** the service layer, **then** it is unit-tested with a mocked DAO and needs no database.
- **Given** the build, **when** line coverage of the model, DAO and service layers falls below **70%**, **then** `mvn verify` fails.
- **Given** every commit, **then** the Jenkins pipeline builds, tests, checks coverage and packages the application.
