package de.maria_writes_code.mcsm.backend.features.server;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.constraints.NotNull;

public class ServerProcess {
    private static final Logger LOGGER = LoggerFactory.getLogger("Server");

    @NotNull
    private Process process;
    @NotNull
    private Terminator terminator;

    public ServerProcess(Process process, Terminator terminator) {
        this.process = process;
        this.terminator = terminator;
    }

    public void sendCommand(String line) throws IOException {
        var writer = process.outputWriter();
        writer.append(line);
        writer.append('\n');
        writer.flush();
    }

    public Stream<String> getConsoleOut() {
        throw new NotImplementedException();
    }

    @Deprecated
    public ServerStatus getStatus() {
        try {
            return process.exitValue() == 0 ? ServerStatus.Stopped : ServerStatus.Crashed;
        } catch (IllegalStateException ignored) {
            return ServerStatus.Started;
        }
    }

    /**
     * @return Gets the exit status of the process
     * @throws IllegalStateException if the process is still running.
     */
    public int getExitValue() throws IllegalStateException {
        return process.exitValue();
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
