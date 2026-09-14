package com.studyflow.dao;

import com.studyflow.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Persistence operations for {@link User}.
 *
 * <p>StudyFlow is single-user, so in practice this DAO manages one row. It still
 * earns its place: courses carry a {@code user_id} foreign key, and having the
 * user in the data layer keeps that relationship real instead of hard-coded.
 */
public interface UserDao {

    /**
     * Persists a new user and assigns it the generated id.
     *
     * @throws DataAccessException if the insert fails (for example, a duplicate email)
     */
    User create(User user);

    /**
     * Finds a user by id.
     *
     * @return the user, or {@link Optional#empty()} if no such row exists
     * @throws DataAccessException if the query fails
     */
    Optional<User> findById(int id);

    /**
     * Finds a user by their unique email address.
     *
     * @return the user, or {@link Optional#empty()} if none matches
     * @throws DataAccessException if the query fails
     */
    Optional<User> findByEmail(String email);

    /**
     * Returns every user, ordered by name.
     *
     * @throws DataAccessException if the query fails
     */
    List<User> findAll();
}
