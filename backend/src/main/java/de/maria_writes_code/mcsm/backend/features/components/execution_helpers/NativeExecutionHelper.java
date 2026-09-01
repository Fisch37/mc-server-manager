package de.maria_writes_code.mcsm.backend.features.components.execution_helpers;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.Consumer;

import de.maria_writes_code.mcsm.backend.features.runtimes.NoSuchRuntimeException;
import de.maria_writes_code.mcsm.backend.features.server.StoppableServerProcess;
import de.maria_writes_code.mcsm.backend.features.server.Terminator;
import de.maria_writes_code.mcsm.backend.features.templates.ServerTemplate;

public class NativeExecutionHelper implements ServerExecutionHelper {
    private final Path executable;

    public NativeExecutionHelper(Path executable) {
        this.executable = executable;
    }

    @Override
    public StoppableServerProcess startServer(Path location, ServerTemplate template, Map<String, String> properties,
            Consumer<Integer> onExit) throws IOException, NoSuchRuntimeException, IllegalStateException {
        var args = template.getDefinition().executable().arguments();
        var command = new ArrayList<String>(args.size());
        command.add(location.resolve(executable).toAbsolutePath().toString());
        command.addAll(args);
        return new StoppableServerProcess(
            new ProcessBuilder(command).directory(location.toFile()).start(),
            Terminator.create(template.getDefinition().executable().terminator()),
            onExit
        );
    }

    @Override
    public void waitUntilStarted(StoppableServerProcess process) {
        
    }
}
