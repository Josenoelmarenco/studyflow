package com.studyflow.support;

import com.studyflow.config.ConnectionProvider;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

/**
 * A disposable H2 database for integration tests.
 *
 * <p>Runs H2 in MariaDB compatibility mode and loads the very same
 * {@code db/schema.sql} the application uses in production. Testing against the
 * real schema is the point: a hand-written test schema would happily drift away
 * from the real one and the tests would keep passing while production broke.
 *
 * <p>Each instance gets a unique database name, so tests never share state and
 * can run in parallel. {@code DB_CLOSE_DELAY=-1} keeps the in-memory database
 * alive between connections for the lifetime of the JVM.
 */
public final class InMemoryDatabase implements ConnectionProvider, AutoCloseable {

    private static final Path SCHEMA = Path.of("db", "schema.sql");

    private final String jdbcUrl;

    private InMemoryDatabase(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    /**
     * Creates a fresh, empty database with the production schema applied.
     */
    public static InMemoryDatabase create() {
        String name = "studyflow_test_" + UUID.randomUUID().toString().replace("-", "");
        String url = "jdbc:h2:mem:" + name + ";DB_CLOSE_DELAY=-1;MODE=MariaDB;DATABASE_TO_LOWER=TRUE";

        InMemoryDatabase database = new InMemoryDatabase(url);
        database.applySchema();
        return database;
    }

    private void applySchema() {
        String ddl;
        try {
            ddl = Files.readString(SCHEMA);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Could not read " + SCHEMA.toAbsolutePath()
                            + ". Tests must be run from the project root.", e);
        }

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(ddl);
        } catch (SQLException e) {
            throw new IllegalStateException("Could not apply schema to test database", e);
        }
    }

    /**
     * Inserts one user and returns its generated id. Useful for tests that need
     * a valid {@code user_id} to hang a course on.
     */
    public int seedUser() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("""
                    INSERT INTO users (name, email, password_hash)
                    VALUES ('Seed User %s', 'seed-%s@studyflow.local', 'hash')
                    """.formatted(UUID.randomUUID(), UUID.randomUUID()));

            return lastId(stmt, "users");

        } catch (SQLException e) {
            throw new IllegalStateException("Could not seed a user", e);
        }
    }

    /**
     * Inserts the minimum rows a task needs: one user and one course.
     *
     * @return the id of the created course
     */
    public int seedCourse() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("""
                    INSERT INTO users (name, email, password_hash)
                    VALUES ('Test Student', 'test@studyflow.local', 'hash')
                    """);

            stmt.executeUpdate("""
                    INSERT INTO courses (user_id, name, code, semester)
                    VALUES (1, 'Test Course', 'TX00TEST', 'Autumn 2026')
                    """);

            return lastId(stmt, "courses");

        } catch (SQLException e) {
            throw new IllegalStateException("Could not seed test data", e);
        }
    }

    /**
     * Inserts one task under the given course and returns its generated id.
     * Useful for tests of subtasks and reminders, which need a parent task.
     *
     * @param courseId an existing course id
     * @return the id of the created task
     */
    public int seedTask(int courseId) {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("""
                    INSERT INTO tasks (course_id, title, status)
                    VALUES (%d, 'Seed task', 'PENDING')
                    """.formatted(courseId));

            return lastId(stmt, "tasks");

        } catch (SQLException e) {
            throw new IllegalStateException("Could not seed a task", e);
        }
    }

    private int lastId(Statement stmt, String table) throws SQLException {
        try (var rs = stmt.executeQuery("SELECT MAX(id) FROM " + table)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, "sa", "");
    }

    /** Drops every table so the database can be garbage collected. */
    @Override
    public void close() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP ALL OBJECTS");
        } catch (SQLException e) {
            // Nothing useful to do while tearing down a test database.
        }
    }
}
