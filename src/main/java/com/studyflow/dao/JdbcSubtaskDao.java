package com.studyflow.dao;

import com.studyflow.config.ConnectionProvider;
import com.studyflow.model.Subtask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * JDBC implementation of {@link SubtaskDao}, following the same conventions as
 * {@link JdbcTaskDao}.
 */
public class JdbcSubtaskDao implements SubtaskDao {

    private static final String SQL_INSERT = """
            INSERT INTO subtasks (task_id, title, is_done)
            VALUES (?, ?, ?)
            """;

    private static final String SQL_FIND_BY_ID = """
            SELECT id, task_id, title, is_done
            FROM subtasks
            WHERE id = ?
            """;

    private static final String SQL_FIND_BY_TASK = """
            SELECT id, task_id, title, is_done
            FROM subtasks
            WHERE task_id = ?
            ORDER BY id
            """;

    private static final String SQL_UPDATE = """
            UPDATE subtasks
            SET task_id = ?, title = ?, is_done = ?
            WHERE id = ?
            """;

    private static final String SQL_DELETE = "DELETE FROM subtasks WHERE id = ?";

    private final ConnectionProvider connections;

    public JdbcSubtaskDao(ConnectionProvider connections) {
        this.connections = Objects.requireNonNull(connections, "connections must not be null");
    }

    @Override
    public Subtask create(Subtask subtask) {
        Objects.requireNonNull(subtask, "subtask must not be null");

        try (Connection conn = connections.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            bindSubtaskFields(stmt, subtask);

            if (stmt.executeUpdate() == 0) {
                throw new DataAccessException("Creating subtask failed: no rows affected");
            }

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new DataAccessException("Creating subtask failed: no id returned");
                }
                subtask.setId(keys.getInt(1));
            }

            return subtask;

        } catch (SQLException e) {
            throw new DataAccessException("Could not create subtask: " + subtask.getTitle(), e);
        }
    }

    @Override
    public Optional<Subtask> findById(int id) {
        try (Connection conn = connections.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_ID)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }

        } catch (SQLException e) {
            throw new DataAccessException("Could not fetch subtask with id " + id, e);
        }
    }

    @Override
    public List<Subtask> findByTaskId(int taskId) {
        try (Connection conn = connections.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_TASK)) {

            stmt.setInt(1, taskId);

            try (ResultSet rs = stmt.executeQuery()) {
                return mapRows(rs);
            }

        } catch (SQLException e) {
            throw new DataAccessException("Could not fetch subtasks for task " + taskId, e);
        }
    }

    @Override
    public boolean update(Subtask subtask) {
        Objects.requireNonNull(subtask, "subtask must not be null");

        try (Connection conn = connections.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {

            bindSubtaskFields(stmt, subtask);
            stmt.setInt(4, subtask.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException("Could not update subtask with id " + subtask.getId(), e);
        }
    }

    @Override
    public boolean deleteById(int id) {
        try (Connection conn = connections.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_DELETE)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException("Could not delete subtask with id " + id, e);
        }
    }

    /** Binds parameters 1–3, shared by INSERT and UPDATE. */
    private void bindSubtaskFields(PreparedStatement stmt, Subtask subtask) throws SQLException {
        stmt.setInt(1, subtask.getTaskId());
        stmt.setString(2, subtask.getTitle());
        stmt.setBoolean(3, subtask.isDone());
    }

    private List<Subtask> mapRows(ResultSet rs) throws SQLException {
        List<Subtask> subtasks = new ArrayList<>();
        while (rs.next()) {
            subtasks.add(mapRow(rs));
        }
        return subtasks;
    }

    private Subtask mapRow(ResultSet rs) throws SQLException {
        return new Subtask(
                rs.getInt("id"),
                rs.getInt("task_id"),
                rs.getString("title"),
                rs.getBoolean("is_done")
        );
    }
}
