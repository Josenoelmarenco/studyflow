package com.studyflow.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Reminder")
class ReminderTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 9, 14, 12, 0);

    @Nested
    @DisplayName("construction")
    class Construction {

        @Test
        @DisplayName("creates a new reminder as not sent, with no id")
        void newReminderDefaults() {
            Reminder reminder = new Reminder(5, NOW);

            assertAll(
                    () -> assertEquals(0, reminder.getId()),
                    () -> assertEquals(5, reminder.getTaskId()),
                    () -> assertEquals(NOW, reminder.getRemindAt()),
                    () -> assertFalse(reminder.isSent())
            );
        }
    }

    @Nested
    @DisplayName("validation")
    class Validation {

        @Test
        @DisplayName("rejects a null remind-at time")
        void rejectsNullRemindAt() {
            assertThrows(NullPointerException.class, () -> new Reminder(1, null));
        }

        @Test
        @DisplayName("rejects a non-positive task id")
        void rejectsInvalidTaskId() {
            assertThrows(IllegalArgumentException.class, () -> new Reminder(0, NOW));
        }
    }

    @Nested
    @DisplayName("isDue")
    class IsDue {

        @Test
        @DisplayName("is true when the time has passed and it has not been sent")
        void dueWhenTimePassed() {
            Reminder reminder = new Reminder(1, NOW.minusMinutes(1));

            assertTrue(reminder.isDue(NOW));
        }

        @Test
        @DisplayName("is true at the exact reminder moment")
        void dueExactlyAtTime() {
            Reminder reminder = new Reminder(1, NOW);

            assertTrue(reminder.isDue(NOW));
        }

        @Test
        @DisplayName("is false when the time is still ahead")
        void notDueWhenAhead() {
            Reminder reminder = new Reminder(1, NOW.plusMinutes(1));

            assertFalse(reminder.isDue(NOW));
        }

        @Test
        @DisplayName("is false once it has been sent, even if overdue")
        void notDueOnceSent() {
            Reminder reminder = new Reminder(0, 1, NOW.minusDays(1), true);

            assertFalse(reminder.isDue(NOW));
        }
    }

    @Nested
    @DisplayName("markSent")
    class MarkSent {

        @Test
        @DisplayName("flags the reminder as sent")
        void marksSent() {
            Reminder reminder = new Reminder(1, NOW);

            reminder.markSent();

            assertTrue(reminder.isSent());
        }
    }
}
