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
import io.craigmiller160.db.backup.config.dto.MongoBackupConfig;
import io.craigmiller160.db.backup.config.dto.MongoDatabaseConfig;
import io.craigmiller160.db.backup.config.dto.PostgresBackupConfig;
import io.craigmiller160.db.backup.config.dto.PostgresDatabaseConfig;
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
        properties.setProperty(PropertyStore.EXECUTOR_INTERVAL_SECS, "300000");
        properties.setProperty(PropertyStore.EMAIL_CONNECT_TIMEOUT_SECS, "30");
        propStore = new PropertyStore(properties);
        backupConfig = new BackupConfig(
                new PostgresBackupConfig(
                        List.of(
                                new PostgresDatabaseConfig(DB_NAME, List.of(SCHEMA_1, SCHEMA_2)),
                                new PostgresDatabaseConfig(DB_NAME_2, List.of(SCHEMA_3))
                        )
                ),
                new MongoBackupConfig(List.of(
                        new MongoDatabaseConfig(DB_NAME),
                        new MongoDatabaseConfig(DB_NAME_2)
                ))
        );
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

        final var postgresBackupTaskProps = backupTaskFactory.getPostgresBackupTaskProps();
        assertEquals(3, postgresBackupTaskProps.size());
        postgresBackupTaskProps.sort(Comparator.comparing(t -> t._2));
        assertEquals(Tuple.of(DB_NAME, SCHEMA_1), postgresBackupTaskProps.get(0));
        assertEquals(Tuple.of(DB_NAME, SCHEMA_2), postgresBackupTaskProps.get(1));
        assertEquals(Tuple.of(DB_NAME_2, SCHEMA_3), postgresBackupTaskProps.get(2));

        final var postgresCleanupTaskProps = backupTaskFactory.getPostgresCleanupTaskProps();
        assertEquals(3, postgresCleanupTaskProps.size());
        postgresCleanupTaskProps.sort(Comparator.comparing(t -> t._2));
        assertEquals(Tuple.of(DB_NAME, SCHEMA_1), postgresCleanupTaskProps.get(0));
        assertEquals(Tuple.of(DB_NAME, SCHEMA_2), postgresCleanupTaskProps.get(1));
        assertEquals(Tuple.of(DB_NAME_2, SCHEMA_3), postgresCleanupTaskProps.get(2));

        final var mongoBackupTaskProps = backupTaskFactory.getMongoBackupTaskProps();
        assertEquals(2, mongoBackupTaskProps.size());
        mongoBackupTaskProps.sort(Comparator.naturalOrder());
        assertEquals(DB_NAME, mongoBackupTaskProps.get(0));
        assertEquals(DB_NAME_2, mongoBackupTaskProps.get(1));

        final var mongoCleanupTaskProps = backupTaskFactory.getMongoCleanupTaskProps();
        assertEquals(2, mongoCleanupTaskProps.size());
        mongoCleanupTaskProps.sort(Comparator.naturalOrder());
        assertEquals(DB_NAME, mongoCleanupTaskProps.get(0));
        assertEquals(DB_NAME_2, mongoCleanupTaskProps.get(1));

        final var livenessCheckPropStore = backupTaskFactory.getLivenessCheckPropStore();
        assertTrue(livenessCheckPropStore.isDefined());
    }

    private static class TestTaskFactory extends TaskFactory {
        private final List<Tuple2<String,String>> postgresBackupTaskProps = Collections.synchronizedList(new ArrayList<>());
        private final List<Tuple2<String,String>> postgresCleanupTaskProps = Collections.synchronizedList(new ArrayList<>());
        private final AtomicReference<PropertyStore> livenessCheckPropStore = new AtomicReference<>(null);
        private final List<String> mongoBackupTaskProps = Collections.synchronizedList(new ArrayList<>());
        private final List<String> mongoCleanupTaskProps = Collections.synchronizedList(new ArrayList<>());

        @Override
        public Runnable createPostgresBackupTask(final PropertyStore propStore, final EmailService emailService, final String database, final String schema) {
            return () -> {
                postgresBackupTaskProps.add(Tuple.of(database, schema));
            };
        }

        @Override
        public Runnable createLivenessCheckTask(final PropertyStore propStore) {
            return () -> {
                livenessCheckPropStore.set(propStore);
            };
        }

        @Override
        public Runnable createPostgresCleanupTask(final PropertyStore propStore, final String database, final String schema) {
            return () -> {
                postgresCleanupTaskProps.add(Tuple.of(database, schema));
            };
        }

        @Override
        public Runnable createMongoBackupTask(final PropertyStore propStore, final EmailService emailService, final String database) {
            return () -> {
                mongoBackupTaskProps.add(database);
            };
        }

        @Override
        public Runnable createMongoCleanupTask(final PropertyStore propStore, final String database) {
            return () -> {
                mongoCleanupTaskProps.add(database);
            };
        }

        public List<Tuple2<String,String>> getPostgresBackupTaskProps() {
            return new ArrayList<>(postgresBackupTaskProps);
        }

        public Option<PropertyStore> getLivenessCheckPropStore() {
            return Option.of(livenessCheckPropStore.get());
        }

        public List<Tuple2<String,String>> getPostgresCleanupTaskProps() {
            return new ArrayList<>(postgresCleanupTaskProps);
        }

        public List<String> getMongoCleanupTaskProps() {
            return new ArrayList<>(mongoCleanupTaskProps);
        }

        public List<String> getMongoBackupTaskProps() {
            return new ArrayList<>(mongoBackupTaskProps);
        }
    }

}
