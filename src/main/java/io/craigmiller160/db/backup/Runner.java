package io.craigmiller160.db.backup;

import io.craigmiller160.db.backup.properties.PropertyReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Runner {

    public static void main(final String[] args) {
        final Logger log = LoggerFactory.getLogger("io.craigmiller160.Runner");
        log.info("Hello World");

        try {
            final var propReader = new PropertyReader();
            final var propStore = propReader.readProperties();
        } catch (final Exception ex) {
            ex.printStackTrace(); // TODO handle this better
        }
    }

}
