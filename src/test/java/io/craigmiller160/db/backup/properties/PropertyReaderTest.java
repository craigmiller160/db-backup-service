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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearEnvironmentVariable;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

public class PropertyReaderTest {

  private final PropertyReader propertyReader = new PropertyReader();

  @AfterEach
  @ClearEnvironmentVariable(key = "db.postgres.host")
  @ClearEnvironmentVariable(key = "db.postgres.password")
  @ClearEnvironmentVariable(key = "db.mongo.password")
  public void clean() {}

  @Test
  @SetEnvironmentVariable(key = "db.postgres.host", value = "TestHost")
  @SetEnvironmentVariable(key = "db.postgres.password", value = "password22")
  @SetEnvironmentVariable(key = "db.mongo.password", value = "password22")
  public void test_readProperties() throws Exception {
    final var propStore = propertyReader.readProperties().get();
    assertEquals("TestHost", propStore.getPostgresHost());
    assertEquals("5432", propStore.getPostgresPort());
    assertEquals("postgres", propStore.getPostgresUser());
    assertEquals("password22", propStore.getPostgresPassword());
  }
}
