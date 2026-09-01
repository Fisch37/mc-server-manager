package de.maria_writes_code.mcsm.backend.features.server;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import org.jspecify.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoppableServerProcess extends ServerProcess {
    static final Logger LOGGER = LoggerFactory.getLogger("Server");

    @NonNull
    private Terminator terminator;

    public StoppableServerProcess(Process process, Terminator terminator, Consumer<Integer> onExit) {
        super(process, onExit);
        this.terminator = terminator;
    }

    public void stop() throws IOException {
        terminator.terminate(process);
        var onExit = process.onExit();
        while (!onExit.isDone()) {
            try {
                onExit.get();
            } catch (InterruptedException ignored) {
            } catch (ExecutionException|CancellationException e) {
                LOGGER.error("Unexpected error while waiting on server termination", e);
                break;
            }
        }
    }
}
