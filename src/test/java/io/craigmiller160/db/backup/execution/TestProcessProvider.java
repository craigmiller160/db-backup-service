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

import io.vavr.control.Option;
import java.io.IOException;
import java.util.Map;

public class TestProcessProvider implements ProcessProvider {

  private String[] command = null;
  private Map<String, String> environment = null;
  private final Process mockProcess;

  public TestProcessProvider(final Process mockProcess) {
    this.mockProcess = mockProcess;
  }

  @Override
  public Process provide(final String[] command, final Map<String, String> environment)
      throws IOException {
    this.command = command;
    this.environment = environment;
    return mockProcess;
  }

  public Option<String[]> getCommand() {
    return Option.of(command);
  }

  public Option<Map<String, String>> getEnvironment() {
    return Option.of(environment);
  }
}
