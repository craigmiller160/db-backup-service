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

package io.craigmiller160.db.backup.email;

import io.craigmiller160.db.backup.properties.PropertyStore;

import java.net.http.HttpClient;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class EmailService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String ERROR_ALERT_SUBJECT = "ERROR ALERT - Database Backup Failed";
    private static final String ERROR_ALERT_MESSAGE = """
            Database Backup Failed
            
            Database: %s
            Schema: %s
            Timestamp: %s
            Error Message: %s
            """;

    private final PropertyStore propStore;
    private final HttpClient httpClient;

    public EmailService(final PropertyStore propStore) {
        this.propStore = propStore;
        httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(propStore.getEmailConnectTimeoutSecs()))
                .build();
    }

    public void sendErrorAlertEmail(final String database, final String schema, final Throwable ex) {
        final var timestamp = ZonedDateTime.now(ZoneId.of("US/Eastern")).format(FORMATTER);
        final var errorMessage = String.format("%s - %s", ex.getClass().getName(), ex.getMessage());
        final var emailText = ERROR_ALERT_MESSAGE.formatted(database, schema, timestamp, errorMessage);

        // TODO send this
    }

}
