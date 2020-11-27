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
import io.craigmiller160.db.backup.properties.PropertyStore;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.net.ssl.SSLSession;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    private static final String EMAIL_TO = "craig@gmail.com";
    private static final String EMAIL_HOST = "https://localhost:7100";
    private static final String AUTH_HOST = "https://localhost:7003";
    private static final String AUTH_EMAIL_CLIENT_KEY = "ABC";
    private static final String AUTH_EMAIL_CLIENT_SECRET = "DEF";
    private static final String ACCESS_TOKEN = "accessToken";
    private static final String DATABASE = "database";
    private static final String SCHEMA = "schema";
    private static final Exception EXCEPTION = new Exception("Dying");
    private static final ZonedDateTime NOW = ZonedDateTime.of(2020, 1, 1, 1, 1, 1, 1, ZoneId.of("US/Eastern"));

    private final ObjectMapper objectMapper = new ObjectMapper();
    private PropertyStore propStore;
    @Mock
    private HttpClient httpClient;
    private EmailService emailService;

    @BeforeEach
    public void beforeEach() {
        final var properties = new Properties();
        properties.setProperty(PropertyStore.EMAIL_CONNECT_TIMEOUT_SECS, "30");
        properties.setProperty(PropertyStore.EMAIL_TO, EMAIL_TO);
        properties.setProperty(PropertyStore.EMAIL_HOST, EMAIL_HOST);
        properties.setProperty(PropertyStore.EMAIL_AUTH_HOST, AUTH_HOST);
        properties.setProperty(PropertyStore.EMAIL_AUTH_CLIENT_KEY, AUTH_EMAIL_CLIENT_KEY);
        properties.setProperty(PropertyStore.EMAIL_AUTH_CLIENT_SECRET, AUTH_EMAIL_CLIENT_SECRET);
        propStore = new PropertyStore(properties);
        emailService = new TestEmailService(propStore, () -> httpClient);
    }

    @Test
    public void test_sendErrorAlertEmail() throws Exception {
        emailService.sendErrorAlertEmail(DATABASE, SCHEMA, EXCEPTION);

        final var tokenResponseDto = new TokenResponse(ACCESS_TOKEN, "", "");
        final var tokenResponse = new TestHttpResponse(200, objectMapper.writeValueAsString(tokenResponseDto));

        when(httpClient.send(any(), any()))
                .thenReturn(tokenResponse);
    }

    private static class TestEmailService extends EmailService {
        public TestEmailService(final PropertyStore propStore, final Supplier<HttpClient> clientSupplier) {
            super (propStore, clientSupplier);
        }

        @Override
        protected ZonedDateTime getNowEastern() {
            return NOW;
        }
    }

    private static class TestHttpResponse implements HttpResponse<Object> {

        private final int statusCode;
        private final Object body;

        public TestHttpResponse(final int statusCode, final Object body) {
            this.statusCode = statusCode;
            this.body = body;
        }

        @Override
        public int statusCode() {
            return statusCode;
        }

        @Override
        public HttpRequest request() {
            throw new NotImplementedException("Method not implemented");
        }

        @Override
        public Optional<HttpResponse<Object>> previousResponse() {
            throw new NotImplementedException("Method not implemented");
        }

        @Override
        public HttpHeaders headers() {
            throw new NotImplementedException("Method not implemented");
        }

        @Override
        public Object body() {
            return body;
        }

        @Override
        public Optional<SSLSession> sslSession() {
            throw new NotImplementedException("Method not implemented");
        }

        @Override
        public URI uri() {
            throw new NotImplementedException("Method not implemented");
        }

        @Override
        public HttpClient.Version version() {
            throw new NotImplementedException("Method not implemented");
        }
    }

}
