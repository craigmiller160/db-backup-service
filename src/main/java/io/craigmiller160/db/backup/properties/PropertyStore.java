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
    public static final String JETTY_PORT = "jetty.port";

    private static final Map<String,PropertyValidator> PROPERTY_VALIDATION_MAP =
            HashMap.of(
                    DB_POSTGRES_HOST, PropertyValidator.IS_NOT_BLANK,
                    DB_POSTGRES_PORT, PropertyValidator.IS_NUMERIC,
                    DB_POSTGRES_USER, PropertyValidator.IS_NOT_BLANK,
                    DB_POSTGRES_PASSWORD, PropertyValidator.IS_NOT_BLANK,
                    EXECUTOR_THREAD_COUNT, PropertyValidator.IS_NUMERIC,
                    EXECUTOR_INTERVAL_SECS, PropertyValidator.IS_NUMERIC,
                    OUTPUT_ROOT_DIR, PropertyValidator.IS_NOT_BLANK,
                    CONFIG_FILE, PropertyValidator.IS_NOT_BLANK,
                    JETTY_PORT, PropertyValidator.IS_NUMERIC
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

    public int getJettyPort() {
        return Integer.parseInt(props.getProperty(JETTY_PORT));
    }

}
