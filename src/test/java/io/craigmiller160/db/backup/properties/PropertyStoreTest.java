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

import io.vavr.control.Try;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PropertyStoreTest {

    private Properties props;

    @BeforeEach
    public void setup() {
        props = new Properties();
        props.setProperty(PropertyStore.DB_POSTGRES_HOST, "host");
        props.setProperty(PropertyStore.DB_POSTGRES_PORT, "100");
        props.setProperty(PropertyStore.DB_POSTGRES_USER, "user");
        props.setProperty(PropertyStore.DB_POSTGRES_PASSWORD, "password");
        props.setProperty(PropertyStore.EXECUTOR_THREAD_COUNT, "4");
        props.setProperty(PropertyStore.EXECUTOR_INTERVAL_SECS, "1000");
        props.setProperty(PropertyStore.OUTPUT_ROOT_DIR, System.getProperty("user.dir"));
        props.setProperty(PropertyStore.CONFIG_FILE, "backup_config.json");
    }

    @Test
    public void test_allValid() {
        final var propStore = new PropertyStore(props);
        final var result = propStore.validateProperties()
                .recoverWith(ex -> {
                    ex.printStackTrace();
                    return Try.failure(ex);
                })
                .isSuccess();
        assertTrue(result);
    }

    @Test
    public void test_missingProperty() {
        throw new RuntimeException();
    }

    @Test
    public void test_propertyShouldBeNumeric() {
        throw new RuntimeException();
    }

}
