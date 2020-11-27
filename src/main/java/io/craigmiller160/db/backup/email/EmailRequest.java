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

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record EmailRequest (
        @JsonProperty("toAddresses") List<String> toAddresses,
        @JsonProperty("ccAddresses") List<String> ccAddresses,
        @JsonProperty("bccAddresses") List<String> bccAddresses,
        @JsonProperty("subject") String subject,
        @JsonProperty("text") String text
) { }
