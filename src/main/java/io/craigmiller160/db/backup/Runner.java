package io.craigmiller160.db.backup;

import io.craigmiller160.db.backup.properties.PropertyReader;

public class Runner {

    public static void main(final String[] args) {
        try {
            final var propReader = new PropertyReader();
            final var propStore = propReader.readProperties();
        } catch (final Exception ex) {
            ex.printStackTrace(); // TODO handle this better
        }
    }

}
