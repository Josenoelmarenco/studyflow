package com.studyflow.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Subtask")
class SubtaskTest {

    @Nested
    @DisplayName("construction")
    class Construction {

        @Test
        @DisplayName("creates a new subtask as not done, with no id")
        void newSubtaskDefaults() {
            Subtask subtask = new Subtask(5, "Collect team hours");

            assertAll(
                    () -> assertEquals(0, subtask.getId()),
                    () -> assertEquals(5, subtask.getTaskId()),
                    () -> assertEquals("Collect team hours", subtask.getTitle()),
                    () -> assertFalse(subtask.isDone())
            );
        }

        @Test
        @DisplayName("trims surrounding whitespace from the title")
        void trimsTitle() {
            Subtask subtask = new Subtask(1, "  Write retrospective  ");

            assertEquals("Write retrospective", subtask.getTitle());
        }
    }

    @Nested
    @DisplayName("validation")
    class Validation {

        @Test
        @DisplayName("rejects a blank title")
        void rejectsBlankTitle() {
            assertThrows(IllegalArgumentException.class, () -> new Subtask(1, "   "));
        }

        @Test
        @DisplayName("rejects a null title")
        void rejectsNullTitle() {
            assertThrows(IllegalArgumentException.class, () -> new Subtask(1, null));
        }

        @Test
        @DisplayName("rejects a non-positive task id")
        void rejectsInvalidTaskId() {
            assertThrows(IllegalArgumentException.class, () -> new Subtask(0, "Valid title"));
        }
    }

    @Nested
    @DisplayName("toggle")
    class Toggle {

        @Test
        @DisplayName("flips not-done to done and back")
        void flipsBothWays() {
            Subtask subtask = new Subtask(1, "Task");

            subtask.toggle();
            assertTrue(subtask.isDone());

            subtask.toggle();
            assertFalse(subtask.isDone());
        }
    }
}
