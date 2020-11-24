package io.craigmiller160.db.backup.execution;

import io.craigmiller160.db.backup.properties.PropertyStore;

public class BackupTask implements Runnable {

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

    }

}
