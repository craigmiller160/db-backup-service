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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.net.ssl.SSLSession;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    private static final String EMAIL_TO = "craig@gmail.com";
    private static final String EMAIL_HOST = "https://localhost:7100";
    private static final String AUTH_HOST = "https://localhost:7003";
    private static final String AUTH_EMAIL_CLIENT_KEY = "ABC";
    private static final String AUTH_EMAIL_CLIENT_SECRET = "DEF";
    private static final String ACCESS_TOKEN = "accessToken";
    private static final String USER = "user";
    private static final String PASSWORD = "password";
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
        properties.setProperty(PropertyStore.EMAIL_AUTH_USER, USER);
        properties.setProperty(PropertyStore.EMAIL_AUTH_PASSWORD, PASSWORD);
        propStore = new PropertyStore(properties);
        emailService = new TestEmailService(propStore, () -> httpClient);
    }

    @Test
    public void test_sendPostgresErrorAlertEmail() throws Exception {
        final var tokenResponseDto = new TokenResponse(ACCESS_TOKEN, "", "");
        final var tokenResponse = new TestHttpResponse(200, objectMapper.writeValueAsString(tokenResponseDto));
        final var emailResponse = new TestHttpResponse(204, "");
        final var tokenRequest = String.format("grant_type=password&username=%s&password=%s", USER, PASSWORD);

        final var emailText = String.format("%s%n%s",
                EmailService.POSTGRES_ERROR_ALERT_MESSAGE.formatted(DATABASE, SCHEMA),
                EmailService.GENERIC_MESSAGE.formatted(NOW.format(EmailService.FORMATTER), String.format("%s - %s", EXCEPTION.getClass().getName(), EXCEPTION.getMessage()))
        );
        final var emailRequestDto = new EmailRequest(
                List.of(EMAIL_TO),
                Collections.emptyList(),
                Collections.emptyList(),
                EmailService.ERROR_ALERT_SUBJECT,
                emailText
        );
        final var emailRequest = objectMapper.writeValueAsString(emailRequestDto);

        when(httpClient.send(any(), any()))
                .thenReturn(tokenResponse)
                .thenReturn(emailResponse);

        emailService.sendPostgresErrorAlertEmail(DATABASE, SCHEMA, EXCEPTION);

        final var requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);

        verify(httpClient, times(2))
                .send(requestCaptor.capture(), any());
        assertEquals(2, requestCaptor.getAllValues().size());
        testHttpRequest(requestCaptor.getAllValues().get(0), URI.create(String.format("%s%s", AUTH_HOST, EmailService.TOKEN_URI)), tokenRequest);
        testHttpRequest(requestCaptor.getAllValues().get(1), URI.create(String.format("%s%s", EMAIL_HOST, EmailService.EMAIL_URI)), emailRequest);
    }

    @Test
    public void test_sendMongoErrorAlertEmail() {
        throw new RuntimeException();
    }

    private void testHttpRequest(final HttpRequest request, final URI expectedUri, final String expectedBody) {
        assertEquals(expectedUri, request.uri());
        assertEquals("POST", request.method());
        var publisher = request.bodyPublisher().get();

        final var subscriber = new TestSubscriber<>();
        publisher.subscribe(subscriber);
        final var contentArray = ((ByteBuffer) subscriber.getBodyItems().get(0)).array();
        final var textContent = new String(contentArray);

        assertEquals(expectedBody, textContent);
    }

    private static class TestSubscriber<T> implements Flow.Subscriber<T> {

        private final List<T> bodyItems = new ArrayList<>();
        private final CountDownLatch latch = new CountDownLatch(1);

        public List<T> getBodyItems() {
            try {
                latch.await(5000L, TimeUnit.MILLISECONDS);
                return bodyItems;
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public void onSubscribe(final Flow.Subscription subscription) {
            subscription.request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(final T item) {
            bodyItems.add(item);
        }

        @Override
        public void onError(final Throwable throwable) {
            throwable.printStackTrace();
            latch.countDown();;
        }

        @Override
        public void onComplete() {
            latch.countDown();
        }
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
