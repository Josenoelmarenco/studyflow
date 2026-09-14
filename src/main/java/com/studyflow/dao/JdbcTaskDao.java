package com.studyflow.dao;

import com.studyflow.config.ConnectionProvider;
import com.studyflow.model.Task;
import com.studyflow.model.TaskStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * JDBC implementation of {@link TaskDao}.
 *
 * <p>The {@link ConnectionProvider} arrives through the constructor instead of
 * being looked up statically. That is the whole reason this class can be tested
 * against an in-memory H2 database — and therefore the reason it can reach real
 * coverage instead of 0%.
 *
 * <p>Every query uses {@code PreparedStatement} (parameterised, so SQL
 * injection is impossible) inside try-with-resources (so connections are always
 * released, even when an exception is thrown).
 */
public class JdbcTaskDao implements TaskDao {

    private static final String SQL_INSERT = """
            INSERT INTO tasks (course_id, title, description, deadline, status)
            VALUES (?, ?, ?, ?, ?)
            """;

    private static final String SQL_FIND_BY_ID = """
            SELECT id, course_id, title, description, deadline, status
            FROM tasks
            WHERE id = ?
            """;

    private static final String SQL_FIND_ALL = """
            SELECT id, course_id, title, description, deadline, status
            FROM tasks
            ORDER BY deadline IS NULL, deadline
            """;

    private static final String SQL_FIND_BY_COURSE = """
            SELECT id, course_id, title, description, deadline, status
            FROM tasks
            WHERE course_id = ?
            ORDER BY deadline IS NULL, deadline
            """;

    private static final String SQL_FIND_BY_STATUS = """
            SELECT id, course_id, title, description, deadline, status
            FROM tasks
            WHERE status = ?
            ORDER BY deadline IS NULL, deadline
            """;

    private static final String SQL_UPDATE = """
            UPDATE tasks
            SET course_id = ?, title = ?, description = ?, deadline = ?, status = ?
            WHERE id = ?
            """;

    private static final String SQL_DELETE = "DELETE FROM tasks WHERE id = ?";

    private final ConnectionProvider connections;

    public JdbcTaskDao(ConnectionProvider connections) {
        this.connections = Objects.requireNonNull(connections, "connections must not be null");
    }

    @Override
    public Task create(Task task) {
        Objects.requireNonNull(task, "task must not be null");

        try (Connection conn = connections.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            bindTaskFields(stmt, task);

            if (stmt.executeUpdate() == 0) {
                throw new DataAccessException("Creating task failed: no rows affected");
            }

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new DataAccessException("Creating task failed: no id returned");
                }
                task.setId(keys.getInt(1));
            }

            return task;

        } catch (SQLException e) {
            throw new DataAccessException("Could not create task: " + task.getTitle(), e);
        }
    }

    @Override
    public Optional<Task> findById(int id) {
        try (Connection conn = connections.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_ID)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }

        } catch (SQLException e) {
            throw new DataAccessException("Could not fetch task with id " + id, e);
        }
    }

    @Override
    public List<Task> findAll() {
        try (Connection conn = connections.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = stmt.executeQuery()) {

            return mapRows(rs);

        } catch (SQLException e) {
            throw new DataAccessException("Could not fetch tasks", e);
        }
    }

    @Override
    public List<Task> findByCourseId(int courseId) {
        try (Connection conn = connections.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_COURSE)) {

            stmt.setInt(1, courseId);

            try (ResultSet rs = stmt.executeQuery()) {
                return mapRows(rs);
            }

        } catch (SQLException e) {
            throw new DataAccessException("Could not fetch tasks for course " + courseId, e);
        }
    }

    @Override
    public List<Task> findByStatus(TaskStatus status) {
        Objects.requireNonNull(status, "status must not be null");

        try (Connection conn = connections.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_STATUS)) {

            stmt.setString(1, status.name());

            try (ResultSet rs = stmt.executeQuery()) {
                return mapRows(rs);
            }

        } catch (SQLException e) {
            throw new DataAccessException("Could not fetch tasks with status " + status, e);
        }
    }

    @Override
    public boolean update(Task task) {
        Objects.requireNonNull(task, "task must not be null");

        try (Connection conn = connections.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {

            bindTaskFields(stmt, task);
            stmt.setInt(6, task.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException("Could not update task with id " + task.getId(), e);
        }
    }

    @Override
    public boolean deleteById(int id) {
        try (Connection conn = connections.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_DELETE)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException("Could not delete task with id " + id, e);
        }
    }

    /** Binds parameters 1–5, shared by INSERT and UPDATE. */
    private void bindTaskFields(PreparedStatement stmt, Task task) throws SQLException {
        stmt.setInt(1, task.getCourseId());
        stmt.setString(2, task.getTitle());
        stmt.setString(3, task.getDescription());
        stmt.setTimestamp(4, toTimestamp(task.getDeadline()));
        stmt.setString(5, task.getStatus().name());
    }

    private List<Task> mapRows(ResultSet rs) throws SQLException {
        List<Task> tasks = new ArrayList<>();
        while (rs.next()) {
            tasks.add(mapRow(rs));
        }
        return tasks;
    }

    /** Translates one row into a domain object, converting JDBC types on the way out. */
    private Task mapRow(ResultSet rs) throws SQLException {
        return new Task(
                rs.getInt("id"),
                rs.getInt("course_id"),
                rs.getString("title"),
                rs.getString("description"),
                toLocalDateTime(rs.getTimestamp("deadline")),
                TaskStatus.fromDatabase(rs.getString("status"))
        );
    }

    private static Timestamp toTimestamp(LocalDateTime value) {
        return value == null ? null : Timestamp.valueOf(value);
    }

    private static LocalDateTime toLocalDateTime(Timestamp value) {
        return value == null ? null : value.toLocalDateTime();
    }
}
