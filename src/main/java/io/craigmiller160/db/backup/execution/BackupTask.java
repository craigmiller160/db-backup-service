package io.craigmiller160.db.backup.execution;

import io.craigmiller160.db.backup.properties.PropertyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackupTask implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(BackupTask.class);

    private final PropertyStore propStore;
    private final String database;
    private final String schema;

    public BackupTask(final PropertyStore propStore, final String database, final String schema) {
        this.propStore = propStore;
        this.database = database;
        this.schema = schema;
    }

    @Override
    public void run() {
        log.info("Running backup for Database {} and Schema {}", database, schema);
    }

}
