package io.craigmiller160.db.backup.properties;

import io.craigmiller160.db.backup.exception.PropertyException;

import java.util.Properties;

public class PropertyStore {

    private static final String DB_POSTGRES_HOST = "db.postgres.host";
    private static final String DB_POSTGRES_PORT = "db.postgres.port";
    private static final String DB_POSTGRES_USER = "db.postgres.user";
    private static final String DB_POSTGRES_PASSWORD = "db.postgres.password";
    private static final String EXECUTOR_THREAD_COUNT = "executor.thread-count";
    private static final String EXECUTOR_INTERVAL_SECS = "executor.interval-secs";

    private final Properties props;

    public PropertyStore(final Properties props) {
        this.props = props;
    }

    public String getPostgresHost() {
        return props.getProperty(DB_POSTGRES_HOST);
    }

    public String getPostgresPort() {
        return props.getProperty(DB_POSTGRES_PORT);
    }

    public String getPostgresUser() {
        return props.getProperty(DB_POSTGRES_USER);
    }

    public String getPostgresPassword() {
        return props.getProperty(DB_POSTGRES_PASSWORD);
    }

    public int getExecutorThreadCount() {
        return Integer.parseInt(props.getProperty(EXECUTOR_THREAD_COUNT));
    }

    public int getExecutorIntervalSecs() {
        return Integer.parseInt(props.getProperty(EXECUTOR_INTERVAL_SECS));
    }

}
