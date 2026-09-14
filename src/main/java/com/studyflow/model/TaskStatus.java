package com.studyflow.model;

import java.util.Locale;

/**
 * Lifecycle state of a {@link Task}.
 *
 * <p>Modelling status as an enum rather than a free-form {@code String} means
 * the compiler rejects {@code "Pendng"} instead of silently storing it, and a
 * {@code switch} over the states is checked for exhaustiveness. The database
 * column stores {@link #name()}.
 */
public enum TaskStatus {

    PENDING("Pending"),
    IN_PROGRESS("In progress"),
    DONE("Done");

    private final String label;

    TaskStatus(String label) {
        this.label = label;
    }

    /** Human-readable text for the user interface. */
    public String label() {
        return label;
    }

    /**
     * Converts a stored database value back into a status.
     *
     * <p>Accepts any casing and tolerates the legacy {@code "In Progress"}
     * spelling with a space, so older rows keep loading.
     *
     * @param value the raw column value, may be {@code null}
     * @return the matching status, or {@link #PENDING} when {@code value} is null or blank
     * @throws IllegalArgumentException if the value is non-blank but unrecognised
     */
    public static TaskStatus fromDatabase(String value) {
        if (value == null || value.isBlank()) {
            return PENDING;
        }

        String normalised = value.trim()
                .toUpperCase(Locale.ROOT)
                .replace(' ', '_');

        try {
            return TaskStatus.valueOf(normalised);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown task status: '" + value + "'", e);
        }
    }
}
