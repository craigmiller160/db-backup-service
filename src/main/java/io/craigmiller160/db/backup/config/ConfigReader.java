package io.craigmiller160.db.backup.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.craigmiller160.db.backup.config.dto.BackupConfig;
import io.craigmiller160.db.backup.exception.ConfigReadException;

import java.io.IOException;

public class ConfigReader {

    private static final String CONFIG_PATH = "backup_config.json";
    private final ObjectMapper mapper = new ObjectMapper();

    public BackupConfig readBackupConfig() throws ConfigReadException {
        try (final var configStream = ConfigReader.class.getClassLoader().getResourceAsStream(CONFIG_PATH)) {
            return mapper.readValue(configStream, BackupConfig.class);
        } catch (final IOException ex) {
            throw new ConfigReadException("Error reading configuration file", ex);
        }
    }

}
