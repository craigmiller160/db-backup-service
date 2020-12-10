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
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class CleanupTaskTest {

    private static final String DB_NAME = "DbName";
    private static final String SCHEMA_NAME = "SchemaName";
    private static final String OUTPUT_ROOT = String.format("%s/%s", System.getProperty("user.dir"), "target/output");

    private PropertyStore propStore;
    private CleanupTask cleanupTask;
    private Path outputPath;

    @BeforeEach
    public void beforeEach() throws Exception {
        FileUtils.deleteDirectory(new File(OUTPUT_ROOT));

        final var props = new Properties();
        props.setProperty(PropertyStore.OUTPUT_ROOT_DIR, OUTPUT_ROOT);
        propStore = new PropertyStore(props);

        outputPath = Paths.get(OUTPUT_ROOT, DB_NAME, SCHEMA_NAME);

        cleanupTask = new CleanupTask(propStore, DB_NAME, SCHEMA_NAME);
    }

    @AfterEach
    public void afterEach() throws Exception {
        FileUtils.deleteDirectory(new File(OUTPUT_ROOT));
    }

    @Test
    public void test_run() throws Exception {
        Files.createDirectories(outputPath);
        throw new RuntimeException();
    }

    @Test
    public void test_run_noDirectory() throws Exception {
        throw new RuntimeException();
    }

}
