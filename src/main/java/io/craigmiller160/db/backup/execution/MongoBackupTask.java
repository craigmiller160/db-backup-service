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

public class MongoBackupTask extends AbstractBackupTask {

    /*
     * TODO I need:
     *  - host
     *  - port
     *  - username
     *  - password
     *  - database
     *  - auth database
     */

    private static final String MONGODUMP_PATH = "/mongotools/mongodump";
    private static final String URI_TEMPLATE = "--uri=\"mongodb://%s:%s@%s:%d/%s?authSource=%s\"";

    private final String database;

    // TODO unify as much of this as possible with PostgresBackupTask by creating an abstract parent class

    public MongoBackupTask(final PropertyStore propStore,
                           final String database,
                           final ProcessProvider processProvider,
                           final EmailService emailService) {
        super(propStore, processProvider, emailService);
        this.database = database;
    }

    public MongoBackupTask(final PropertyStore propStore,
                           final String database,
                           final EmailService emailService) {
        this (propStore, database, ProcessProvider.DEFAULT, emailService);
    }

    @Override
    public void run() {
        // TODO finish this
    }

}
