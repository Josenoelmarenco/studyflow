package com.studyflow.dao;

import com.studyflow.model.Reminder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Persistence operations for {@link Reminder}.
 */
public interface ReminderDao {

    /**
     * Persists a new reminder and assigns it the generated id.
     *
     * @throws DataAccessException if the insert fails
     */
    Reminder create(Reminder reminder);

    /**
     * Finds a reminder by its id.
     *
     * @return the reminder, or {@link Optional#empty()} if no such row exists
     * @throws DataAccessException if the query fails
     */
    Optional<Reminder> findById(int id);

    /**
     * Returns the reminders attached to one task, ordered by time.
     *
     * @throws DataAccessException if the query fails
     */
    List<Reminder> findByTaskId(int taskId);

    /**
     * Returns pending reminders whose time is at or before {@code now},
     * earliest first — i.e. the reminders that should fire.
     *
     * <p>The filtering is done in SQL rather than in Java so the database only
     * ships back the rows that matter.
     *
     * @param now the reference time
     * @throws DataAccessException if the query fails
     */
    List<Reminder> findDue(LocalDateTime now);

    /**
     * Saves changes to an existing reminder (time or sent flag).
     *
     * @return {@code true} if a row was updated, {@code false} if the id was not found
     * @throws DataAccessException if the update fails
     */
    boolean update(Reminder reminder);

    /**
     * Deletes a reminder by id.
     *
     * @return {@code true} if a row was deleted, {@code false} if the id was not found
     * @throws DataAccessException if the delete fails
     */
    boolean deleteById(int id);
}
