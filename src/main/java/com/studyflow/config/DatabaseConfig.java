package com.studyflow.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

/**
 * Resolves database settings from the environment, falling back to
 * {@code config.properties} on the classpath.
 *
 * <p>Precedence is deliberate: environment variables win. That order is what
 * lets the exact same build run locally (properties file), in a Jenkins job
 * (injected credentials), and inside a Docker container (env vars) without a
 * single code change.
 *
 * <p>No credential is ever hard-coded here.
 */
public final class DatabaseConfig {

    private static final String CONFIG_FILE = "/config.properties";

    private static final String ENV_URL = "STUDYFLOW_DB_URL";
    private static final String ENV_USER = "STUDYFLOW_DB_USER";
    private static final String ENV_PASSWORD = "STUDYFLOW_DB_PASSWORD";

    private final String url;
    private final String user;
    private final String password;

    DatabaseConfig(String url, String user, String password) {
        this.url = Objects.requireNonNull(url, "db.url must not be null");
        this.user = Objects.requireNonNull(user, "db.user must not be null");
        this.password = Objects.requireNonNull(password, "db.password must not be null");
    }

    /**
     * Builds the configuration for the running application.
     *
     * @return the resolved configuration
     * @throws IllegalStateException if a required setting is missing
     */
    public static DatabaseConfig load() {
        Properties properties = readPropertiesFile();

        String url = resolve(ENV_URL, properties, "db.url");
        String user = resolve(ENV_USER, properties, "db.user");
        String password = resolve(ENV_PASSWORD, properties, "db.password");

        return new DatabaseConfig(url, user, password);
    }

    private static Properties readPropertiesFile() {
        Properties properties = new Properties();
        try (InputStream in = DatabaseConfig.class.getResourceAsStream(CONFIG_FILE)) {
            if (in != null) {
                properties.load(in);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + CONFIG_FILE, e);
        }
        return properties;
    }

    private static String resolve(String envKey, Properties properties, String propertyKey) {
        String fromEnv = System.getenv(envKey);
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv;
        }

        String fromFile = properties.getProperty(propertyKey);
        if (fromFile != null && !fromFile.isBlank()) {
            return fromFile;
        }

        throw new IllegalStateException(
                "Missing database setting '" + propertyKey + "'. "
                        + "Set the " + envKey + " environment variable, or copy "
                        + "config.properties.example to config.properties.");
    }

    public String url() {
        return url;
    }

    public String user() {
        return user;
    }

    public String password() {
        return password;
    }

    /** Never expose the password in logs or stack traces. */
    @Override
    public String toString() {
        return "DatabaseConfig{url='" + url + "', user='" + user + "', password=***}";
    }
}
