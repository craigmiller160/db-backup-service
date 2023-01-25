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
import io.craigmiller160.db.backup.config.dto.MongoDatabaseConfig;
import io.craigmiller160.db.backup.email.EmailService;
import io.craigmiller160.db.backup.properties.PropertyStore;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Try;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackupScheduler {

  private static final Logger log = LoggerFactory.getLogger(BackupScheduler.class);

  private final PropertyStore propStore;
  private final BackupConfig backupConfig;
  private final ScheduledExecutorService executor;
  private final TaskFactory taskFactory;
  private final EmailService emailService;

  public BackupScheduler(
      final PropertyStore propStore,
      final BackupConfig backupConfig,
      final TaskFactory taskFactory,
      final EmailService emailService) {
    this.propStore = propStore;
    this.backupConfig = backupConfig;
    this.taskFactory = taskFactory;
    this.emailService = emailService;
    this.executor = Executors.newScheduledThreadPool(propStore.getExecutorThreadCount());
  }

  public void start() {
    this.executor.scheduleAtFixedRate(
        taskFactory.createLivenessCheckTask(propStore),
        0,
        propStore.getExecutorIntervalSecs(),
        TimeUnit.SECONDS);
    backupConfig.postgres().databases().stream()
        .flatMap(db -> db.schemas().stream().map(schema -> Tuple.of(db.name(), schema)))
        .map(
            tuple -> {
              final var backupTask =
                  taskFactory.createPostgresBackupTask(propStore, emailService, tuple._1, tuple._2);
              final var cleanupTask =
                  taskFactory.createPostgresCleanupTask(propStore, tuple._1, tuple._2);
              return Tuple.of(backupTask, cleanupTask);
            })
        .forEach(this::scheduleTaskPair);

    backupConfig.mongodb().databases().stream()
        .map(MongoDatabaseConfig::name)
        .map(
            dbName -> {
              final var backupTask =
                  taskFactory.createMongoBackupTask(propStore, emailService, dbName);
              final var cleanupTask = taskFactory.createMongoCleanupTask(propStore, dbName);
              return Tuple.of(backupTask, cleanupTask);
            })
        .forEach(this::scheduleTaskPair);
  }

  private void scheduleTaskPair(final Tuple2<Runnable, Runnable> taskTuple) {
    final var backupTask = taskTuple._1;
    final var cleanupTask = taskTuple._2;
    executor.scheduleAtFixedRate(
        backupTask, 0, propStore.getExecutorIntervalSecs(), TimeUnit.SECONDS);
    executor.scheduleAtFixedRate(
        cleanupTask, 0, propStore.getExecutorIntervalSecs(), TimeUnit.SECONDS);
  }

  public boolean stop() {
    return Try.of(
            () -> {
              this.executor.shutdown();
              return this.executor.awaitTermination(60000, TimeUnit.SECONDS);
            })
        .recoverWith(
            ex -> {
              log.error("Error shutting down executor", ex);
              return Try.success(false);
            })
        .get();
  }
}
