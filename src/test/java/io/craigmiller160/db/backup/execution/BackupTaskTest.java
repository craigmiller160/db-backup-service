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

import io.craigmiller160.db.backup.email.EmailService;
import io.craigmiller160.db.backup.exception.BackupException;
import io.craigmiller160.db.backup.properties.PropertyStore;
import io.vavr.control.Option;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BackupTaskTest {

    private static final String DATA_CONTENT = "Success";
    private static final String DB_NAME = "DbName";
    private static final String SCHEMA_NAME = "SchemaName";
    private static final String HOST = "host";
    private static final String PORT = "100";
    private static final String USER = "user";
    private static final String PASSWORD = "password";
    private static final String OUTPUT_ROOT = String.format("%s/%s", System.getProperty("user.dir"), "target/output");

    private PropertyStore propStore;
    private BackupTask backupTask;
    @Mock
    private Process process;
    @Mock
    private EmailService emailService;
    private TestProcessProvider testProcessProvider;

    @BeforeEach
    public void setup() throws Exception {
        FileUtils.deleteDirectory(new File(OUTPUT_ROOT));

        final var props = new Properties();
        props.setProperty(PropertyStore.DB_POSTGRES_HOST, HOST);
        props.setProperty(PropertyStore.DB_POSTGRES_PORT, PORT);
        props.setProperty(PropertyStore.DB_POSTGRES_USER, USER);
        props.setProperty(PropertyStore.DB_POSTGRES_PASSWORD, PASSWORD);
        props.setProperty(PropertyStore.OUTPUT_ROOT_DIR, OUTPUT_ROOT);

        propStore = new PropertyStore(props);
        testProcessProvider = new TestProcessProvider(process);
        backupTask = new BackupTask(propStore, DB_NAME, SCHEMA_NAME, emailService, testProcessProvider);
    }

    @Test
    public void test_run() throws Exception {
        when(process.getInputStream())
                .thenReturn(IOUtils.toInputStream(DATA_CONTENT, StandardCharsets.UTF_8));
        when(process.exitValue())
                .thenReturn(0);

        backupTask.run();
        final var expectedCommand = new String[] {
                BackupTask.PG_DUMP_CMD,
                DB_NAME,
                BackupTask.SCHEMA_ARG,
                SCHEMA_NAME,
                BackupTask.HOST_ARG,
                HOST,
                BackupTask.PORT_ARG,
                PORT,
                BackupTask.USER_ARG,
                USER,
                BackupTask.USE_INSERT_STATEMENTS
        };
        final var expectedEnvironment = Map.of(BackupTask.PASSWORD_ENV, PASSWORD);

        assertTrue(testProcessProvider.getCommand().isDefined());
        assertTrue(Arrays.equals(expectedCommand, testProcessProvider.getCommand().get()));

        assertTrue(testProcessProvider.getEnvironment().isDefined());
        assertEquals(expectedEnvironment, testProcessProvider.getEnvironment().get());

        final var outputRootDir = new File(OUTPUT_ROOT);
        final var outputDbDir = new File(outputRootDir, DB_NAME);
        final var outputSchemaDir = new File(outputDbDir, SCHEMA_NAME);
        assertTrue(outputSchemaDir.exists());

        final var files = outputSchemaDir.listFiles(file -> file.getName().endsWith(".sql"));
        assertNotNull(files);
        assertEquals(1, files.length);

        final var fileContent = IOUtils.toString(new FileInputStream(files[0]), StandardCharsets.UTF_8);
        assertEquals(DATA_CONTENT, fileContent);

        verify(emailService, times(0))
                .sendErrorAlertEmail(any(), any(), any());
    }

    @Test
    public void test_run_processError() {
        when(process.getInputStream())
                .thenReturn(IOUtils.toInputStream("", StandardCharsets.UTF_8));
        when(process.getErrorStream())
                .thenReturn(IOUtils.toInputStream("Error Message", StandardCharsets.UTF_8));
        when(process.exitValue())
                .thenReturn(1);

        backupTask.run();
        final var expectedCommand = new String[] {
                BackupTask.PG_DUMP_CMD,
                DB_NAME,
                BackupTask.SCHEMA_ARG,
                SCHEMA_NAME,
                BackupTask.HOST_ARG,
                HOST,
                BackupTask.PORT_ARG,
                PORT,
                BackupTask.USER_ARG,
                USER,
                BackupTask.USE_INSERT_STATEMENTS
        };
        final var expectedEnvironment = Map.of(BackupTask.PASSWORD_ENV, PASSWORD);

        assertTrue(testProcessProvider.getCommand().isDefined());
        assertTrue(Arrays.equals(expectedCommand, testProcessProvider.getCommand().get()));

        final var exceptionCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(emailService, times(1))
                .sendErrorAlertEmail(eq(DB_NAME), eq(SCHEMA_NAME), exceptionCaptor.capture());

        final var exception = exceptionCaptor.getValue();
        assertNotNull(exception);
        assertTrue(exception instanceof BackupException);
        assertEquals("Error Message", exception.getMessage());
    }

    @Test
    public void test_run_error() {
        when(process.getInputStream())
                .thenThrow(new RuntimeException("Dying"));
        when(process.exitValue())
                .thenReturn(0);

        backupTask.run();
        final var expectedCommand = new String[] {
                BackupTask.PG_DUMP_CMD,
                DB_NAME,
                BackupTask.SCHEMA_ARG,
                SCHEMA_NAME,
                BackupTask.HOST_ARG,
                HOST,
                BackupTask.PORT_ARG,
                PORT,
                BackupTask.USER_ARG,
                USER,
                BackupTask.USE_INSERT_STATEMENTS
        };
        final var expectedEnvironment = Map.of(BackupTask.PASSWORD_ENV, PASSWORD);

        assertTrue(testProcessProvider.getCommand().isDefined());
        assertTrue(Arrays.equals(expectedCommand, testProcessProvider.getCommand().get()));

        assertTrue(testProcessProvider.getEnvironment().isDefined());
        assertEquals(expectedEnvironment, testProcessProvider.getEnvironment().get());

        assertFalse(new File(OUTPUT_ROOT).exists());

        final var exceptionCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(emailService, times(1))
                .sendErrorAlertEmail(eq(DB_NAME), eq(SCHEMA_NAME), exceptionCaptor.capture());

        final var exception = exceptionCaptor.getValue();
        assertNotNull(exception);
        assertTrue(exception instanceof RuntimeException);
        assertEquals("Dying", exception.getMessage());
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
