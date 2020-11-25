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

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.atomic.AtomicLong;

public class LivenessCheckTask implements Runnable {

    // TODO need to be able to handle the root directory for unit tests

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
    private final AtomicLong livenessTimestamp; // TODO this one might not be necessary anymore

    public LivenessCheckTask(final PropertyStore propStore, final AtomicLong livenessTimestamp) {
        this.propStore = propStore;
        this.livenessTimestamp = livenessTimestamp;
    }

    @Override
    public void run() {
        log.info("Updating liveness check script");

        final var maxTimestamp = ZonedDateTime.now(ZoneId.of("UTC"))
                .plusSeconds(propStore.getExecutorIntervalSecs() * 2)
                .toInstant()
                .toEpochMilli();

        final var script = LIVENESS_SCRIPT_TEMPLATE.formatted(maxTimestamp);
        System.out.println(script); // TODO delete this
    }

}
