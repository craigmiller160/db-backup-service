package io.craigmiller160.db.backup.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.craigmiller160.db.backup.config.dto.BackupConfig;
import io.craigmiller160.db.backup.exception.ConfigReadException;
import io.vavr.control.Try;

public class ConfigReader {

    private static final String CONFIG_PATH = "backup_config.json";
    private final ObjectMapper mapper = new ObjectMapper();

    public Try<BackupConfig> readBackupConfig() {
        return Try.withResources(() -> ConfigReader.class.getClassLoader().getResourceAsStream(CONFIG_PATH))
                .of(configStream -> mapper.readValue(configStream, BackupConfig.class))
                .recoverWith(ex -> Try.failure(new ConfigReadException("Error reading configuration file", ex)));
    }

}
