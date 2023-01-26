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

import io.craigmiller160.db.backup.email.EmailService;
import io.craigmiller160.db.backup.properties.PropertyStore;

public class TaskFactory {

  public Runnable createPostgresBackupTask(
      final PropertyStore propStore,
      final EmailService emailService,
      final String database,
      final String schema) {
    return new PostgresBackupTask(propStore, database, schema, emailService);
  }

  public Runnable createLivenessCheckTask(final PropertyStore propStore) {
    return new LivenessCheckTask(propStore);
  }

  public Runnable createPostgresCleanupTask(
      final PropertyStore propStore, final String database, final String schema) {
    return new PostgresCleanupTask(propStore, database, schema);
  }

  public Runnable createMongoBackupTask(
      final PropertyStore propStore, final EmailService emailService, final String database) {
    return new MongoBackupTask(propStore, database, emailService);
  }

  public Runnable createMongoCleanupTask(final PropertyStore propStore, final String database) {
    return new MongoCleanupTask(propStore, database);
  }
}
