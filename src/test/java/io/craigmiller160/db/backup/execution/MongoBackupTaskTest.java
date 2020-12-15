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
import io.craigmiller160.db.backup.properties.PropertyStore;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MongoBackupTaskTest {

    private static final String DATA_CONTENT = "Success";
    private static final String DB_NAME = "DbName";
    private static final String HOST = "host";
    private static final String PORT = "100";
    private static final String USER = "user";
    private static final String PASSWORD = "password";
    private static final String AUTH_DB = "authDb";
    private static final String OUTPUT_ROOT = String.format("%s/%s", System.getProperty("user.dir"), "target/output");

    private PropertyStore propStore;
    private MongoBackupTask mongoBackupTask;
    @Mock
    private Process process;
    @Mock
    private EmailService emailService;
    private TestProcessProvider testProcessProvider;

    @BeforeEach
    public void beforeEach() throws Exception {
        FileUtils.deleteDirectory(new File(OUTPUT_ROOT));

        final var props = new Properties();
        props.setProperty(PropertyStore.DB_MONGO_HOST, HOST);
        props.setProperty(PropertyStore.DB_MONGO_PORT, PORT);
        props.setProperty(PropertyStore.DB_MONGO_USER, USER);
        props.setProperty(PropertyStore.DB_MONGO_PASSWORD, PASSWORD);
        props.setProperty(PropertyStore.OUTPUT_ROOT_DIR, OUTPUT_ROOT);
        props.setProperty(PropertyStore.DB_MONGO_AUTH_DB, AUTH_DB);

        propStore = new PropertyStore(props);
        testProcessProvider = new TestProcessProvider(process);
        mongoBackupTask = new MongoBackupTask(propStore, DB_NAME, testProcessProvider, emailService);
    }

    @AfterEach
    public void afterEach() throws Exception {
        FileUtils.deleteDirectory(new File(OUTPUT_ROOT));
    }

    @Test
    public void test_run() {
        when(process.getInputStream())
                .thenReturn(IOUtils.toInputStream(DATA_CONTENT, StandardCharsets.UTF_8));
        when(process.exitValue())
                .thenReturn(0);

        final var timestamp = BackupConstants.FORMAT.format(ZonedDateTime.now(ZoneId.of(BackupConstants.TIME_ZONE)));
        final var outputPath = Paths.get(OUTPUT_ROOT, MongoBackupTask.MONGO_DIR, DB_NAME, timestamp);

        mongoBackupTask.run();
        final var expectedCommand = new String[] {
                MongoBackupTask.MONGODUMP_PATH,
                MongoBackupTask.URI_TEMPLATE.formatted(USER, PASSWORD, HOST, Integer.parseInt(PORT), DB_NAME, AUTH_DB),
                MongoBackupTask.OUTPUT_PATH_ARG,
                outputPath.toString()
        };
        final var expectedEnvironment = new HashMap<String,String>();

        assertTrue(testProcessProvider.getCommand().isDefined());
        assertArrayEquals(expectedCommand, testProcessProvider.getCommand().get());

        // TODO finish this
    }

    @Test
    public void tst_run_error() {
        throw new RuntimeException();
    }

    @Test
    public void test_run_processError() {
        throw new RuntimeException();
    }

}
