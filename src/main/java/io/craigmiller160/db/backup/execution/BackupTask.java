package io.craigmiller160.db.backup.execution;

import io.craigmiller160.db.backup.properties.PropertyStore;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
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
        final var environment = Map.of("PGPASSWORD", propStore.getPostgresPassword());
        Try.of(() -> processProvider.provide(command, environment))
                .flatMap(this::readOutput)
                .flatMap(this::writeToFile)
                .onSuccess(filePath -> log.info("Successfully write backup for Database {} and Schema {} to File {}", database, schema, filePath))
                .onFailure(ex -> log.error(String.format("Error running backup for Database %s and Schema %s", database, schema), ex));
    }

    private Try<String> readOutput(final Process process) {
        return Try.withResources(() -> new BufferedReader(new InputStreamReader(process.getInputStream())))
                .of(reader -> reader.lines().collect(Collectors.joining("\n")));
    }

    private Try<String> writeToFile(final String dbBackupText) {
        // TODO finish this, return file path
        return null;
    }

}
