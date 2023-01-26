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

package io.craigmiller160.db.backup.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.craigmiller160.db.backup.config.dto.BackupConfig;
import io.craigmiller160.db.backup.config.dto.MongoBackupConfig;
import io.craigmiller160.db.backup.config.dto.MongoDatabaseConfig;
import io.craigmiller160.db.backup.config.dto.PostgresBackupConfig;
import io.craigmiller160.db.backup.config.dto.PostgresDatabaseConfig;
import io.craigmiller160.db.backup.properties.PropertyStore;
import java.util.List;
import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ConfigReaderTest {

  private PropertyStore propStore;
  private ConfigReader configReader;

  @BeforeEach
  public void setup() {
    final var properties = new Properties();
    properties.setProperty("config.file", "backup_config.json");
    propStore = new PropertyStore(properties);
    configReader = new ConfigReader(propStore);
  }

  @Test
  public void test_readBackupConfig() throws Exception {
    final var expected =
        new BackupConfig(
            new PostgresBackupConfig(
                List.of(
                    new PostgresDatabaseConfig("vm_dev", List.of("public")),
                    new PostgresDatabaseConfig("fake", List.of("public")))),
            new MongoBackupConfig(List.of(new MongoDatabaseConfig("covid_19_prod"))));
    final var actual = configReader.readBackupConfig().get();
    assertEquals(expected, actual);
  }
}
