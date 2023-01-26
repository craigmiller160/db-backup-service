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

import io.craigmiller160.webutils.tls.TlsConfigurer;

public class Runner {

  private static final String TRUST_STORE_TYPE = "JKS";
  private static final String TRUST_STORE_PATH = "truststore.jks";
  private static final String TRUST_STORE_PASSWORD = "changeit";

  public static void main(final String[] args) {
    TlsConfigurer.INSTANCE.configureTlsTrustStore(
        TRUST_STORE_PATH, TRUST_STORE_TYPE, TRUST_STORE_PASSWORD);
    new Application().start();
  }
}
