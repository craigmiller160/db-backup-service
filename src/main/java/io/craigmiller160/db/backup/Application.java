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

package io.craigmiller160.db.backup;

import io.craigmiller160.db.backup.config.ConfigReader;
import io.craigmiller160.db.backup.execution.BackupScheduler;
import io.craigmiller160.db.backup.execution.TaskFactory;
import io.craigmiller160.db.backup.properties.PropertyReader;
import io.craigmiller160.db.backup.server.JettyServer;
import io.vavr.Tuple;
import io.vavr.control.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);
    private static final Object LOCK = new Object();

    private final TaskFactory taskFactory = new TaskFactory();
    private BackupScheduler backupScheduler;
    private JettyServer jettyServer;

    public void start() {
        log.info("Starting application");
        new PropertyReader().readProperties()
                .flatMap(propStore ->
                        new ConfigReader(propStore).readBackupConfig()
                                .map(config -> Tuple.of(propStore, config))
                )
                .onSuccess(tuple -> {
                    jettyServer = new JettyServer(tuple._1);
                    jettyServer.start(); // TODO figure out a better way to do this

                    log.info("Setting up scheduler");
                    synchronized (LOCK) {
                        backupScheduler = new BackupScheduler(tuple._1, tuple._2, taskFactory);
                        backupScheduler.start();
                    }
                })
                .onFailure(ex -> log.error("Error starting application", ex));
    }

    // TODO how to trigger this when application is shutting down?
    public void stop() {
        log.info("Stopping scheduler");
        synchronized (LOCK) {
            Option.of(jettyServer)
                    .forEach(JettyServer::stop);
            Option.of(backupScheduler)
                    .forEach(BackupScheduler::stop);
        }
    }

}
