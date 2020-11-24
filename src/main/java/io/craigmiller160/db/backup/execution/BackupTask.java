package io.craigmiller160.db.backup.execution;

import io.craigmiller160.db.backup.properties.PropertyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class BackupTask implements Runnable {

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
                "pg_dump",
                database,
                "-n",
                schema,
                "-h",
                propStore.getPostgresHost(),
                "-p",
                propStore.getPostgresPort(),
                "-U",
                propStore.getPostgresUser()
        };
        final var environment = Map.of("PGPASSWORD", propStore.getPostgresPassword());
        final var process = processProvider.provide(command, environment);
    }

}
