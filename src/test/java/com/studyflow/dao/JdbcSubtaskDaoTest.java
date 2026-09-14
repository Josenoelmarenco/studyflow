package com.studyflow.dao;

import com.studyflow.model.Subtask;
import com.studyflow.support.InMemoryDatabase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for {@link JdbcSubtaskDao} against in-memory H2.
 */
@DisplayName("JdbcSubtaskDao")
class JdbcSubtaskDaoTest {

    private InMemoryDatabase database;
    private JdbcSubtaskDao dao;
    private int taskId;

    @BeforeEach
    void setUp() {
        database = InMemoryDatabase.create();
        int courseId = database.seedCourse();
        taskId = database.seedTask(courseId);
        dao = new JdbcSubtaskDao(database);
    }

    @AfterEach
    void tearDown() {
        database.close();
    }

    private Subtask persist(String title) {
        return dao.create(new Subtask(taskId, title));
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("assigns a generated id and stores fields")
        void assignsIdAndStores() {
            Subtask saved = persist("Collect team hours");

            Subtask loaded = dao.findById(saved.getId()).orElseThrow();

            assertAll(
                    () -> assertTrue(saved.getId() > 0),
                    () -> assertEquals(taskId, loaded.getTaskId()),
                    () -> assertEquals("Collect team hours", loaded.getTitle()),
                    () -> assertFalse(loaded.isDone())
            );
        }

        @Test
        @DisplayName("rejects a subtask pointing at a task that does not exist")
        void rejectsUnknownTask() {
            Subtask orphan = new Subtask(9999, "Orphan");

            assertThrows(DataAccessException.class, () -> dao.create(orphan));
        }
    }

    @Nested
    @DisplayName("findByTaskId")
    class FindByTask {

        @Test
        @DisplayName("returns the subtasks of the task in insertion order")
        void returnsInOrder() {
            persist("First");
            persist("Second");

            List<String> titles = dao.findByTaskId(taskId).stream()
                    .map(Subtask::getTitle).toList();

            assertEquals(List.of("First", "Second"), titles);
        }

        @Test
        @DisplayName("returns empty for a task with no subtasks")
        void emptyWhenNone() {
            assertTrue(dao.findByTaskId(taskId).isEmpty());
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("persists the done flag and the title")
        void persistsChanges() {
            Subtask saved = persist("Draft");

            saved.setTitle("Final");
            saved.setDone(true);
            boolean updated = dao.update(saved);

            Subtask loaded = dao.findById(saved.getId()).orElseThrow();

            assertAll(
                    () -> assertTrue(updated),
                    () -> assertEquals("Final", loaded.getTitle()),
                    () -> assertTrue(loaded.isDone())
            );
        }

        @Test
        @DisplayName("reports false when the subtask does not exist")
        void falseForUnknownId() {
            Subtask ghost = new Subtask(9999, taskId, "Ghost", false);

            assertFalse(dao.update(ghost));
        }
    }

    @Nested
    @DisplayName("deleteById")
    class Delete {

        @Test
        @DisplayName("removes the row")
        void removesTheRow() {
            Subtask saved = persist("Temporary");

            assertAll(
                    () -> assertTrue(dao.deleteById(saved.getId())),
                    () -> assertEquals(Optional.empty(), dao.findById(saved.getId()))
            );
        }

        @Test
        @DisplayName("reports false when the subtask does not exist")
        void falseForUnknownId() {
            assertFalse(dao.deleteById(9999));
        }
    }
}
