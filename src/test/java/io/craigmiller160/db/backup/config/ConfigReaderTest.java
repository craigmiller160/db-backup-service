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

import io.craigmiller160.db.backup.config.dto.BackupConfig;
import io.craigmiller160.db.backup.config.dto.DatabaseConfig;
import io.craigmiller160.db.backup.properties.PropertyStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        final var expected = new BackupConfig(
                List.of(
                        new DatabaseConfig(
                                "vm_dev",
                                List.of("public")
                        ),
                        new DatabaseConfig(
                                "fake",
                                List.of("public")
                        )
                )
        );
        final var actual = configReader.readBackupConfig().get();
        assertEquals(expected, actual);
    }

}
