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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.HttpClient;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Properties;
import java.util.function.Supplier;

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
    public void test_sendErrorAlertEmail() {
        emailService.sendErrorAlertEmail(DATABASE, SCHEMA, EXCEPTION);
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

}
