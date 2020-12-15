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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.Properties;

@ExtendWith(MockitoExtension.class)
public class MongoBackupTaskTest {

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
        throw new RuntimeException();
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
