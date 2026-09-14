package com.studyflow.dao;

import com.studyflow.config.ConnectionProvider;
import com.studyflow.model.Reminder;

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
 * JDBC implementation of {@link ReminderDao}, following the same conventions as
 * {@link JdbcTaskDao}. As in the task DAO, {@code java.sql.Timestamp} is
 * confined to this class and never reaches the domain model.
 */
public class JdbcReminderDao implements ReminderDao {

    private static final String SQL_INSERT = """
            INSERT INTO reminders (task_id, remind_at, is_sent)
            VALUES (?, ?, ?)
            """;

    private static final String SQL_FIND_BY_ID = """
            SELECT id, task_id, remind_at, is_sent
            FROM reminders
            WHERE id = ?
            """;

    private static final String SQL_FIND_BY_TASK = """
            SELECT id, task_id, remind_at, is_sent
            FROM reminders
            WHERE task_id = ?
            ORDER BY remind_at
            """;

    private static final String SQL_FIND_DUE = """
            SELECT id, task_id, remind_at, is_sent
            FROM reminders
            WHERE is_sent = FALSE AND remind_at <= ?
            ORDER BY remind_at
            """;

    private static final String SQL_UPDATE = """
            UPDATE reminders
            SET task_id = ?, remind_at = ?, is_sent = ?
            WHERE id = ?
            """;

    private static final String SQL_DELETE = "DELETE FROM reminders WHERE id = ?";

    private final ConnectionProvider connections;

    public JdbcReminderDao(ConnectionProvider connections) {
        this.connections = Objects.requireNonNull(connections, "connections must not be null");
    }

    @Override
    public Reminder create(Reminder reminder) {
        Objects.requireNonNull(reminder, "reminder must not be null");

        try (Connection conn = connections.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            bindReminderFields(stmt, reminder);

            if (stmt.executeUpdate() == 0) {
                throw new DataAccessException("Creating reminder failed: no rows affected");
            }

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new DataAccessException("Creating reminder failed: no id returned");
                }
                reminder.setId(keys.getInt(1));
            }

            return reminder;

        } catch (SQLException e) {
            throw new DataAccessException("Could not create reminder for task " + reminder.getTaskId(), e);
        }
    }

    @Override
    public Optional<Reminder> findById(int id) {
        try (Connection conn = connections.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_ID)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }

        } catch (SQLException e) {
            throw new DataAccessException("Could not fetch reminder with id " + id, e);
        }
    }

    @Override
    public List<Reminder> findByTaskId(int taskId) {
        try (Connection conn = connections.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_TASK)) {

            stmt.setInt(1, taskId);

            try (ResultSet rs = stmt.executeQuery()) {
                return mapRows(rs);
            }

        } catch (SQLException e) {
            throw new DataAccessException("Could not fetch reminders for task " + taskId, e);
        }
    }

    @Override
    public List<Reminder> findDue(LocalDateTime now) {
        Objects.requireNonNull(now, "now must not be null");

        try (Connection conn = connections.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_DUE)) {

            stmt.setTimestamp(1, Timestamp.valueOf(now));

            try (ResultSet rs = stmt.executeQuery()) {
                return mapRows(rs);
            }

        } catch (SQLException e) {
            throw new DataAccessException("Could not fetch due reminders", e);
        }
    }

    @Override
    public boolean update(Reminder reminder) {
        Objects.requireNonNull(reminder, "reminder must not be null");

        try (Connection conn = connections.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {

            bindReminderFields(stmt, reminder);
            stmt.setInt(4, reminder.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException("Could not update reminder with id " + reminder.getId(), e);
        }
    }

    @Override
    public boolean deleteById(int id) {
        try (Connection conn = connections.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_DELETE)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException("Could not delete reminder with id " + id, e);
        }
    }

    /** Binds parameters 1–3, shared by INSERT and UPDATE. */
    private void bindReminderFields(PreparedStatement stmt, Reminder reminder) throws SQLException {
        stmt.setInt(1, reminder.getTaskId());
        stmt.setTimestamp(2, Timestamp.valueOf(reminder.getRemindAt()));
        stmt.setBoolean(3, reminder.isSent());
    }

    private List<Reminder> mapRows(ResultSet rs) throws SQLException {
        List<Reminder> reminders = new ArrayList<>();
        while (rs.next()) {
            reminders.add(mapRow(rs));
        }
        return reminders;
    }

    private Reminder mapRow(ResultSet rs) throws SQLException {
        return new Reminder(
                rs.getInt("id"),
                rs.getInt("task_id"),
                rs.getTimestamp("remind_at").toLocalDateTime(),
                rs.getBoolean("is_sent")
        );
    }
}
