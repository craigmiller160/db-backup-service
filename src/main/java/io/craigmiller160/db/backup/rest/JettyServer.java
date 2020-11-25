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

package io.craigmiller160.db.backup.rest;

import io.craigmiller160.db.backup.properties.PropertyStore;
import io.vavr.control.Try;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;

public class JettyServer {

    private final PropertyStore propStore;
    private Server server;

    public JettyServer(final PropertyStore propStore) {
        this.propStore = propStore;
    }

    public Try<Void> start() {
        server = new Server(propStore.getJettyPort());
        final var handler = new ServletContextHandler(server, "/");

        final var jerseyServletHolder = new ServletHolder("JerseyServlet", ServletContainer.class);
        jerseyServletHolder.setInitOrder(0);
        jerseyServletHolder.setInitParameter("javax.ws.rs.Application", AppResourceConfig.class.getName());
        handler.addServlet(jerseyServletHolder, "/");

        return Try.run(() -> server.start());
    }

    public Try<Void> stop() {
        return Try.run(() -> {
            if (server != null) {
                server.stop();
            }
        });
    }

}
