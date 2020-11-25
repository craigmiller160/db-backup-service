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

    // TODO add validation to the properties when they are first read

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

    public String getOutputRootDirectory() {
        return props.getProperty(OUTPUT_ROOT_DIR);
    }

    public String getConfigFile() {
        return props.getProperty(CONFIG_FILE);
    }

}
