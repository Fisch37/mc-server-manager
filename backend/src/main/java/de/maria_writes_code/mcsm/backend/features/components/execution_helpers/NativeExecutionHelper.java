package de.maria_writes_code.mcsm.backend.features.components.execution_helpers;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;

import de.maria_writes_code.mcsm.backend.features.runtimes.NoSuchRuntimeException;
import de.maria_writes_code.mcsm.backend.features.server.StoppableServerProcess;
import de.maria_writes_code.mcsm.backend.features.server.Terminator;
import de.maria_writes_code.mcsm.backend.features.templates.ServerTemplate;

public abstract class NativeExecutionHelper implements ServerExecutionHelper {
    private final Path executable;
    private final Map<String, EnvironmentSource> environment;

    public NativeExecutionHelper(Path executable, Map<String, EnvironmentSource> environment) {
        this.executable = executable;
        this.environment = environment;
    }

    @Override
    public StoppableServerProcess startServer(Path location, ServerTemplate template, Map<String, String> properties,
            Consumer<Integer> onExit) throws IOException, NoSuchRuntimeException, IllegalStateException {
        var templateArgs = template.getDefinition().executable().arguments();
        var command = new ArrayList<String>(templateArgs.size());
        command.add(location.resolve(executable).toAbsolutePath().toString());
        command.addAll(getStandardArguments(properties));
        command.addAll(templateArgs);
        var processBuilder = new ProcessBuilder(command)
            .directory(location.toFile());
        for (var entry : environment.entrySet()) {
            var envValue = entry.getValue().getValue(location, properties);
            if (envValue != null)
                processBuilder.environment().put(entry.getKey(), envValue);
        }
        return new StoppableServerProcess(
            processBuilder.start(),
            Terminator.create(template.getDefinition().executable().terminator()),
            onExit
        );
    }

    @Override
    public void waitUntilStarted(StoppableServerProcess process) {
        
    }

    protected abstract List<String> getStandardArguments(Map<String, String> properties);

    @FunctionalInterface
    public static interface EnvironmentSource {
        String getValue(Path serverLocation, Map<String, String> serverProperties);

        public static final class Static implements EnvironmentSource {
            private final String value;

            public Static(String value) {
                this.value = value;
            }
            
            @Override @Nullable
            public String getValue(Path serverLocation, Map<String, String> serverProperties) {
                return value;
            }
        }
    }
}
