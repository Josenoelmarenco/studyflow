package com.studyflow.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Production {@link ConnectionProvider} backed by MariaDB.
 *
 * <p>Opens a fresh connection per call. That is intentional for a single-user
 * desktop application: a connection pool would add a dependency and complexity
 * that this workload does not justify. If StudyFlow ever grows into a
 * multi-user or server-side product, this is the one class to replace — and
 * nothing in the DAO layer would need to change, because everything depends on
 * the {@link ConnectionProvider} interface rather than on this class.
 */
public final class MariaDbConnectionProvider implements ConnectionProvider {

    private final DatabaseConfig config;

    public MariaDbConnectionProvider(DatabaseConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
    }

    /** Convenience factory using settings from the environment. */
    public static MariaDbConnectionProvider fromEnvironment() {
        return new MariaDbConnectionProvider(DatabaseConfig.load());
    }

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(config.url(), config.user(), config.password());
    }
}
