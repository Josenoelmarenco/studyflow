package com.studyflow.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Course")
class CourseTest {

    @Nested
    @DisplayName("construction")
    class Construction {

        @Test
        @DisplayName("stores every field it is given")
        void storesAllFields() {
            Course course = new Course(10, 3, "Software Project", "TX00EY27", "Autumn 2026");

            assertAll(
                    () -> assertEquals(10, course.getId()),
                    () -> assertEquals(3, course.getUserId()),
                    () -> assertEquals("Software Project", course.getName()),
                    () -> assertEquals("TX00EY27", course.getCode()),
                    () -> assertEquals("Autumn 2026", course.getSemester())
            );
        }

        @Test
        @DisplayName("leaves a new course without an id until it is saved")
        void newCourseHasNoId() {
            Course course = new Course(1, "Design Patterns", "TX00EY29", "Autumn 2026");

            assertEquals(0, course.getId());
        }

        @Test
        @DisplayName("trims surrounding whitespace from the name")
        void trimsName() {
            Course course = new Course(1, "  WebAssembly  ", "TX00GN69", "Autumn 2026");

            assertEquals("WebAssembly", course.getName());
        }
    }

    @Nested
    @DisplayName("validation")
    class Validation {

        @Test
        @DisplayName("rejects a blank name")
        void rejectsBlankName() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Course(1, "   ", "TX00", "Autumn 2026"));
        }

        @Test
        @DisplayName("rejects a null name")
        void rejectsNullName() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Course(1, null, "TX00", "Autumn 2026"));
        }

        @Test
        @DisplayName("rejects a non-positive user id")
        void rejectsInvalidUserId() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Course(0, "Valid name", "TX00", "Autumn 2026"));
        }
    }

    @Nested
    @DisplayName("displayName")
    class DisplayName_ {

        @Test
        @DisplayName("combines code and name when a code is present")
        void withCode() {
            Course course = new Course(1, "Software Project", "TX00EY27", "Autumn 2026");

            assertEquals("TX00EY27 — Software Project", course.displayName());
        }

        @Test
        @DisplayName("falls back to the name alone when the code is blank")
        void withoutCode() {
            Course course = new Course(1, "Software Project", "  ", "Autumn 2026");

            assertEquals("Software Project", course.displayName());
        }

        @Test
        @DisplayName("falls back to the name alone when the code is null")
        void withNullCode() {
            Course course = new Course(1, "Software Project", null, "Autumn 2026");

            assertEquals("Software Project", course.displayName());
        }
    }
}
