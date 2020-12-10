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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

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

        try {
            Files.list(schemaOutputDir)
                    .forEach(System.out::println);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
