package io.craigmiller160.db.backup.execution;

import java.io.IOException;
import java.util.Map;

@FunctionalInterface
public interface ProcessProvider {
    ProcessProvider DEFAULT = (command, environment) -> {
        final var processBuilder = new ProcessBuilder(command);
        final var processEnvironment = processBuilder.environment();
        environment.forEach(processEnvironment::put);
        return processBuilder.start();
    };

    Process provide(final String[] command, final Map<String,String> environment) throws IOException;

}
