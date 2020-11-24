package io.craigmiller160.db.backup.properties;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PropertyReaderTest {

    private final PropertyReader propertyReader = new PropertyReader();

    @Test
    public void test_readProperties() throws Exception {
        final var propStore = propertyReader.readProperties().get();
        assertEquals("localhost", propStore.getPostgresHost());
        assertEquals("30001", propStore.getPostgresPort());
        assertEquals("user", propStore.getPostgresUser());
        assertEquals("password", propStore.getPostgresPassword());
    }

}
