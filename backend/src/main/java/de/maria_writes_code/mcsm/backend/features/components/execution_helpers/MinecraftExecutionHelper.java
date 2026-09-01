package de.maria_writes_code.mcsm.backend.features.components.execution_helpers;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.Consumer;

import org.jspecify.annotations.NullMarked;

import de.maria_writes_code.mcsm.backend.features.runtimes.NoSuchRuntimeException;
import de.maria_writes_code.mcsm.backend.features.runtimes.RuntimeProvider;
import de.maria_writes_code.mcsm.backend.features.server.StoppableServerProcess;
import de.maria_writes_code.mcsm.backend.features.server.Terminator;
import de.maria_writes_code.mcsm.backend.features.templates.ServerTemplate;
import de.maria_writes_code.mcsm.backend.utils.Utils;

@NullMarked
public class MinecraftExecutionHelper implements ServerExecutionHelper {
    private final RuntimeProvider runtimeProvider;

    public MinecraftExecutionHelper(RuntimeProvider runtimeProvider) {
        this.runtimeProvider = runtimeProvider;
    }

    /**
     * Start the server located at location assuming the passed template and returns the new process.
     * @param location The location of the server execution environment
     * @param template The template used for the server. Used to get the executable path and termination method.
     * @param properties The extra properties associated with this server. Must contain an {@code java-version} entry with a non-negative integer string.
     * @param onExit A function to call when the process exits. Receives the exit code.
     * @return The newly created ServerProcess.
     * @throws IOException if an I/O error occured while starting the server
     * @throws NoSuchRuntimeException if the runtime required by the {@code java-version} key is not installed
     * @throws IllegalStateException if {@code properties} is missing a key
     */
    @Override
    public StoppableServerProcess startServer(Path location, ServerTemplate template, Map<String, String> properties, Consumer<Integer> onExit)
        throws IOException, NoSuchRuntimeException, IllegalStateException
    {
        var executable = template.getDefinition().executable();
        int javaVersion;
        try {
            var javaVersionString = Utils.throwIfNull(
                properties.get("java-version"),
                () -> new IllegalStateException("Minecraft server missing required property 'java-version'")
            );
            javaVersion = Integer.parseUnsignedInt(javaVersionString);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Required property java-version is not interpretable as an integer");
        }
        var runtime = runtimeProvider.getRuntimeSupporting(javaVersion);
        if (runtime == null) {
            throw new NoSuchRuntimeException("Runtime for server does not exist");
        }
        var args = new ArrayList<String>();
        args.add(runtime.getExecutable().toString());
        args.addAll(executable.arguments());
        args.addAll(runtime.getArguments(location.resolve(executable.file())));
        var process = new ProcessBuilder(args)
            .directory(location.toFile())
            .redirectErrorStream(true)
            .start();
        return new StoppableServerProcess(
            process,
            Terminator.create(executable.terminator()),
            onExit
        );
    }

    @Override
    public void waitUntilStarted(StoppableServerProcess process) {
        // TODO: Implement this? Maybe.
    }
    
}
