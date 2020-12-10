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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class CleanupTask implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(CleanupTask.class);

    private final PropertyStore propStore;
    private final String database;
    private final String schema;

    public CleanupTask(final PropertyStore propStore,
                       final String database,
                       final String schema) {
        this.propStore = propStore;
        this.database = database;
        this.schema = schema;
    }

    @Override
    public void run() {
        log.info("Running cleanup for Database {} and Schema {}", database, schema);

        final var schemaOutputDir = Paths.get(propStore.getOutputRootDirectory(), database, schema);
        if (!Files.exists(schemaOutputDir)) {
            log.info("Directory to cleanup does not exist: {}", schemaOutputDir.toAbsolutePath().toString());
            return;
        }

        final var oldestAllowed = ZonedDateTime.now(ZoneId.of(BackupConstants.TIME_ZONE))
                .minusDays(propStore.getOutputCleanupAgeDays());

        Try.of(() -> {
            return Stream.ofAll(Files.list(schemaOutputDir))
                    .map(path -> path.getFileName().toString())
                    .filter(path -> {
                        final var timestampString = path.replace("backup_", "").replace(".sql", "");
                        final var timestamp = LocalDateTime.parse(timestampString, BackupConstants.FORMAT).atZone(ZoneId.of(BackupConstants.TIME_ZONE));

                        return oldestAllowed.compareTo(timestamp) > 0;
                    })
                    .toList();
        })
                .onSuccess(filesToDelete -> {
                    filesToDelete.forEach(fileName -> {
                        Try.run(() -> {
                            final var fullPath = Path.of(schemaOutputDir.toString(), fileName);
                            Files.delete(fullPath);
                        })
                                .onFailure(ex -> log.error(String.format("Error deleting file")));
                    });
                })


//        filesToDelete.forEach(fileName -> {
//            final var fullPath = Path.of(schemaOutputDir.toString(), fileName);
//            Files.delete(fullPath);
//        });
//
//        log.info("Cleaned up {} files for Database {} and Schema {}", filesToDelete.size(), database, schema);
    }

}
