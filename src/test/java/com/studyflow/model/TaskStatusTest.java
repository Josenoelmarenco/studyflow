package com.studyflow.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("TaskStatus")
class TaskStatusTest {

    @ParameterizedTest(name = "\"{0}\" -> {1}")
    @CsvSource({
            "PENDING,     PENDING",
            "IN_PROGRESS, IN_PROGRESS",
            "DONE,        DONE",
            "pending,     PENDING",
            "In Progress, IN_PROGRESS",
            "'  done  ',  DONE"
    })
    @DisplayName("parses stored values regardless of casing or spacing")
    void parsesStoredValues(String stored, TaskStatus expected) {
        assertEquals(expected, TaskStatus.fromDatabase(stored));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("falls back to PENDING for null or blank values")
    void defaultsToPending(String stored) {
        assertEquals(TaskStatus.PENDING, TaskStatus.fromDatabase(stored));
    }

    @Test
    @DisplayName("rejects an unrecognised value instead of guessing")
    void rejectsUnknownValue() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> TaskStatus.fromDatabase("ARCHIVED"));

        assertEquals("Unknown task status: 'ARCHIVED'", error.getMessage());
    }

    @ParameterizedTest
    @EnumSource(TaskStatus.class)
    @DisplayName("every status round-trips through the database format")
    void roundTripsThroughDatabaseFormat(TaskStatus status) {
        assertEquals(status, TaskStatus.fromDatabase(status.name()));
    }

    @ParameterizedTest
    @EnumSource(TaskStatus.class)
    @DisplayName("every status exposes a non-blank label for the UI")
    void everyStatusHasLabel(TaskStatus status) {
        assertFalse(status.label().isBlank());
    }
}
