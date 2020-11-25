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

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

public class LivenessCheckTask implements Runnable {

    private static final String LIVENESS_SCRIPT = "liveness.sh";

    private final AtomicLong livenessTimestamp;

    public LivenessCheckTask(final AtomicLong livenessTimestamp) {
        this.livenessTimestamp = livenessTimestamp;
    }

    @Override
    public void run() {
//        timestampConsumer.accept(System.currentTimeMillis());
    }

}
