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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.craigmiller160.db.backup.properties.PropertyStore;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private static final String EMAIL_URI = "/email";
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
    private final ObjectMapper objectMapper;

    public EmailService(final PropertyStore propStore) {
        this.propStore = propStore;
        this.objectMapper = new ObjectMapper();
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

        final var emailRequest = new EmailRequest(
                List.of(propStore.getEmailTo()),
                Collections.emptyList(),
                Collections.emptyList(),
                ERROR_ALERT_SUBJECT,
                emailText
        );

        Try.run(() -> {
            final var jsonBody = objectMapper.writeValueAsString(emailRequest);
            final var httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(String.format("%s%s", propStore.getEmailHost(), EMAIL_URI)))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .header("Content-Type", "application/json")
                    .build();

            var response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() >= 400) {
                // TODO throw special exception
            }
        })
                .onSuccess((v) -> log.info("Successfully sent error alert email for Database {} and Schema {}", database, schema))
                .onFailure(ex2 -> log.error(String.format("Error sending error alert email for Database %s and Schema %s", database, schema), ex2));
    }

}
