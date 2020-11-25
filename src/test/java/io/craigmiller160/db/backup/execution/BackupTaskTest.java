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

package io.craigmiller160.db.backup.execution;

import io.craigmiller160.db.backup.properties.PropertyStore;
import io.vavr.control.Option;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

@ExtendWith(MockitoExtension.class)
public class BackupTaskTest {

    private static final String DB_NAME = "DbName";
    private static final String SCHEMA_NAME = "SchemaName";
    private static final String HOST = "host";
    private static final String PORT = "100";
    private static final String USER = "user";
    private static final String PASSWORD = "password";
    private static final String OUTPUT_ROOT = "";

    private PropertyStore propStore;
    private BackupTask backupTask;
    @Mock
    private Process process;
    private TestProcessProvider testProcessProvider;

    @BeforeEach
    public void setup() {
        final var props = new Properties();
        props.setProperty(PropertyStore.DB_POSTGRES_HOST, HOST);
        props.setProperty(PropertyStore.DB_POSTGRES_PORT, PORT);
        props.setProperty(PropertyStore.DB_POSTGRES_USER, USER);
        props.setProperty(PropertyStore.DB_POSTGRES_PASSWORD, PASSWORD);
        props.setProperty(PropertyStore.OUTPUT_ROOT_DIR, OUTPUT_ROOT);
        propStore = new PropertyStore(props);
        testProcessProvider = new TestProcessProvider(process);
        backupTask = new BackupTask(propStore, DB_NAME, SCHEMA_NAME, testProcessProvider);
    }

    @Test
    public void test_run() {
        backupTask.run();
        throw new RuntimeException();
    }

    @Test
    public void test_run_cantFindData() {
        throw new RuntimeException();
    }

    private static class TestProcessProvider implements ProcessProvider {
        private String[] command = null;
        private Map<String,String> environment = null;
        private final Process mockProcess;

        public TestProcessProvider(final Process mockProcess) {
            this.mockProcess = mockProcess;
        }

        @Override
        public Process provide(final String[] command, final Map<String, String> environment) throws IOException {
            this.command = command;
            this.environment = environment;
            return mockProcess;
        }

        public Option<String[]> getCommand() {
            return Option.of(command);
        }

        public Option<Map<String,String>> getEnvironment() {
            return Option.of(environment);
        }
    }

}
