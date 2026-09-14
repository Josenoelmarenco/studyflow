package com.studyflow.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Task")
class TaskTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 9, 14, 12, 0);

    private static Task taskWithDeadline(LocalDateTime deadline, TaskStatus status) {
        return new Task(1, "Write sprint report", "Sprint 2", deadline, status);
    }

    @Nested
    @DisplayName("construction")
    class Construction {

        @Test
        @DisplayName("stores every field it is given")
        void storesAllFields() {
            LocalDateTime deadline = NOW.plusDays(3);
            Task task = new Task(42, 7, "Jenkins pipeline", "Set up CI", deadline, TaskStatus.IN_PROGRESS);

            assertAll(
                    () -> assertEquals(42, task.getId()),
                    () -> assertEquals(7, task.getCourseId()),
                    () -> assertEquals("Jenkins pipeline", task.getTitle()),
                    () -> assertEquals("Set up CI", task.getDescription()),
                    () -> assertEquals(deadline, task.getDeadline()),
                    () -> assertEquals(TaskStatus.IN_PROGRESS, task.getStatus())
            );
        }

        @Test
        @DisplayName("leaves a new task without an id until it is saved")
        void newTaskHasNoId() {
            Task task = new Task(1, "Read chapter 4", null, null, TaskStatus.PENDING);

            assertEquals(0, task.getId());
        }

        @Test
        @DisplayName("trims surrounding whitespace from the title")
        void trimsTitle() {
            Task task = new Task(1, "  Docker image  ", null, null, TaskStatus.PENDING);

            assertEquals("Docker image", task.getTitle());
        }
    }

    @Nested
    @DisplayName("validation")
    class Validation {

        @Test
        @DisplayName("rejects a blank title")
        void rejectsBlankTitle() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Task(1, "   ", null, null, TaskStatus.PENDING));
        }

        @Test
        @DisplayName("rejects a null title")
        void rejectsNullTitle() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Task(1, null, null, null, TaskStatus.PENDING));
        }

        @Test
        @DisplayName("rejects a non-positive course id")
        void rejectsInvalidCourseId() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Task(0, "Valid title", null, null, TaskStatus.PENDING));
        }

        @Test
        @DisplayName("rejects a null status")
        void rejectsNullStatus() {
            assertThrows(NullPointerException.class,
                    () -> new Task(1, "Valid title", null, null, null));
        }
    }

    @Nested
    @DisplayName("isOverdue")
    class IsOverdue {

        @Test
        @DisplayName("is true when the deadline has passed and the task is unfinished")
        void overdueWhenDeadlinePassed() {
            Task task = taskWithDeadline(NOW.minusDays(1), TaskStatus.PENDING);

            assertTrue(task.isOverdue(NOW));
        }

        @Test
        @DisplayName("is false when the deadline is still ahead")
        void notOverdueWhenDeadlineAhead() {
            Task task = taskWithDeadline(NOW.plusDays(1), TaskStatus.PENDING);

            assertFalse(task.isOverdue(NOW));
        }

        @Test
        @DisplayName("is false for a finished task, even past its deadline")
        void doneTaskIsNeverOverdue() {
            Task task = taskWithDeadline(NOW.minusWeeks(2), TaskStatus.DONE);

            assertFalse(task.isOverdue(NOW));
        }

        @Test
        @DisplayName("is false when the task has no deadline")
        void noDeadlineIsNeverOverdue() {
            Task task = taskWithDeadline(null, TaskStatus.PENDING);

            assertFalse(task.isOverdue(NOW));
        }

        @Test
        @DisplayName("is false at the exact deadline moment")
        void notOverdueExactlyAtDeadline() {
            Task task = taskWithDeadline(NOW, TaskStatus.PENDING);

            assertFalse(task.isOverdue(NOW));
        }
    }

    @Nested
    @DisplayName("markDone")
    class MarkDone {

        @Test
        @DisplayName("moves the task to DONE")
        void setsStatusToDone() {
            Task task = taskWithDeadline(NOW, TaskStatus.IN_PROGRESS);

            task.markDone();

            assertEquals(TaskStatus.DONE, task.getStatus());
        }
    }

    @Nested
    @DisplayName("equality")
    class Equality {

        @Test
        @DisplayName("treats saved tasks with the same id as equal")
        void equalWhenSameId() {
            Task a = new Task(5, 1, "A", null, null, TaskStatus.PENDING);
            Task b = new Task(5, 2, "B", null, null, TaskStatus.DONE);

            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("treats two unsaved tasks as different")
        void unsavedTasksAreNotEqual() {
            Task a = new Task(1, "A", null, null, TaskStatus.PENDING);
            Task b = new Task(1, "A", null, null, TaskStatus.PENDING);

            assertNotEquals(a, b);
        }
    }
}
