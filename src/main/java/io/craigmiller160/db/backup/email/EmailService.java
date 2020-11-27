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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.craigmiller160.db.backup.exception.HttpResponseException;
import io.craigmiller160.db.backup.properties.PropertyStore;
import io.vavr.Tuple;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    public static final String TOKEN_URI = "/oauth/token";
    public static final String EMAIL_URI = "/email";
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final String ERROR_ALERT_SUBJECT = "ERROR ALERT - Database Backup Failed";
    public static final String ERROR_ALERT_MESSAGE = """
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
        this (propStore, () -> createHttpClient(propStore));
    }

    public EmailService(final PropertyStore propStore, final Supplier<HttpClient> clientSupplier) {
        this.propStore = propStore;
        this.objectMapper = new ObjectMapper();
        this.httpClient = clientSupplier.get();
    }

    private static HttpClient createHttpClient(final PropertyStore propStore) {
        return Try.of(() -> {
            System.setProperty("jdk.internal.httpclient.disableHostnameVerification", Boolean.TRUE.toString());
            final var sslContext = SSLContext.getDefault();
            return HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .connectTimeout(Duration.ofSeconds(propStore.getEmailConnectTimeoutSecs()))
                    .sslContext(sslContext)
                    .build();
        })
                .recoverWith(ex -> Try.failure(new RuntimeException("Error creating HttpClient", ex)))
                .get();
    }

    public void sendErrorAlertEmail(final String database, final String schema, final Throwable ex) {
        getAccessToken()
                .flatMap(accessToken -> {
                    final var timestamp = getNowEastern().format(FORMATTER);
                    final var errorMessage = String.format("%s - %s", ex.getClass().getName(), ex.getMessage());
                    final var emailText = ERROR_ALERT_MESSAGE.formatted(database, schema, timestamp, errorMessage);

                    final var emailRequest = new EmailRequest(
                            List.of(propStore.getEmailTo()),
                            Collections.emptyList(),
                            Collections.emptyList(),
                            ERROR_ALERT_SUBJECT,
                            emailText
                    );

                    return Try.of(() -> objectMapper.writeValueAsString(emailRequest))
                            .map(jsonBody -> Tuple.of(accessToken, jsonBody));
                })
                .flatMap(tuple -> {
                    final var accessToken = tuple._1;
                    final var jsonBody = tuple._2;

                    final var httpRequest = HttpRequest.newBuilder()
                            .uri(URI.create(String.format("%s%s", propStore.getEmailHost(), EMAIL_URI)))
                            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                            .header("Content-Type", "application/json")
                            .header("Authorization", String.format("Bearer %s", accessToken))
                            .build();

                    return Try.of(() -> httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString()))
                            .flatMap(response -> {
                                if (response.statusCode() >= 400) {
                                    return Try.failure(new HttpResponseException("Error sending email request", response.statusCode(), response.body()));
                                }
                                return Try.success("");
                            });
                })
                .onSuccess((v) -> log.info("Successfully sent error alert email for Database {} and Schema {}", database, schema))
                .onFailure(ex2 -> log.error(String.format("Error sending error alert email for Database %s and Schema %s", database, schema), ex2));
    }

    protected ZonedDateTime getNowEastern() {
        return ZonedDateTime.now(ZoneId.of("US/Eastern"));
    }

    private Try<String> getAccessToken() {
        return Try.of(() -> {
            final var rawBasicAuth = String.format("%s:%s", propStore.getEmailAuthClientKey(), propStore.getEmailAuthClientSecret());
            final var encodedBasicAuth = Base64.getEncoder().encodeToString(rawBasicAuth.getBytes());
            final var formBody = String.format("grant_type=password&username=%s&password=%s", propStore.getEmailAuthUser(), propStore.getEmailAuthPassword());
            final var httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(String.format("%s%s", propStore.getEmailAuthHost(), TOKEN_URI)))
                    .POST(HttpRequest.BodyPublishers.ofString(formBody))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .headers("Authorization", String.format("Basic %s", encodedBasicAuth))
                    .build();

            final var response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new HttpResponseException("Error getting access token", response.statusCode(), response.body());
            }

            final var tokenResponse = objectMapper.readValue(response.body(), TokenResponse.class);
            return tokenResponse.accessToken();
        });
    }

}
