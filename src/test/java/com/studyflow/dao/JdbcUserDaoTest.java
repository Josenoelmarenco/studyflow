package com.studyflow.dao;

import com.studyflow.model.User;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for {@link JdbcUserDao} against in-memory H2.
 */
@DisplayName("JdbcUserDao")
class JdbcUserDaoTest {

    private InMemoryDatabase database;
    private JdbcUserDao dao;

    @BeforeEach
    void setUp() {
        database = InMemoryDatabase.create();
        dao = new JdbcUserDao(database);
    }

    @AfterEach
    void tearDown() {
        database.close();
    }

    private User persist(String name, String email) {
        return dao.create(new User(name, email, "hash"));
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("assigns a generated id and stores every field")
        void assignsIdAndStores() {
            User saved = persist("José Noel", "jose@studyflow.local");

            User loaded = dao.findById(saved.getId()).orElseThrow();

            assertAll(
                    () -> assertTrue(saved.getId() > 0),
                    () -> assertEquals("José Noel", loaded.getName()),
                    () -> assertEquals("jose@studyflow.local", loaded.getEmail()),
                    () -> assertEquals("hash", loaded.getPasswordHash())
            );
        }

        @Test
        @DisplayName("rejects a duplicate email (unique constraint)")
        void rejectsDuplicateEmail() {
            persist("First", "dup@studyflow.local");

            assertThrows(DataAccessException.class,
                    () -> persist("Second", "dup@studyflow.local"));
        }
    }

    @Nested
    @DisplayName("queries")
    class Queries {

        @Test
        @DisplayName("finds a user by email")
        void findsByEmail() {
            persist("José", "jose@studyflow.local");

            assertTrue(dao.findByEmail("jose@studyflow.local").isPresent());
        }

        @Test
        @DisplayName("returns empty for an unknown email")
        void emptyForUnknownEmail() {
            assertEquals(Optional.empty(), dao.findByEmail("nobody@studyflow.local"));
        }

        @Test
        @DisplayName("returns empty for an unknown id")
        void emptyForUnknownId() {
            assertEquals(Optional.empty(), dao.findById(12345));
        }

        @Test
        @DisplayName("lists users ordered by name")
        void listsOrderedByName() {
            persist("Zoe", "zoe@studyflow.local");
            persist("Ada", "ada@studyflow.local");

            List<String> names = dao.findAll().stream().map(User::getName).toList();

            assertEquals(List.of("Ada", "Zoe"), names);
        }
    }
}
