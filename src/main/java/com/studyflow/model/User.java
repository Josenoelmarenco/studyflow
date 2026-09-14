package com.studyflow.model;

import java.time.LocalDateTime;

/**
 * The person who owns the planner. Courses belong to a user.
 *
 * <p>StudyFlow is a single-user desktop application, so in practice there is one
 * row in the {@code users} table. The entity still exists because the schema is
 * relational and every course carries a {@code user_id}: modelling it keeps the
 * data layer honest and leaves the door open to a future multi-user version
 * without reshaping the database.
 *
 * <p>The password hash is never logged. This class stores whatever hash the
 * caller provides; it deliberately does not know <em>how</em> passwords are
 * hashed, because that is an authentication concern, not a domain one.
 */
public class User {

    private int id;
    private String name;
    private String email;
    private String passwordHash;
    private LocalDateTime createdAt;

    /** Creates an unsaved user (no id, no creation timestamp yet). */
    public User(String name, String email, String passwordHash) {
        this(0, name, email, passwordHash, null);
    }

    public User(int id, String name, String email, String passwordHash, LocalDateTime createdAt) {
        this.id = id;
        setName(name);
        setEmail(email);
        setPasswordHash(passwordHash);
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public final void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be null or blank");
        }
        this.name = name.trim();
    }

    public String getEmail() {
        return email;
    }

    /**
     * Sets the email after a minimal sanity check.
     *
     * <p>The check is intentionally shallow — exactly one {@code @} with text on
     * both sides. Full RFC-5322 validation is famously error-prone and belongs
     * to whatever actually sends mail, not to a domain object.
     */
    public final void setEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email must not be null or blank");
        }
        String trimmed = email.trim();
        int at = trimmed.indexOf('@');
        if (at <= 0 || at != trimmed.lastIndexOf('@') || at == trimmed.length() - 1) {
            throw new IllegalArgumentException("email is not a valid address: " + email);
        }
        this.email = trimmed;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public final void setPasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("passwordHash must not be null or blank");
        }
        this.passwordHash = passwordHash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /** Never expose the password hash in logs or stack traces. */
    @Override
    public String toString() {
        return "User{id=%d, name='%s', email='%s', passwordHash=***}"
                .formatted(id, name, email);
    }
}
