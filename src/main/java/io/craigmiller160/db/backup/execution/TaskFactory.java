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

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class TaskFactory {

    public Runnable createBackupTask(final PropertyStore propStore, final String database, final String schema) {
        return new BackupTask(propStore, database, schema);
    }

    public Runnable createLivenessCheckTask(final AtomicLong livenessTimestamp) {
        return new LivenessCheckTask(livenessTimestamp);
    }

}
