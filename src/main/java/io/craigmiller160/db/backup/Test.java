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

import org.apache.commons.io.IOUtils;

import java.io.InputStream;

public class Test {

    // TODO delete this when done

    public static void main(final String[] args) throws Exception {
        final var processBuilder = new ProcessBuilder(
                "/home/craig/Applications/MongoDbTools/latest/bin/mongodump",
                "--uri=\"mongodb://user:password@localhost:30002/covid_19_dev?authSource=admin\"",
                "-o",
                "/home/craig/Documents/MongoDbDump"
        );
        final var process = processBuilder.start();
        final var output = getContent(process.getInputStream());
        final var error = getContent(process.getErrorStream());
        process.destroy();

        final var code = process.exitValue();
        System.out.println("Code: " + code);
        System.out.println("Output: " + output);
        System.out.println("Error: " + error);
    }

    private static String getContent(final InputStream stream) throws Exception {
        final String content = IOUtils.toString(stream, "UTF-8");
        stream.close();
        return content;
    }

}
