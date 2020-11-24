package io.craigmiller160.db.backup.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record BackupConfig(
        @JsonProperty("databases") List<DatabaseConfig> databases
) {}
