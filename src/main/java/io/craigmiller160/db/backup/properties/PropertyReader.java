/*
 *     db-backup-service
 *     Copyright (C) 2020 Craig Miller
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
                .flatMap(propStore -> propStore.validateProperties().map(result -> propStore))
                .recoverWith(ex -> Try.failure(new PropertyException("Error reading properties file", ex)));
    }

}
