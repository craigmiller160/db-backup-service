package io.craigmiller160.db.backup.properties;

import io.craigmiller160.db.backup.exception.PropertyException;

import java.util.Properties;

public class PropertyStore {

    private static final String DB_POSTGRES_HOST = "db.postgres.host";
    private static final String DB_POSTGRES_PORT = "db.postgres.port";
    private static final String DB_POSTGRES_USER = "db.postgres.user";
    private static final String DB_POSTGRES_PASSWORD = "db.postgres.password";

    private final Properties props;

    public PropertyStore(final Properties props) {
        this.props = props;
    }

    private String getRequiredProperty(final String key) throws PropertyException {
        final String value = props.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            throw new PropertyException(String.format("Required property is missing: %s", key));
        }
        return value;
    }

    public String getPostgresHost() throws PropertyException {
        return getRequiredProperty(DB_POSTGRES_HOST);
    }

    public String getPostgresPort() throws PropertyException {
        return getRequiredProperty(DB_POSTGRES_PORT);
    }

    public String getPostgresUser() throws PropertyException {
        return getRequiredProperty(DB_POSTGRES_USER);
    }

    public String getPostgresPassword() throws PropertyException {
        return getRequiredProperty(DB_POSTGRES_PASSWORD);
    }

}
