package com.studyflow.service;

import com.studyflow.dao.ReminderDao;
import com.studyflow.model.Reminder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReminderService")
class ReminderServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 9, 14, 12, 0);

    @Mock
    private ReminderDao reminderDao;

    private ReminderService service;

    @BeforeEach
    void setUp() {
        service = new ReminderService(reminderDao);
    }

    @Nested
    @DisplayName("schedule")
    class Schedule {

        @Test
        @DisplayName("stores a reminder set for the future")
        void storesFutureReminder() {
            when(reminderDao.create(any(Reminder.class))).thenAnswer(call -> call.getArgument(0));

            service.schedule(1, NOW.plusHours(2), NOW);

            verify(reminderDao).create(any(Reminder.class));
        }

        @Test
        @DisplayName("refuses a reminder in the past and never touches the database")
        void rejectsPast() {
            assertThrows(IllegalArgumentException.class,
                    () -> service.schedule(1, NOW.minusMinutes(1), NOW));

            verify(reminderDao, never()).create(any(Reminder.class));
        }
    }

    @Nested
    @DisplayName("dueReminders")
    class DueReminders {

        @Test
        @DisplayName("delegates to the DAO's due query")
        void delegatesToDao() {
            when(reminderDao.findDue(NOW)).thenReturn(List.of(new Reminder(1, NOW.minusDays(1))));

            assertEquals(1, service.dueReminders(NOW).size());
        }
    }

    @Nested
    @DisplayName("markSent")
    class MarkSent {

        @Test
        @DisplayName("marks an existing reminder as sent and saves it")
        void marksExisting() {
            Reminder reminder = new Reminder(1, NOW);
            when(reminderDao.findById(1)).thenReturn(Optional.of(reminder));
            when(reminderDao.update(reminder)).thenReturn(true);

            boolean result = service.markSent(1);

            assertAll(
                    () -> assertTrue(result),
                    () -> assertTrue(reminder.isSent())
            );
        }

        @Test
        @DisplayName("reports false and saves nothing when the reminder is missing")
        void falseForMissing() {
            when(reminderDao.findById(99)).thenReturn(Optional.empty());

            assertFalse(service.markSent(99));
            verify(reminderDao, never()).update(any(Reminder.class));
        }
    }

    @Nested
    @DisplayName("delegation")
    class Delegation {

        @Test
        @DisplayName("delegates findByTask to the DAO")
        void delegatesFindByTask() {
            when(reminderDao.findByTaskId(7)).thenReturn(List.of());

            assertTrue(service.findByTask(7).isEmpty());
        }

        @Test
        @DisplayName("delegates delete to the DAO")
        void delegatesDelete() {
            when(reminderDao.deleteById(3)).thenReturn(true);

            assertTrue(service.deleteReminder(3));
        }
    }
}
