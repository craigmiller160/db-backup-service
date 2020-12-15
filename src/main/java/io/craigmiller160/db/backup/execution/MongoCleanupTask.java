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

import io.craigmiller160.db.backup.properties.PropertyStore;
import io.vavr.collection.Stream;
import io.vavr.control.Try;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class MongoCleanupTask  implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(MongoCleanupTask.class);

    private final PropertyStore propStore;
    private final String database;

    public MongoCleanupTask(final PropertyStore propStore,
                            final String database) {
        this.propStore = propStore;
        this.database = database;
    }

    @Override
    public void run() {
        log.info("Running cleanup for MongoDB Database {}", database);

        final var targetDir = Paths.get(propStore.getOutputRootDirectory(), BackupConstants.MONGO_DIR, database);
        if (!Files.exists(targetDir)) {
            log.info("Directory to cleanup MongoDB files does not exist: {}", targetDir.toAbsolutePath().toString());
            return;
        }

        final var oldestAllowed = ZonedDateTime.now(ZoneId.of(BackupConstants.TIME_ZONE))
                .minusDays(propStore.getOutputCleanupAgeDays());

        Try.of(() ->
                Stream.ofAll(Files.list(targetDir))
                .filter(path -> {
                    final var fileName = path.getFileName().toString();
                    final var timestamp = LocalDateTime.parse(fileName, BackupConstants.FORMAT).atZone(ZoneId.of(BackupConstants.TIME_ZONE));
                    return oldestAllowed.compareTo(timestamp) > 0;
                })
                .foldLeft(new CleanupResult(0, 0), (result, path) ->
                        Try.of(() -> {
                            FileUtils.deleteDirectory(path.toFile());
                            return path;
                        })
                                .map(p -> new CleanupResult(result.successCount() + 1, result.failureCount()))
                                .recoverWith(ex -> {
                                    log.debug(String.format("Failed to cleanup MongoDB directory %s for Database %s", path, database), ex);
                                    return Try.success(new CleanupResult(result.successCount(), result.failureCount() + 1));
                                })
                                .get()
                )
        )
                .onSuccess(result -> log.info("Finished cleaning up MongoDB Database {}. Success: {} Failure: {}", database, result.successCount(), result.failureCount()))
                .onFailure(ex -> log.error(String.format("Error attempting to cleanup MongoDB Database %s", database), ex));
    }

}
