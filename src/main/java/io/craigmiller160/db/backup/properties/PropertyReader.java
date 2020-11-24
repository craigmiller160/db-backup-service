package io.craigmiller160.db.backup.properties;

import io.craigmiller160.db.backup.exception.PropertyException;

import java.io.IOException;
import java.util.Properties;

public class PropertyReader {

    private static final String PROPERTIES_PATH = "application.properties";

    public PropertyStore readProperties() throws PropertyException {
        try (final var propStream = PropertyReader.class.getClassLoader().getResourceAsStream(PROPERTIES_PATH)) {
            final Properties props = new Properties();
            props.load(propStream);
            return new PropertyStore(props);
        } catch (final IOException ex) {
            throw new PropertyException("Error reading properties file", ex);
        }
    }

}
