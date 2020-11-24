package io.craigmiller160.db.backup.properties;

import io.craigmiller160.db.backup.exception.PropertyException;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class PropertyReader {

    private static final Logger log = LoggerFactory.getLogger(PropertyReader.class);
    private static final String PROPERTIES_PATH = "application.properties";

    public Try<PropertyStore> readProperties() {
        log.info("Reading properties");
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
