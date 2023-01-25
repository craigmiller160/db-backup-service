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

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.craigmiller160.db.backup.properties.PropertyStore;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MongoCleanupTaskTest {

  private static final String DB_NAME = "DbName";
  private static final String OUTPUT_ROOT =
      String.format("%s/%s", System.getProperty("user.dir"), "target/output");

  private PropertyStore propStore;
  private MongoCleanupTask mongoCleanupTask;
  private String time1;
  private String time2;
  private String time3;
  private Path outputPath;

  @BeforeEach
  public void beforeEach() throws Exception {
    FileUtils.deleteDirectory(new File(OUTPUT_ROOT));

    final var props = new Properties();
    props.setProperty(PropertyStore.OUTPUT_ROOT_DIR, OUTPUT_ROOT);
    props.setProperty(PropertyStore.OUTPUT_CLEANUP_AGE_DAYS, "10");
    propStore = new PropertyStore(props);

    time1 = LocalDateTime.of(2020, 1, 1, 0, 0, 0).format(BackupConstants.FORMAT);
    time2 = LocalDateTime.of(2020, 1, 2, 0, 0, 0).format(BackupConstants.FORMAT);
    time3 = LocalDateTime.now().format(BackupConstants.FORMAT);

    outputPath = Paths.get(OUTPUT_ROOT, BackupConstants.MONGO_DIR, DB_NAME);

    mongoCleanupTask = new MongoCleanupTask(propStore, DB_NAME);
  }

  @AfterEach
  public void afterEach() throws Exception {
    FileUtils.deleteDirectory(new File(OUTPUT_ROOT));
  }

  @Test
  public void test_run() throws Exception {
    Files.createDirectories(Path.of(outputPath.toString(), time1));
    Files.createDirectories(Path.of(outputPath.toString(), time2));
    Files.createDirectories(Path.of(outputPath.toString(), time3));

    mongoCleanupTask.run();

    final var remainingFiles = Files.list(outputPath).collect(Collectors.toList());
    assertEquals(1, remainingFiles.size());
    assertEquals(time3, remainingFiles.get(0).getFileName().toString());
  }
}
