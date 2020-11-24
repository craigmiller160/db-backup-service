package io.craigmiller160.db.backup.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record DatabaseConfig(
        @JsonProperty("name") String name,
        @JsonProperty("schemas") List<String> schemas
) {}
