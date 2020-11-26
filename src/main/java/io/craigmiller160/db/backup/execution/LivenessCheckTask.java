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
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.atomic.AtomicLong;

public class LivenessCheckTask implements Runnable {

    // TODO needs to be added to unit tests... somehow

    private static final Logger log = LoggerFactory.getLogger(LivenessCheckTask.class);

    private static final String LIVENESS_SCRIPT_FILE = "liveness.sh";
    private static final String LIVENESS_SCRIPT_TEMPLATE = """
            #!/bin/sh
            
            current_timestamp=$(date +%%s%%3N)
            max_timestamp=%d
            
            if [ $current_timestamp -gt $max_timestamp ]; then
                exit 1
            else
                exit 0
            fi
            """;

    private final PropertyStore propStore;

    public LivenessCheckTask(final PropertyStore propStore) {
        this.propStore = propStore;
    }

    @Override
    public void run() {
        log.debug("Updating liveness check script");

        final var maxTimestamp = ZonedDateTime.now(ZoneId.of("UTC"))
                .plusSeconds(propStore.getExecutorIntervalSecs() * 2)
                .toInstant()
                .toEpochMilli();

        final var script = LIVENESS_SCRIPT_TEMPLATE.formatted(maxTimestamp);
        final var outputDir = new File(propStore.getOutputRootDirectory());
        final var livenessFile = new File(outputDir, LIVENESS_SCRIPT_FILE);

        Try.withResources(() -> new FileWriter(livenessFile))
                .of(writer -> {
                    writer.write(script);
                    return writer;
                })
                .onSuccess(writer -> log.info("Successfully updated liveness check script"))
                .onFailure(ex -> log.error("Error updating liveness check script", ex));
    }

}
