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

import io.craigmiller160.db.backup.config.dto.BackupConfig;
import io.craigmiller160.db.backup.properties.PropertyStore;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class BackupSchedulerTest {

    private PropertyStore propStore;
    private BackupConfig backupConfig;
    private TestBackupTaskFactory backupTaskFactory;

    @BeforeEach
    public void setup() {
        final var properties = new Properties();
        propStore = new PropertyStore(properties);
    }

    @Test
    public void test_start() {
        throw new RuntimeException();
    }

    private static class TestBackupTaskFactory extends BackupTaskFactory {
        private final List<Tuple2<String,String>> taskProps = Collections.synchronizedList(new ArrayList<>());

        @Override
        public Runnable createBackupTask(PropertyStore propStore, String database, String schema) {
            return () -> {
                taskProps.add(Tuple.of(database, schema));
            };
        }

        public List<Tuple2<String,String>> getTaskProps() {
            return new ArrayList<>(taskProps);
        }
    }

}
