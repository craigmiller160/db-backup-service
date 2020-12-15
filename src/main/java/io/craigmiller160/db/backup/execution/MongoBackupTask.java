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
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;

public class MongoBackupTask extends AbstractBackupTask {

    private static final Logger log = LoggerFactory.getLogger(MongoBackupTask.class);

    /*
     * TODO I need:
     *  - host
     *  - port
     *  - username
     *  - password
     *  - database
     *  - auth database
     */

    private static final String OUTPUT_PATH_ARG = "-o";
    private static final String MONGODUMP_PATH = "/mongotools/mongodump";
    private static final String URI_TEMPLATE = "--uri=\"mongodb://%s:%s@%s:%d/%s?authSource=%s\"";
    private static final String MONGO_DIR = "MongoDB";

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
        log.info("Running MongoDB backup for Database {}", database);

        final var host = propStore.getMongoHost();
        final var port = propStore.getMongoPort();
        final var user = propStore.getMongoUser();
        final var password = propStore.getMongoPassword();
        final var authDb = propStore.getMongoAuthDb();

        final var uriArg = String.format(URI_TEMPLATE, user, password, host, port, database, authDb);
        final var timestamp = BackupConstants.FORMAT.format(ZonedDateTime.now(ZoneId.of(BackupConstants.TIME_ZONE)));
        final var outputPath = Paths.get(propStore.getOutputRootDirectory(), MONGO_DIR, database, timestamp);

        final var command = new String[] {
                MONGODUMP_PATH,
                uriArg,
                OUTPUT_PATH_ARG,
                outputPath.toString()
        };
        final var environment = new HashMap<String,String>();

        Try.of(() -> processProvider.provide(command, environment))
                .flatMap(this::readProcess)
                .onSuccess(content -> log.info("Successfully wrote MongoDB backup for Database {} to directory {}", database, outputPath.toString()))
                .onFailure(ex -> {
                    log.error(String.format("Error running MongoDB backup for Database %s", database), ex);
                    emailService.sendMongoErrorAlertEmail(database, ex);
                });
    }

}
