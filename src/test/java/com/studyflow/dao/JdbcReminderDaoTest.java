package com.studyflow.dao;

import com.studyflow.model.Reminder;
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
 * Integration tests for {@link JdbcReminderDao} against in-memory H2.
 */
@DisplayName("JdbcReminderDao")
class JdbcReminderDaoTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 9, 14, 12, 0);

    private InMemoryDatabase database;
    private JdbcReminderDao dao;
    private int taskId;

    @BeforeEach
    void setUp() {
        database = InMemoryDatabase.create();
        int courseId = database.seedCourse();
        taskId = database.seedTask(courseId);
        dao = new JdbcReminderDao(database);
    }

    @AfterEach
    void tearDown() {
        database.close();
    }

    private Reminder persist(LocalDateTime remindAt, boolean sent) {
        return dao.create(new Reminder(0, taskId, remindAt, sent));
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("assigns a generated id and round-trips the timestamp")
        void assignsIdAndStores() {
            Reminder saved = persist(NOW, false);

            Reminder loaded = dao.findById(saved.getId()).orElseThrow();

            assertAll(
                    () -> assertTrue(saved.getId() > 0),
                    () -> assertEquals(taskId, loaded.getTaskId()),
                    () -> assertEquals(NOW, loaded.getRemindAt()),
                    () -> assertFalse(loaded.isSent())
            );
        }

        @Test
        @DisplayName("rejects a reminder pointing at a task that does not exist")
        void rejectsUnknownTask() {
            Reminder orphan = new Reminder(9999, NOW);

            assertThrows(DataAccessException.class, () -> dao.create(orphan));
        }
    }

    @Nested
    @DisplayName("findByTaskId")
    class FindByTask {

        @Test
        @DisplayName("returns the reminders of the task, earliest first")
        void ordersByTime() {
            persist(NOW.plusDays(2), false);
            persist(NOW.plusDays(1), false);

            List<LocalDateTime> times = dao.findByTaskId(taskId).stream()
                    .map(Reminder::getRemindAt).toList();

            assertEquals(List.of(NOW.plusDays(1), NOW.plusDays(2)), times);
        }
    }

    @Nested
    @DisplayName("findDue")
    class FindDue {

        @Test
        @DisplayName("returns only pending reminders whose time has come, earliest first")
        void returnsOnlyDue() {
            persist(NOW.minusDays(1), false);   // due
            persist(NOW.minusDays(3), false);   // due, earlier
            persist(NOW.plusDays(1), false);    // future, not due
            persist(NOW.minusDays(5), true);    // past but already sent

            List<Reminder> due = dao.findDue(NOW);

            assertAll(
                    () -> assertEquals(2, due.size()),
                    () -> assertEquals(NOW.minusDays(3), due.get(0).getRemindAt()),
                    () -> assertEquals(NOW.minusDays(1), due.get(1).getRemindAt())
            );
        }

        @Test
        @DisplayName("includes a reminder falling exactly on now")
        void includesBoundary() {
            persist(NOW, false);

            assertEquals(1, dao.findDue(NOW).size());
        }
    }

    @Nested
    @DisplayName("update and delete")
    class UpdateDelete {

        @Test
        @DisplayName("persists the sent flag")
        void marksSent() {
            Reminder saved = persist(NOW, false);

            saved.markSent();
            boolean updated = dao.update(saved);

            assertAll(
                    () -> assertTrue(updated),
                    () -> assertTrue(dao.findById(saved.getId()).orElseThrow().isSent())
            );
        }

        @Test
        @DisplayName("reports false when updating a reminder that does not exist")
        void updateFalseForUnknownId() {
            Reminder ghost = new Reminder(9999, taskId, NOW, false);

            assertFalse(dao.update(ghost));
        }

        @Test
        @DisplayName("removes the row")
        void removesTheRow() {
            Reminder saved = persist(NOW, false);

            assertAll(
                    () -> assertTrue(dao.deleteById(saved.getId())),
                    () -> assertEquals(Optional.empty(), dao.findById(saved.getId()))
            );
        }

        @Test
        @DisplayName("reports false when deleting a reminder that does not exist")
        void deleteFalseForUnknownId() {
            assertFalse(dao.deleteById(9999));
        }
    }
}
