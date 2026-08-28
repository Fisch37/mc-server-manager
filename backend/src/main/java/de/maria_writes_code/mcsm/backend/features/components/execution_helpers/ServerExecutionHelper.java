package de.maria_writes_code.mcsm.backend.features.components.execution_helpers;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Consumer;

import org.jspecify.annotations.NullMarked;

import de.maria_writes_code.mcsm.backend.features.runtimes.NoSuchRuntimeException;
import de.maria_writes_code.mcsm.backend.features.server.ServerProcess;
import de.maria_writes_code.mcsm.backend.features.templates.ServerTemplate;

@NullMarked
public interface ServerExecutionHelper {
    ServerProcess startServer(Path location, ServerTemplate template, Map<String, String> properties, Consumer<Integer> onExit)
        throws IOException, NoSuchRuntimeException, IllegalStateException;
    
    void waitUntilStarted(ServerProcess process);
}
