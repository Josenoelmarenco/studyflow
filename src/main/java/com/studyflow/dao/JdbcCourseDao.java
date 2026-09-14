package com.studyflow.dao;

import com.studyflow.config.ConnectionProvider;
import com.studyflow.model.Course;

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
 * JDBC implementation of {@link CourseDao}.
 *
 * <p>Mirrors {@link JdbcTaskDao} exactly: the {@link ConnectionProvider} is
 * injected through the constructor, every statement is parameterised, and every
 * connection is opened inside try-with-resources so it is always released.
 */
public class JdbcCourseDao implements CourseDao {

    private static final String SQL_INSERT = """
            INSERT INTO courses (user_id, name, code, semester)
            VALUES (?, ?, ?, ?)
            """;

    private static final String SQL_FIND_BY_ID = """
            SELECT id, user_id, name, code, semester
            FROM courses
            WHERE id = ?
            """;

    private static final String SQL_FIND_ALL = """
            SELECT id, user_id, name, code, semester
            FROM courses
            ORDER BY name
            """;

    private static final String SQL_FIND_BY_USER = """
            SELECT id, user_id, name, code, semester
            FROM courses
            WHERE user_id = ?
            ORDER BY name
            """;

    private static final String SQL_UPDATE = """
            UPDATE courses
            SET user_id = ?, name = ?, code = ?, semester = ?
            WHERE id = ?
            """;

    private static final String SQL_DELETE = "DELETE FROM courses WHERE id = ?";

    private final ConnectionProvider connections;

    public JdbcCourseDao(ConnectionProvider connections) {
        this.connections = Objects.requireNonNull(connections, "connections must not be null");
    }

    @Override
    public Course create(Course course) {
        Objects.requireNonNull(course, "course must not be null");

        try (Connection conn = connections.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            bindCourseFields(stmt, course);

            if (stmt.executeUpdate() == 0) {
                throw new DataAccessException("Creating course failed: no rows affected");
            }

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new DataAccessException("Creating course failed: no id returned");
                }
                course.setId(keys.getInt(1));
            }

            return course;

        } catch (SQLException e) {
            throw new DataAccessException("Could not create course: " + course.getName(), e);
        }
    }

    @Override
    public Optional<Course> findById(int id) {
        try (Connection conn = connections.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_ID)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }

        } catch (SQLException e) {
            throw new DataAccessException("Could not fetch course with id " + id, e);
        }
    }

    @Override
    public List<Course> findAll() {
        try (Connection conn = connections.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = stmt.executeQuery()) {

            return mapRows(rs);

        } catch (SQLException e) {
            throw new DataAccessException("Could not fetch courses", e);
        }
    }

    @Override
    public List<Course> findByUserId(int userId) {
        try (Connection conn = connections.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_USER)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                return mapRows(rs);
            }

        } catch (SQLException e) {
            throw new DataAccessException("Could not fetch courses for user " + userId, e);
        }
    }

    @Override
    public boolean update(Course course) {
        Objects.requireNonNull(course, "course must not be null");

        try (Connection conn = connections.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {

            bindCourseFields(stmt, course);
            stmt.setInt(5, course.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException("Could not update course with id " + course.getId(), e);
        }
    }

    @Override
    public boolean deleteById(int id) {
        try (Connection conn = connections.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_DELETE)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException("Could not delete course with id " + id, e);
        }
    }

    /** Binds parameters 1–4, shared by INSERT and UPDATE. */
    private void bindCourseFields(PreparedStatement stmt, Course course) throws SQLException {
        stmt.setInt(1, course.getUserId());
        stmt.setString(2, course.getName());
        stmt.setString(3, course.getCode());
        stmt.setString(4, course.getSemester());
    }

    private List<Course> mapRows(ResultSet rs) throws SQLException {
        List<Course> courses = new ArrayList<>();
        while (rs.next()) {
            courses.add(mapRow(rs));
        }
        return courses;
    }

    private Course mapRow(ResultSet rs) throws SQLException {
        return new Course(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getString("name"),
                rs.getString("code"),
                rs.getString("semester")
        );
    }
}
