package io.craigmiller160.db.backup;

import io.craigmiller160.db.backup.config.ConfigReader;
import io.craigmiller160.db.backup.execution.BackupScheduler;
import io.craigmiller160.db.backup.properties.PropertyReader;
import io.vavr.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);
    private static final Object BACKUP_SCHEDULER_LOG = new Object();

    private final PropertyReader propReader;
    private final ConfigReader configReader;
    private BackupScheduler backupScheduler;

    public Application() {
        this.propReader = new PropertyReader();
        this.configReader = new ConfigReader();
    }

    public void start() {
        log.info("Starting application");
        propReader.readProperties()
                .map(propStore ->
                        configReader.readBackupConfig()
                                .map(config -> Tuple.of(propStore, config))
                )
                .flatMap(tupleTry -> tupleTry)
                .onSuccess(tuple -> {
                    synchronized (BACKUP_SCHEDULER_LOG) {
                        backupScheduler = new BackupScheduler(tuple._1, tuple._2);
                    }
                })
                .onFailure(ex -> log.error("Error starting application", ex));
    }

    public void stop() {
        // TODO probably need some kind of graceful shutdown here
    }

}
