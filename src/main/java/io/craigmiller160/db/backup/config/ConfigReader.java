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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.craigmiller160.db.backup.config.dto.BackupConfig;
import io.craigmiller160.db.backup.exception.ConfigReadException;
import io.craigmiller160.db.backup.properties.PropertyStore;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigReader {

  private static final Logger log = LoggerFactory.getLogger(ConfigReader.class);
  private final ObjectMapper mapper = new ObjectMapper();

  private final PropertyStore propStore;

  public ConfigReader(final PropertyStore propStore) {
    this.propStore = propStore;
  }

  public Try<BackupConfig> readBackupConfig() {
    log.info("Reading Backup Configuration");
    return Try.withResources(
            () ->
                ConfigReader.class.getClassLoader().getResourceAsStream(propStore.getConfigFile()))
        .of(configStream -> mapper.readValue(configStream, BackupConfig.class))
        .recoverWith(
            ex -> Try.failure(new ConfigReadException("Error reading configuration file", ex)));
  }
}
