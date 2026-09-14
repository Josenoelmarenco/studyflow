package com.studyflow.dao;

import com.studyflow.model.Course;

import java.util.List;
import java.util.Optional;

/**
 * Persistence operations for {@link Course}.
 *
 * <p>Same contract shape as {@link TaskDao}: an interface so the service layer
 * can be mocked, and {@link DataAccessException} on failure so errors are never
 * silently swallowed.
 */
public interface CourseDao {

    /**
     * Persists a new course and assigns it the generated id.
     *
     * @param course the course to store; its id is updated in place
     * @return the same instance, now carrying its database id
     * @throws DataAccessException if the insert fails
     */
    Course create(Course course);

    /**
     * Finds a course by its id.
     *
     * @return the course, or {@link Optional#empty()} if no such row exists
     * @throws DataAccessException if the query fails
     */
    Optional<Course> findById(int id);

    /**
     * Returns every course, ordered by name.
     *
     * @throws DataAccessException if the query fails
     */
    List<Course> findAll();

    /**
     * Returns the courses owned by one user, ordered by name.
     *
     * @throws DataAccessException if the query fails
     */
    List<Course> findByUserId(int userId);

    /**
     * Saves changes to an existing course.
     *
     * @return {@code true} if a row was updated, {@code false} if the id was not found
     * @throws DataAccessException if the update fails
     */
    boolean update(Course course);

    /**
     * Deletes a course by id. Its tasks are removed too, by the
     * {@code ON DELETE CASCADE} foreign key in the schema.
     *
     * @return {@code true} if a row was deleted, {@code false} if the id was not found
     * @throws DataAccessException if the delete fails
     */
    boolean deleteById(int id);
}
