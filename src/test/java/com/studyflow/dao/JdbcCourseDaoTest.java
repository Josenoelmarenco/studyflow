package com.studyflow.dao;

import com.studyflow.model.Course;
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
 * Integration tests for {@link JdbcCourseDao} against an in-memory H2 database
 * loaded with the production schema.
 */
@DisplayName("JdbcCourseDao")
class JdbcCourseDaoTest {

    private InMemoryDatabase database;
    private JdbcCourseDao dao;
    private int userId;

    @BeforeEach
    void setUp() {
        database = InMemoryDatabase.create();
        userId = database.seedUser();
        dao = new JdbcCourseDao(database);
    }

    @AfterEach
    void tearDown() {
        database.close();
    }

    private Course persist(String name, String code) {
        return dao.create(new Course(userId, name, code, "Autumn 2026"));
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("assigns a generated id")
        void assignsGeneratedId() {
            Course saved = persist("Software Project", "TX00EY27");

            assertTrue(saved.getId() > 0);
        }

        @Test
        @DisplayName("stores every field so it can be read back")
        void storesAllFields() {
            Course saved = persist("Software Project", "TX00EY27");

            Course loaded = dao.findById(saved.getId()).orElseThrow();

            assertAll(
                    () -> assertEquals(userId, loaded.getUserId()),
                    () -> assertEquals("Software Project", loaded.getName()),
                    () -> assertEquals("TX00EY27", loaded.getCode()),
                    () -> assertEquals("Autumn 2026", loaded.getSemester())
            );
        }

        @Test
        @DisplayName("rejects a course pointing at a user that does not exist")
        void rejectsUnknownUser() {
            Course orphan = new Course(9999, "Orphan", "X", "Autumn 2026");

            assertThrows(DataAccessException.class, () -> dao.create(orphan));
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("returns empty when the id does not exist")
        void returnsEmptyForUnknownId() {
            assertEquals(Optional.empty(), dao.findById(12345));
        }
    }

    @Nested
    @DisplayName("findAll and findByUserId")
    class Queries {

        @Test
        @DisplayName("returns an empty list when there are no courses")
        void emptyWhenNone() {
            assertTrue(dao.findAll().isEmpty());
        }

        @Test
        @DisplayName("orders courses alphabetically by name")
        void ordersByName() {
            persist("Zeta course", "Z");
            persist("Alpha course", "A");

            List<String> names = dao.findAll().stream().map(Course::getName).toList();

            assertEquals(List.of("Alpha course", "Zeta course"), names);
        }

        @Test
        @DisplayName("returns only the courses of the requested user")
        void filtersByUser() {
            int otherUser = database.seedUser();
            persist("Mine", "M");
            dao.create(new Course(otherUser, "Theirs", "T", "Autumn 2026"));

            assertAll(
                    () -> assertEquals(1, dao.findByUserId(userId).size()),
                    () -> assertEquals(1, dao.findByUserId(otherUser).size()),
                    () -> assertTrue(dao.findByUserId(9999).isEmpty())
            );
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("persists the changed fields")
        void persistsChanges() {
            Course saved = persist("Original", "O");

            saved.setName("Renamed");
            saved.setCode("R");
            boolean updated = dao.update(saved);

            Course loaded = dao.findById(saved.getId()).orElseThrow();

            assertAll(
                    () -> assertTrue(updated),
                    () -> assertEquals("Renamed", loaded.getName()),
                    () -> assertEquals("R", loaded.getCode())
            );
        }

        @Test
        @DisplayName("reports false when the course does not exist")
        void falseForUnknownId() {
            Course ghost = new Course(9999, userId, "Ghost", "G", "Autumn 2026");

            assertFalse(dao.update(ghost));
        }
    }

    @Nested
    @DisplayName("deleteById")
    class Delete {

        @Test
        @DisplayName("removes the row")
        void removesTheRow() {
            Course saved = persist("Temporary", "T");

            boolean deleted = dao.deleteById(saved.getId());

            assertAll(
                    () -> assertTrue(deleted),
                    () -> assertEquals(Optional.empty(), dao.findById(saved.getId()))
            );
        }

        @Test
        @DisplayName("reports false when the course does not exist")
        void falseForUnknownId() {
            assertFalse(dao.deleteById(9999));
        }
    }
}
