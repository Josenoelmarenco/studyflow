package com.studyflow.service;

import com.studyflow.dao.UserDao;
import com.studyflow.model.User;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Business logic for users.
 *
 * <p>Because StudyFlow is a single-user desktop planner without a login screen,
 * the method that matters most is {@link #findOrCreateDefault}: on first launch
 * it creates the one local user; on every launch after that it returns the same
 * one. That keeps every course anchored to a real {@code user_id} without asking
 * the student to register.
 */
public class UserService {

    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = Objects.requireNonNull(userDao, "userDao must not be null");
    }

    /** Registers a new user. Field validation is enforced by the {@link User} model. */
    public User register(String name, String email, String passwordHash) {
        return userDao.create(new User(name, email, passwordHash));
    }

    public Optional<User> findById(int id) {
        return userDao.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userDao.findByEmail(email);
    }

    public List<User> findAll() {
        return userDao.findAll();
    }

    /**
     * Returns the existing user with this email, or creates one if none exists.
     *
     * <p>Idempotent: calling it twice with the same email yields the same user,
     * never a duplicate — which is exactly what a launch-time bootstrap needs.
     */
    public User findOrCreateDefault(String name, String email, String passwordHash) {
        return userDao.findByEmail(email)
                .orElseGet(() -> userDao.create(new User(name, email, passwordHash)));
    }
}
