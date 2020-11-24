package io.craigmiller160.db.backup.properties;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearEnvironmentVariable;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PropertyReaderTest {

    private final PropertyReader propertyReader = new PropertyReader();

    @AfterEach
    @ClearEnvironmentVariable(key = "db.postgres.host")
    public void clean() {}

    @Test
    @SetEnvironmentVariable(key = "db.postgres.host", value = "TestHost")
    public void test_readProperties() throws Exception {
        final var propStore = propertyReader.readProperties().get();
        assertEquals("TestHost", propStore.getPostgresHost());
        assertEquals("30001", propStore.getPostgresPort());
        assertEquals("user", propStore.getPostgresUser());
        assertEquals("password", propStore.getPostgresPassword());
    }

}
