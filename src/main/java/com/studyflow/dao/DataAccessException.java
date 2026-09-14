package com.studyflow.dao;

/**
 * Thrown when a database operation fails.
 *
 * <p>Wraps the low-level {@code SQLException} so that callers above the DAO
 * layer never import {@code java.sql}. It is unchecked on purpose: a failed
 * query is almost always unrecoverable at the call site, and forcing every
 * caller to write a {@code catch} block tends to produce empty handlers that
 * swallow the error — which is exactly how bugs become invisible.
 */
public class DataAccessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataAccessException(String message) {
        super(message);
    }
}
