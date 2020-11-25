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
import io.craigmiller160.db.backup.config.dto.DatabaseConfig;
import io.craigmiller160.db.backup.properties.PropertyStore;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BackupSchedulerTest {

    private static final String DB_NAME = "DbName";
    private static final String DB_NAME_2 = "DbName2";
    private static final String SCHEMA_1 = "schema1";
    private static final String SCHEMA_2 = "schema2";
    private static final String SCHEMA_3 = "schema3";

    private PropertyStore propStore;
    private BackupConfig backupConfig;
    private TestBackupTaskFactory backupTaskFactory;
    private BackupScheduler backupScheduler;

    @BeforeEach
    public void setup() {
        final var properties = new Properties();
        properties.setProperty("executor.thread-count", "4");
        properties.setProperty("executor.interval-secs", "3000");
        propStore = new PropertyStore(properties);
        backupConfig = new BackupConfig(List.of(
                new DatabaseConfig(DB_NAME, List.of(SCHEMA_1, SCHEMA_2)),
                new DatabaseConfig(DB_NAME_2, List.of(SCHEMA_3))
        ));
        backupTaskFactory = new TestBackupTaskFactory();
        backupScheduler = new BackupScheduler(propStore, backupConfig, backupTaskFactory);
    }

    @Test
    public void test_start() throws Exception {
        backupScheduler.start();
        Thread.sleep(1000);
        assertTrue(backupScheduler.stop());
        final var taskProps = backupTaskFactory.getTaskProps();
        assertEquals(3, taskProps.size());
        taskProps.sort(Comparator.comparing(t -> t._2));
        assertEquals(Tuple.of(DB_NAME, SCHEMA_1), taskProps.get(0));
        assertEquals(Tuple.of(DB_NAME, SCHEMA_2), taskProps.get(1));
        assertEquals(Tuple.of(DB_NAME_2, SCHEMA_3), taskProps.get(2));
    }

    private static class TestBackupTaskFactory extends BackupTaskFactory {
        private final List<Tuple2<String,String>> taskProps = Collections.synchronizedList(new ArrayList<>());

        @Override
        public Runnable createBackupTask(PropertyStore propStore, String database, String schema) {
            System.out.println("Creating task: " + database + " " + schema); // TODO delete this
            return () -> {
                taskProps.add(Tuple.of(database, schema));
            };
        }

        public List<Tuple2<String,String>> getTaskProps() {
            return new ArrayList<>(taskProps);
        }
    }

}
