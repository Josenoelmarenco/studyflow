package com.studyflow.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("User")
class UserTest {

    @Nested
    @DisplayName("construction")
    class Construction {

        @Test
        @DisplayName("stores every field it is given")
        void storesAllFields() {
            LocalDateTime created = LocalDateTime.of(2026, 9, 1, 8, 0);
            User user = new User(7, "José Noel", "jose@studyflow.local", "hash", created);

            assertAll(
                    () -> assertEquals(7, user.getId()),
                    () -> assertEquals("José Noel", user.getName()),
                    () -> assertEquals("jose@studyflow.local", user.getEmail()),
                    () -> assertEquals("hash", user.getPasswordHash()),
                    () -> assertEquals(created, user.getCreatedAt())
            );
        }

        @Test
        @DisplayName("leaves a new user without an id or timestamp until saved")
        void newUserHasNoId() {
            User user = new User("José Noel", "jose@studyflow.local", "hash");

            assertAll(
                    () -> assertEquals(0, user.getId()),
                    () -> assertEquals(null, user.getCreatedAt())
            );
        }

        @Test
        @DisplayName("trims surrounding whitespace from name and email")
        void trimsFields() {
            User user = new User("  José  ", "  jose@studyflow.local  ", "hash");

            assertAll(
                    () -> assertEquals("José", user.getName()),
                    () -> assertEquals("jose@studyflow.local", user.getEmail())
            );
        }
    }

    @Nested
    @DisplayName("validation")
    class Validation {

        @Test
        @DisplayName("rejects a blank name")
        void rejectsBlankName() {
            assertThrows(IllegalArgumentException.class,
                    () -> new User("  ", "jose@studyflow.local", "hash"));
        }

        @Test
        @DisplayName("rejects a blank password hash")
        void rejectsBlankPasswordHash() {
            assertThrows(IllegalArgumentException.class,
                    () -> new User("José", "jose@studyflow.local", "  "));
        }

        @ParameterizedTest
        @ValueSource(strings = {"no-at-sign", "@leading.local", "trailing@", "two@@ats.local", "  "})
        @DisplayName("rejects malformed email addresses")
        void rejectsBadEmail(String email) {
            assertThrows(IllegalArgumentException.class,
                    () -> new User("José", email, "hash"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"a@b", "jose@studyflow.local", "j.noel+tag@sub.domain.fi"})
        @DisplayName("accepts reasonable email addresses")
        void acceptsGoodEmail(String email) {
            User user = new User("José", email, "hash");
            assertEquals(email, user.getEmail());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringMasksSecret {

        @Test
        @DisplayName("never reveals the password hash")
        void hidesPasswordHash() {
            User user = new User("José", "jose@studyflow.local", "super-secret-hash");

            assertAll(
                    () -> assertFalse(user.toString().contains("super-secret-hash")),
                    () -> assertTrue(user.toString().contains("***"))
            );
        }
    }
}
