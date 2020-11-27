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
import java.util.Properties;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private PropertyStore propStore;
    @Mock
    private HttpClient httpClient;
    private EmailService emailService;

    @BeforeEach
    public void beforeEach() {
        final var properties = new Properties();
        propStore = new PropertyStore(properties);
        emailService = new EmailService(propStore, () -> httpClient);
    }

    @Test
    public void test_sendErrorAlertEmail() {
        throw new RuntimeException();
    }

}
