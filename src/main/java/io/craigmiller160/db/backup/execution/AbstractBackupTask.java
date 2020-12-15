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

import io.craigmiller160.db.backup.email.EmailService;
import io.craigmiller160.db.backup.exception.BackupException;
import io.craigmiller160.db.backup.properties.PropertyStore;
import io.vavr.control.Try;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public abstract class AbstractBackupTask implements Runnable {

    protected final PropertyStore propStore;
    protected final ProcessProvider processProvider;
    protected final EmailService emailService;

    protected AbstractBackupTask(final PropertyStore propStore,
                                 final ProcessProvider processProvider,
                                 final EmailService emailService) {
        this.propStore = propStore;
        this.processProvider = processProvider;
        this.emailService = emailService;
    }

    private Try<String> readStream(final InputStream stream) {
        return Try.withResources(() -> new BufferedReader(new InputStreamReader(stream)))
                .of(reader -> reader.lines().collect(Collectors.joining("\n")));
    }

    protected Try<String> readProcess(final Process process) {
        final var outputTry = readStream(process.getInputStream());
        final var errorTry = readStream(process.getErrorStream());

        return Try.of(process::waitFor)
                .flatMap(exitCode -> {
                    if (exitCode == 0) {
                        return outputTry;
                    }

                    return errorTry.flatMap(content -> Try.failure(new BackupException(content)));
                });
    }

}
