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

import io.craigmiller160.db.backup.exception.BackupException;
import io.craigmiller160.db.backup.properties.PropertyStore;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Collectors;

public class BackupTask implements Runnable {

    private static final String PG_DUMP_CMD = "pg_dump";
    private static final String HOST_ARG = "-h";
    private static final String SCHEMA_ARG = "-n";
    private static final String PORT_ARG = "-p";
    private static final String USER_ARG = "-U";
    private static final String USE_INSERT_STATEMENTS = "--column-inserts";

    private static final Logger log = LoggerFactory.getLogger(BackupTask.class);
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String TIME_ZONE = "US/Eastern";
    private static final String PASSWORD_ENV = "PGPASSWORD";

    private final PropertyStore propStore;
    private final String database;
    private final String schema;
    private final ProcessProvider processProvider;

    public BackupTask(final PropertyStore propStore,
                      final String database,
                      final String schema,
                      final ProcessProvider processProvider) {
        this.propStore = propStore;
        this.database = database;
        this.schema = schema;
        this.processProvider = processProvider;
    }

    public BackupTask(final PropertyStore propStore,
                      final String database,
                      final String schema) {
        this (propStore, database, schema, ProcessProvider.DEFAULT);
    }

    @Override
    public void run() {
        log.info("Running backup for Database {} and Schema {}", database, schema);

        final var command = new String[] {
                PG_DUMP_CMD,
                database,
                SCHEMA_ARG,
                schema,
                HOST_ARG,
                propStore.getPostgresHost(),
                PORT_ARG,
                propStore.getPostgresPort(),
                USER_ARG,
                propStore.getPostgresUser(),
                USE_INSERT_STATEMENTS
        };
        final var environment = Map.of(PASSWORD_ENV, propStore.getPostgresPassword());
        Try.of(() -> processProvider.provide(command, environment))
                .flatMap(this::readOutput)
                .flatMap(this::writeToFile)
                .onSuccess(filePath -> log.info("Successfully wrote backup for Database {} and Schema {} to File {}", database, schema, filePath))
                .onFailure(ex -> log.error(String.format("Error running backup for Database %s and Schema %s", database, schema), ex));
    }

    private Try<String> readOutput(final Process process) {
        return Try.withResources(() -> new BufferedReader(new InputStreamReader(process.getInputStream())))
                .of(reader -> reader.lines().collect(Collectors.joining("\n")));
    }

    private Try<String> writeToFile(final String dbBackupText) {
        final var outputRootDir = new File(propStore.getOutputRootDirectory());
        final var dbOutputDir = new File(outputRootDir, database);

        if (!dbOutputDir.exists() && !dbOutputDir.mkdirs()) {
            return Try.failure(new BackupException(String.format("Unable to create output directory: %s", dbOutputDir.getAbsolutePath())));
        }

        final var timestamp = FORMAT.format(ZonedDateTime.now(ZoneId.of(TIME_ZONE)));
        final var outputFile = new File(dbOutputDir, String.format("backup_%s.sql", timestamp));

        return Try.withResources(() -> new FileWriter(outputFile))
                .of(writer -> {
                    writer.write(dbBackupText);
                    return writer;
                })
                .map(writer -> outputFile.getAbsolutePath())
                .recoverWith(ex -> Try.failure(
                        new BackupException(String.format("Error writing backup data for Database %s and Schema %s to File %s", database, schema, outputFile.getAbsolutePath()), ex)
                ));
    }

}
