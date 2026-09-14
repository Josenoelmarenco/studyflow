package com.studyflow.config;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Supplies database connections to the DAO layer.
 *
 * <p>This tiny interface is the single most important design decision in the
 * project. Because DAOs depend on this abstraction rather than on a concrete
 * static method, the same DAO class can run against:
 *
 * <ul>
 *   <li>MariaDB, in production ({@link MariaDbConnectionProvider})</li>
 *   <li>H2 in-memory, in tests</li>
 * </ul>
 *
 * <p>Without it, every DAO test would require a running MariaDB server — which
 * is why hard-wiring connections inside a DAO leads to untestable code and,
 * inevitably, to 0% coverage.
 *
 * <p>It is a {@code @FunctionalInterface}, so a test can supply one with a
 * lambda: {@code () -> DriverManager.getConnection(H2_URL)}.
 */
@FunctionalInterface
public interface ConnectionProvider {

    /**
     * Opens a new database connection.
     *
     * <p>The caller owns the returned connection and is responsible for
     * closing it — always use try-with-resources.
     *
     * @return an open connection
     * @throws SQLException if the connection cannot be established
     */
    Connection getConnection() throws SQLException;
}
