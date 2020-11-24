package io.craigmiller160.db.backup.config;

import io.craigmiller160.db.backup.config.dto.BackupConfig;
import io.craigmiller160.db.backup.config.dto.DatabaseConfig;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigReaderTest {

    private final ConfigReader configReader = new ConfigReader();

    @Test
    public void test_readBackupConfig() throws Exception {
        final var expected = new BackupConfig(
                List.of(
                        new DatabaseConfig(
                                "vm_dev",
                                List.of("public")
                        )
                )
        );
        final var actual = configReader.readBackupConfig();
        assertEquals(expected, actual);
    }

}
