package io.craigmiller160.db.backup.config;

import java.util.List;

public record DatabaseConfig(
        String name,
        List<String> schemas
) {}
