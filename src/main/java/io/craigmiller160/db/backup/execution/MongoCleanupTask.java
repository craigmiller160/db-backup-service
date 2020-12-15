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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Paths;
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

        final var targetDir = Paths.get(propStore.getOutputRootDirectory(), MongoBackupTask.MONGO_DIR, database);
        if (!Files.exists(targetDir)) {
            log.info("Directory to cleanup MongoDB files does not exist: {}", targetDir.toAbsolutePath().toString());
            return;
        }

        final var oldestAllowed = ZonedDateTime.now(ZoneId.of(BackupConstants.TIME_ZONE))
                .minusDays(propStore.getOutputCleanupAgeDays());

        // TODO finish this
    }

}
