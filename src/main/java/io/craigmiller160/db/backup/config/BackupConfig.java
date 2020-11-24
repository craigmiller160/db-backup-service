package io.craigmiller160.db.backup.config;

import java.util.List;

public record BackupConfig(
        List<DatabaseConfig> databases
) {}
