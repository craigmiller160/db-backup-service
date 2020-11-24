package io.craigmiller160.db.backup.execution;

import io.craigmiller160.db.backup.config.dto.BackupConfig;
import io.craigmiller160.db.backup.properties.PropertyStore;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class BackupScheduler {

    private final PropertyStore propStore;
    private final BackupConfig backupConfig;
    private final ScheduledExecutorService executor;

    public BackupScheduler(final PropertyStore propStore, final BackupConfig backupConfig) {
        this.propStore = propStore;
        this.backupConfig = backupConfig;
        this.executor = Executors.newScheduledThreadPool(propStore.getExecutorThreadCount());
    }

    public void shutdown() {
        this.executor.shutdown();
    }

}
