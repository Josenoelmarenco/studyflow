package com.studyflow.dao;

import com.studyflow.model.Task;
import com.studyflow.model.TaskStatus;

import java.util.List;
import java.util.Optional;

/**
 * Persistence operations for {@link Task}.
 *
 * <p>Declaring the contract as an interface lets the service layer be tested
 * with a mock, and lets the storage technology change without touching
 * anything above this line.
 *
 * <p>Implementations throw {@link DataAccessException} on failure rather than
 * returning {@code null} or {@code -1}: a silent sentinel value is easy to
 * ignore, while an exception forces the caller to decide what to do.
 */
public interface TaskDao {

    /**
     * Persists a new task and assigns it the generated id.
     *
     * @param task the task to store; its id is updated in place
     * @return the same instance, now carrying its database id
     * @throws DataAccessException if the insert fails
     */
    Task create(Task task);

    /**
     * Finds a task by its id.
     *
     * @param id the task id
     * @return the task, or {@link Optional#empty()} if no such row exists
     * @throws DataAccessException if the query fails
     */
    Optional<Task> findById(int id);

    /**
     * Returns every task, ordered by deadline (nulls last).
     *
     * @throws DataAccessException if the query fails
     */
    List<Task> findAll();

    /**
     * Returns the tasks belonging to one course, ordered by deadline.
     *
     * @throws DataAccessException if the query fails
     */
    List<Task> findByCourseId(int courseId);

    /**
     * Returns the tasks in a given state, ordered by deadline.
     *
     * @throws DataAccessException if the query fails
     */
    List<Task> findByStatus(TaskStatus status);

    /**
     * Saves changes to an existing task.
     *
     * @return {@code true} if a row was updated, {@code false} if the id was not found
     * @throws DataAccessException if the update fails
     */
    boolean update(Task task);

    /**
     * Deletes a task by id.
     *
     * @return {@code true} if a row was deleted, {@code false} if the id was not found
     * @throws DataAccessException if the delete fails
     */
    boolean deleteById(int id);
}
