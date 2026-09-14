package com.studyflow.dao;

import com.studyflow.model.Subtask;

import java.util.List;
import java.util.Optional;

/**
 * Persistence operations for {@link Subtask}.
 */
public interface SubtaskDao {

    /**
     * Persists a new subtask and assigns it the generated id.
     *
     * @throws DataAccessException if the insert fails
     */
    Subtask create(Subtask subtask);

    /**
     * Finds a subtask by its id.
     *
     * @return the subtask, or {@link Optional#empty()} if no such row exists
     * @throws DataAccessException if the query fails
     */
    Optional<Subtask> findById(int id);

    /**
     * Returns the subtasks of one task, in insertion order.
     *
     * @throws DataAccessException if the query fails
     */
    List<Subtask> findByTaskId(int taskId);

    /**
     * Saves changes to an existing subtask (title or done flag).
     *
     * @return {@code true} if a row was updated, {@code false} if the id was not found
     * @throws DataAccessException if the update fails
     */
    boolean update(Subtask subtask);

    /**
     * Deletes a subtask by id.
     *
     * @return {@code true} if a row was deleted, {@code false} if the id was not found
     * @throws DataAccessException if the delete fails
     */
    boolean deleteById(int id);
}
