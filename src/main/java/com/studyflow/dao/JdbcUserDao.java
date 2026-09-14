package com.studyflow.dao;

import com.studyflow.config.ConnectionProvider;
import com.studyflow.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * JDBC implementation of {@link UserDao}, following the same conventions as
 * {@link JdbcTaskDao}.
 *
 * <p>{@code created_at} is left to the database default on insert and read back
 * on load, so the application never has to invent a creation timestamp.
 */
public class JdbcUserDao implements UserDao {

    private static final String SQL_INSERT = """
            INSERT INTO users (name, email, password_hash)
            VALUES (?, ?, ?)
            """;

    private static final String SQL_FIND_BY_ID = """
            SELECT id, name, email, password_hash, created_at
            FROM users
            WHERE id = ?
            """;

    private static final String SQL_FIND_BY_EMAIL = """
            SELECT id, name, email, password_hash, created_at
            FROM users
            WHERE email = ?
            """;

    private static final String SQL_FIND_ALL = """
            SELECT id, name, email, password_hash, created_at
            FROM users
            ORDER BY name
            """;

    private final ConnectionProvider connections;

    public JdbcUserDao(ConnectionProvider connections) {
        this.connections = Objects.requireNonNull(connections, "connections must not be null");
    }

    @Override
    public User create(User user) {
        Objects.requireNonNull(user, "user must not be null");

        try (Connection conn = connections.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPasswordHash());

            if (stmt.executeUpdate() == 0) {
                throw new DataAccessException("Creating user failed: no rows affected");
            }

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new DataAccessException("Creating user failed: no id returned");
                }
                user.setId(keys.getInt(1));
            }

            return user;

        } catch (SQLException e) {
            throw new DataAccessException("Could not create user: " + user.getEmail(), e);
        }
    }

    @Override
    public Optional<User> findById(int id) {
        try (Connection conn = connections.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_ID)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }

        } catch (SQLException e) {
            throw new DataAccessException("Could not fetch user with id " + id, e);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        Objects.requireNonNull(email, "email must not be null");

        try (Connection conn = connections.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_EMAIL)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }

        } catch (SQLException e) {
            throw new DataAccessException("Could not fetch user with email " + email, e);
        }
    }

    @Override
    public List<User> findAll() {
        try (Connection conn = connections.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = stmt.executeQuery()) {

            List<User> users = new ArrayList<>();
            while (rs.next()) {
                users.add(mapRow(rs));
            }
            return users;

        } catch (SQLException e) {
            throw new DataAccessException("Could not fetch users", e);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        Timestamp createdAt = rs.getTimestamp("created_at");
        return new User(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("password_hash"),
                createdAt == null ? null : createdAt.toLocalDateTime()
        );
    }
}
