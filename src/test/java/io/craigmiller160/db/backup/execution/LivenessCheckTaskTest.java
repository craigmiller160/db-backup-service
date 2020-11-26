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

package io.craigmiller160.db.backup.execution;

import io.craigmiller160.db.backup.properties.PropertyStore;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Properties;

public class LivenessCheckTaskTest {

    private static final ZonedDateTime TEST_NOW = ZonedDateTime.of(2020, 1, 1, 1, 1, 1, 1, ZoneId.of("UTC"));
    private static final String OUTPUT_ROOT = String.format("%s/%s", System.getProperty("user.dir"), "target/output");
    private static final String INTERVAL_SECS = "30";

    private LivenessCheckTask livenessCheckTask;

    @BeforeEach
    public void beforeEach() throws Exception {
        FileUtils.deleteDirectory(new File(OUTPUT_ROOT));

        final var properties = new Properties();
        properties.setProperty(PropertyStore.OUTPUT_ROOT_DIR, OUTPUT_ROOT);
        properties.setProperty(PropertyStore.EXECUTOR_INTERVAL_SECS, INTERVAL_SECS);
        final var propStore = new PropertyStore(properties);

        livenessCheckTask = new TestLivenessCheckTask(propStore);
    }

    @Test
    public void test_run() {
        livenessCheckTask.run();
    }

    private static class TestLivenessCheckTask extends LivenessCheckTask {
        public TestLivenessCheckTask(final PropertyStore propStore) {
            super(propStore);
        }

        @Override
        protected ZonedDateTime nowUtc() {
            return TEST_NOW;
        }
    }

}
