package io.craigmiller160.db.backup.properties;

import io.craigmiller160.db.backup.exception.PropertyException;
import io.vavr.control.Try;

import java.util.Properties;

public class PropertyReader {

    private static final String PROPERTIES_PATH = "application.properties";

    public Try<PropertyStore> readProperties() {
        return Try.withResources(() -> PropertyReader.class.getClassLoader().getResourceAsStream(PROPERTIES_PATH))
                .of(propStream -> {
                    final Properties props = new Properties();
                    props.load(propStream);
                    return props;
                })
                .map(props -> {
                    System.getenv().forEach(props::setProperty);
                    return props;
                })
                .map(PropertyStore::new)
                .recoverWith(ex -> Try.failure(new PropertyException("Error reading properties file", ex)));
    }

}
