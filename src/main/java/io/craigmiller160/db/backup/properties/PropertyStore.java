/*
 *     db-backup-service
 *     Copyright (C) 2020 Craig Miller
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.craigmiller160.db.backup.properties;

import io.craigmiller160.db.backup.exception.PropertyException;
import io.vavr.Tuple;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.control.Try;

import java.util.NoSuchElementException;
import java.util.Properties;

public class PropertyStore {

    public static final String DB_POSTGRES_HOST = "db.postgres.host";
    public static final String DB_POSTGRES_PORT = "db.postgres.port";
    public static final String DB_POSTGRES_USER = "db.postgres.user";
    public static final String DB_POSTGRES_PASSWORD = "db.postgres.password";
    public static final String EXECUTOR_THREAD_COUNT = "executor.thread-count";
    public static final String EXECUTOR_INTERVAL_SECS = "executor.interval-secs";
    public static final String OUTPUT_ROOT_DIR = "output.root-directory";
    public static final String CONFIG_FILE = "config.file";
    public static final String EMAIL_HOST = "email.host";
    public static final String EMAIL_TO = "email.to";
    public static final String EMAIL_CONNECT_TIMEOUT_SECS = "email.connect-timeout-secs";
    public static final String EMAIL_AUTH_HOST = "email.auth.host";
    public static final String EMAIL_AUTH_CLIENT_KEY = "email.auth.client-key";
    public static final String EMAIL_AUTH_CLIENT_SECRET = "email.auth.client-secret";
    public static final String EMAIL_AUTH_USER = "email.auth.user";
    public static final String EMAIL_AUTH_PASSWORD = "email.auth.password";

    private static final Map<String,PropertyValidator> PROPERTY_VALIDATION_MAP =
            HashMap.ofEntries(
                    Tuple.of(DB_POSTGRES_HOST, PropertyValidator.IS_NOT_BLANK),
                    Tuple.of(DB_POSTGRES_PORT, PropertyValidator.IS_NUMERIC),
                    Tuple.of(DB_POSTGRES_USER, PropertyValidator.IS_NOT_BLANK),
                    Tuple.of(DB_POSTGRES_PASSWORD, PropertyValidator.IS_NOT_BLANK),
                    Tuple.of(EXECUTOR_THREAD_COUNT, PropertyValidator.IS_NUMERIC),
                    Tuple.of(EXECUTOR_INTERVAL_SECS, PropertyValidator.IS_NUMERIC),
                    Tuple.of(OUTPUT_ROOT_DIR, PropertyValidator.IS_NOT_BLANK),
                    Tuple.of(CONFIG_FILE, PropertyValidator.IS_NOT_BLANK),
                    Tuple.of(EMAIL_HOST, PropertyValidator.IS_NOT_BLANK),
                    Tuple.of(EMAIL_TO, PropertyValidator.IS_NOT_BLANK),
                    Tuple.of(EMAIL_CONNECT_TIMEOUT_SECS, PropertyValidator.IS_NUMERIC),
                    Tuple.of(EMAIL_AUTH_HOST, PropertyValidator.IS_NOT_BLANK),
                    Tuple.of(EMAIL_AUTH_CLIENT_KEY, PropertyValidator.IS_NOT_BLANK),
                    Tuple.of(EMAIL_AUTH_CLIENT_SECRET, PropertyValidator.IS_NOT_BLANK),
                    Tuple.of(EMAIL_AUTH_USER, PropertyValidator.IS_NOT_BLANK),
                    Tuple.of(EMAIL_AUTH_PASSWORD, PropertyValidator.IS_NOT_BLANK)
            );

    private final Properties props;

    public PropertyStore(final Properties props) {
        this.props = props;
    }

    public Try<?> validateProperties() {
        return PROPERTY_VALIDATION_MAP
                .find(entry -> !entry._2.validate(props.getProperty(entry._1)))
                .toTry()
                .flatMap(entry -> Try.failure(new PropertyException(String.format("Invalid property value: %s", entry._1))))
                .recoverWith(ex -> {
                    if (ex instanceof NoSuchElementException) {
                        return Try.success("Found element");
                    } else {
                        return Try.failure(ex);
                    }
                });
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

    public String getOutputRootDirectory() {
        return props.getProperty(OUTPUT_ROOT_DIR);
    }

    public String getConfigFile() {
        return props.getProperty(CONFIG_FILE);
    }

    public String getEmailHost() {
        return props.getProperty(EMAIL_HOST);
    }

    public String getEmailTo() {
        return props.getProperty(EMAIL_TO);
    }

    public int getEmailConnectTimeoutSecs() {
        return Integer.parseInt(props.getProperty(EMAIL_CONNECT_TIMEOUT_SECS));
    }

    public String getEmailAuthHost() {
        return props.getProperty(EMAIL_AUTH_HOST);
    }

    public String getEmailAuthClientKey() {
        return props.getProperty(EMAIL_AUTH_CLIENT_KEY);
    }

    public String getEmailAuthClientSecret() {
        return props.getProperty(EMAIL_AUTH_CLIENT_SECRET);
    }

    public String getEmailAuthUser() {
        return props.getProperty(EMAIL_AUTH_USER);
    }

    public String getEmailAuthPassword() {
        return props.getProperty(EMAIL_AUTH_PASSWORD);
    }

}
