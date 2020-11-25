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
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BackupScheduler {

    private static final Logger log = LoggerFactory.getLogger(BackupScheduler.class);

    private final PropertyStore propStore;
    private final BackupConfig backupConfig;
    private final ScheduledExecutorService executor;
    private final BackupTaskFactory backupTaskFactory;

    public BackupScheduler(final PropertyStore propStore,
                           final BackupConfig backupConfig,
                           final BackupTaskFactory backupTaskFactory) {
        this.propStore = propStore;
        this.backupConfig = backupConfig;
        this.backupTaskFactory = backupTaskFactory;
        this.executor = Executors.newScheduledThreadPool(propStore.getExecutorThreadCount());
    }

    public void start() {
        backupConfig.databases()
                .stream()
                .flatMap(db ->
                        db.schemas()
                                .stream()
                                .map(schema -> Tuple.of(db.name(), schema))
                )
                .map(tuple -> backupTaskFactory.createBackupTask(propStore, tuple._1, tuple._2))
                .forEach(task -> executor.scheduleAtFixedRate(task, 0, propStore.getExecutorIntervalSecs(), TimeUnit.SECONDS));
    }

    public boolean stop() {
        return Try.of(() -> {
            this.executor.shutdown();
            return this.executor.awaitTermination(60000, TimeUnit.SECONDS);
        })
                .recoverWith(ex -> {
                    log.error("Error shutting down executor", ex);
                    return Try.success(false);
                })
                .get();
    }

}
