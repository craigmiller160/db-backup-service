package io.craigmiller160.db.backup;

import io.craigmiller160.db.backup.config.ConfigReader;
import io.craigmiller160.db.backup.properties.PropertyReader;
import io.vavr.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    private final PropertyReader propReader;
    private final ConfigReader configReader;

    public Application() {
        this.propReader = new PropertyReader();
        this.configReader = new ConfigReader();
    }

    public void start() {
        propReader.readProperties()
                .map(propStore ->
                        configReader.readBackupConfig()
                                .map(config -> Tuple.of(propStore, config))
                )
                .flatMap(tupleTry -> tupleTry);
    }

}
