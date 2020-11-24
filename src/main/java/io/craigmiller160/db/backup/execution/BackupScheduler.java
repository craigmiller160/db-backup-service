package io.craigmiller160.db.backup.execution;

import io.craigmiller160.db.backup.config.dto.BackupConfig;
import io.craigmiller160.db.backup.properties.PropertyStore;
import io.vavr.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BackupScheduler {

    private static final Logger log = LoggerFactory.getLogger(BackupScheduler.class);

    private final PropertyStore propStore;
    private final BackupConfig backupConfig;
    private final ScheduledExecutorService executor;

    public BackupScheduler(final PropertyStore propStore, final BackupConfig backupConfig) {
        this.propStore = propStore;
        this.backupConfig = backupConfig;
        this.executor = Executors.newScheduledThreadPool(propStore.getExecutorThreadCount());
    }

    public void start() {
        backupConfig.databases()
                .stream()
                .flatMap(db ->
                        db.schemas()
                                .stream()
                                .map(schema -> Tuple.of(db.name(), schema))
                )
                .map(tuple -> new BackupTask(propStore, tuple._1, tuple._2))
                .forEach(task -> executor.scheduleAtFixedRate(task, 0, propStore.getExecutorIntervalSecs(), TimeUnit.SECONDS));
    }

    public void stop() {
        this.executor.shutdown();
    }

}
