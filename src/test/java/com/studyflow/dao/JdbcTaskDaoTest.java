package com.studyflow.dao;

import com.studyflow.model.Task;
import com.studyflow.model.TaskStatus;
import com.studyflow.support.InMemoryDatabase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for {@link JdbcTaskDao}, running against an in-memory H2
 * database loaded with the real production schema.
 *
 * <p>No MariaDB server is required, which is what allows these tests to run on
 * a laptop and on a Jenkins agent alike.
 */
@DisplayName("JdbcTaskDao")
class JdbcTaskDaoTest {

    private static final LocalDateTime DEADLINE = LocalDateTime.of(2026, 9, 20, 23, 59);

    private InMemoryDatabase database;
    private JdbcTaskDao dao;
    private int courseId;

    @BeforeEach
    void setUp() {
        database = InMemoryDatabase.create();
        courseId = database.seedCourse();
        dao = new JdbcTaskDao(database);
    }

    @AfterEach
    void tearDown() {
        database.close();
    }

    private Task newTask(String title, LocalDateTime deadline, TaskStatus status) {
        return new Task(courseId, title, "description", deadline, status);
    }

    private Task persist(String title, LocalDateTime deadline, TaskStatus status) {
        return dao.create(newTask(title, deadline, status));
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("assigns a generated id")
        void assignsGeneratedId() {
            Task saved = persist("Sprint report", DEADLINE, TaskStatus.PENDING);

            assertTrue(saved.getId() > 0, "expected a generated id");
        }

        @Test
        @DisplayName("stores every field so it can be read back")
        void storesAllFields() {
            Task saved = persist("Sprint report", DEADLINE, TaskStatus.IN_PROGRESS);

            Task loaded = dao.findById(saved.getId()).orElseThrow();

            assertAll(
                    () -> assertEquals(courseId, loaded.getCourseId()),
                    () -> assertEquals("Sprint report", loaded.getTitle()),
                    () -> assertEquals("description", loaded.getDescription()),
                    () -> assertEquals(DEADLINE, loaded.getDeadline()),
                    () -> assertEquals(TaskStatus.IN_PROGRESS, loaded.getStatus())
            );
        }

        @Test
        @DisplayName("accepts a task without a deadline")
        void acceptsNullDeadline() {
            Task saved = persist("Someday", null, TaskStatus.PENDING);

            assertEquals(Optional.empty(),
                    dao.findById(saved.getId()).map(Task::getDeadline));
        }

        @Test
        @DisplayName("rejects a task pointing at a course that does not exist")
        void rejectsUnknownCourse() {
            Task orphan = new Task(9999, "Orphan", null, DEADLINE, TaskStatus.PENDING);

            assertThrows(DataAccessException.class, () -> dao.create(orphan));
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("returns empty when the id does not exist")
        void returnsEmptyForUnknownId() {
            assertEquals(Optional.empty(), dao.findById(12345));
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("returns an empty list when there are no tasks")
        void emptyWhenNoTasks() {
            assertTrue(dao.findAll().isEmpty());
        }

        @Test
        @DisplayName("returns every stored task")
        void returnsAllTasks() {
            persist("First", DEADLINE, TaskStatus.PENDING);
            persist("Second", DEADLINE.plusDays(1), TaskStatus.PENDING);

            assertEquals(2, dao.findAll().size());
        }

        @Test
        @DisplayName("orders by deadline, putting undated tasks last")
        void ordersByDeadlineNullsLast() {
            persist("No deadline", null, TaskStatus.PENDING);
            persist("Later", DEADLINE.plusDays(5), TaskStatus.PENDING);
            persist("Sooner", DEADLINE, TaskStatus.PENDING);

            List<String> titles = dao.findAll().stream().map(Task::getTitle).toList();

            assertEquals(List.of("Sooner", "Later", "No deadline"), titles);
        }
    }

    @Nested
    @DisplayName("findByCourseId and findByStatus")
    class Filters {

        @Test
        @DisplayName("returns only the tasks of the requested course")
        void filtersByCourse() {
            persist("Mine", DEADLINE, TaskStatus.PENDING);

            assertAll(
                    () -> assertEquals(1, dao.findByCourseId(courseId).size()),
                    () -> assertTrue(dao.findByCourseId(9999).isEmpty())
            );
        }

        @Test
        @DisplayName("returns only the tasks in the requested state")
        void filtersByStatus() {
            persist("Pending one", DEADLINE, TaskStatus.PENDING);
            persist("Pending two", DEADLINE, TaskStatus.PENDING);
            persist("Finished", DEADLINE, TaskStatus.DONE);

            assertAll(
                    () -> assertEquals(2, dao.findByStatus(TaskStatus.PENDING).size()),
                    () -> assertEquals(1, dao.findByStatus(TaskStatus.DONE).size()),
                    () -> assertTrue(dao.findByStatus(TaskStatus.IN_PROGRESS).isEmpty())
            );
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("persists the changed fields")
        void persistsChanges() {
            Task saved = persist("Original", DEADLINE, TaskStatus.PENDING);

            saved.setTitle("Renamed");
            saved.setStatus(TaskStatus.DONE);
            boolean updated = dao.update(saved);

            Task loaded = dao.findById(saved.getId()).orElseThrow();

            assertAll(
                    () -> assertTrue(updated),
                    () -> assertEquals("Renamed", loaded.getTitle()),
                    () -> assertEquals(TaskStatus.DONE, loaded.getStatus())
            );
        }

        @Test
        @DisplayName("reports false when the task does not exist")
        void falseForUnknownId() {
            Task ghost = new Task(9999, courseId, "Ghost", null, DEADLINE, TaskStatus.PENDING);

            assertFalse(dao.update(ghost));
        }
    }

    @Nested
    @DisplayName("deleteById")
    class Delete {

        @Test
        @DisplayName("removes the row")
        void removesTheRow() {
            Task saved = persist("Temporary", DEADLINE, TaskStatus.PENDING);

            boolean deleted = dao.deleteById(saved.getId());

            assertAll(
                    () -> assertTrue(deleted),
                    () -> assertEquals(Optional.empty(), dao.findById(saved.getId()))
            );
        }

        @Test
        @DisplayName("reports false when the task does not exist")
        void falseForUnknownId() {
            assertFalse(dao.deleteById(9999));
        }
    }

    @Nested
    @DisplayName("full CRUD cycle")
    class FullCycle {

        @Test
        @DisplayName("create, read, update and delete work end to end")
        void endToEnd() {
            Task created = persist("Lifecycle", DEADLINE, TaskStatus.PENDING);
            int id = created.getId();

            created.setStatus(TaskStatus.IN_PROGRESS);
            dao.update(created);
            assertEquals(TaskStatus.IN_PROGRESS, dao.findById(id).orElseThrow().getStatus());

            dao.deleteById(id);
            assertEquals(Optional.empty(), dao.findById(id));
        }
    }
}
