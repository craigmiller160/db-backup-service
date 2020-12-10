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
import io.craigmiller160.db.backup.email.EmailService;
import io.craigmiller160.db.backup.properties.PropertyStore;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Option;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

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
    private EmailService emailService;
    private TestTaskFactory backupTaskFactory;
    private BackupScheduler backupScheduler;

    @BeforeEach
    public void setup() {
        final var properties = new Properties();
        properties.setProperty(PropertyStore.EXECUTOR_THREAD_COUNT, "4");
        properties.setProperty(PropertyStore.EXECUTOR_INTERVAL_SECS, "3000");
        properties.setProperty(PropertyStore.EMAIL_CONNECT_TIMEOUT_SECS, "30");
        propStore = new PropertyStore(properties);
        backupConfig = new BackupConfig(List.of(
                new DatabaseConfig(DB_NAME, List.of(SCHEMA_1, SCHEMA_2)),
                new DatabaseConfig(DB_NAME_2, List.of(SCHEMA_3))
        ));
        emailService = new EmailService(propStore);
        backupTaskFactory = new TestTaskFactory();
        backupScheduler = new BackupScheduler(propStore, backupConfig, backupTaskFactory, emailService);
    }

    @AfterEach
    public void after() {
        backupScheduler.stop();
    }

    @Test
    public void test_start() throws Exception {
        backupScheduler.start();
        Thread.sleep(1000);
        assertTrue(backupScheduler.stop());

        final var backupTaskProps = backupTaskFactory.getBackupTaskProps();
        assertEquals(3, backupTaskProps.size());
        backupTaskProps.sort(Comparator.comparing(t -> t._2));
        assertEquals(Tuple.of(DB_NAME, SCHEMA_1), backupTaskProps.get(0));
        assertEquals(Tuple.of(DB_NAME, SCHEMA_2), backupTaskProps.get(1));
        assertEquals(Tuple.of(DB_NAME_2, SCHEMA_3), backupTaskProps.get(2));

        final var cleanupTaskProps = backupTaskFactory.getCleanupTaskProps();
        assertEquals(3, cleanupTaskProps.size());
        backupTaskProps.sort(Comparator.comparing(t -> t._2));
        assertEquals(Tuple.of(DB_NAME, SCHEMA_1), cleanupTaskProps.get(0));
        assertEquals(Tuple.of(DB_NAME, SCHEMA_2), cleanupTaskProps.get(1));
        assertEquals(Tuple.of(DB_NAME_2, SCHEMA_3), cleanupTaskProps.get(2));

        final var livenessCheckPropStore = backupTaskFactory.getLivenessCheckPropStore();
        assertTrue(livenessCheckPropStore.isDefined());
    }

    private static class TestTaskFactory extends TaskFactory {
        private final List<Tuple2<String,String>> backupTaskProps = Collections.synchronizedList(new ArrayList<>());
        private final List<Tuple2<String,String>> cleanupTaskProps = Collections.synchronizedList(new ArrayList<>());
        private final AtomicReference<PropertyStore> livenessCheckPropStore = new AtomicReference<>(null);

        @Override
        public Runnable createBackupTask(final PropertyStore propStore, final EmailService emailService, final String database, final String schema) {
            return () -> {
                backupTaskProps.add(Tuple.of(database, schema));
            };
        }

        @Override
        public Runnable createLivenessCheckTask(final PropertyStore propStore) {
            return () -> {
                livenessCheckPropStore.set(propStore);
            };
        }

        @Override
        public Runnable createCleanupTask(final PropertyStore propStore, final String database, final String schema) {
            return () -> {
                cleanupTaskProps.add(Tuple.of(database, schema));
            };
        }

        public List<Tuple2<String,String>> getBackupTaskProps() {
            return new ArrayList<>(backupTaskProps);
        }

        public Option<PropertyStore> getLivenessCheckPropStore() {
            return Option.of(livenessCheckPropStore.get());
        }

        public List<Tuple2<String,String>> getCleanupTaskProps() {
            return new ArrayList<>(cleanupTaskProps);
        }
    }

}
