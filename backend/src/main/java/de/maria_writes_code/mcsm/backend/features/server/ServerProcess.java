package de.maria_writes_code.mcsm.backend.features.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.commons.lang3.NotImplementedException;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.maria_writes_code.mcsm.backend.utils.Event;
import de.maria_writes_code.mcsm.backend.utils.ReadOnlyEvent;
import jakarta.validation.constraints.NotNull;

public class ServerProcess {
    private static final Logger LOGGER = LoggerFactory.getLogger("Server");

    @NotNull
    private Process process;
    @NotNull
    private Terminator terminator;
    @NotNull
    private Event<@Nullable String> consoleEvent;
    @NotNull
    private ConsoleHandler thread;
    @NotNull
    private Thread waiterThread;
    @Nullable
    private Consumer<Integer> onExit;

    public ServerProcess(Process process, Terminator terminator, Consumer<Integer> onExit) {
        this.process = process;
        this.terminator = terminator;
        this.consoleEvent = new Event<>();
        this.onExit = onExit;
        thread = new ConsoleHandler(process, consoleEvent::push);
        thread.start();
        waiterThread = Thread.ofVirtual()
            .name("WaitOnExit")
            .start(this::waitOnExit);
    }

    public void sendCommand(String line) throws IOException {
        var writer = process.outputWriter();
        writer.append(line);
        writer.append(System.lineSeparator());
        writer.flush();
    }

    @Deprecated
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

    /**
     * Returns the event associated with this console.
     * This event will fire every time a new line is printed to the console,
     * sending that line,
     * or once when the console is closed, at which point any remanining characters are sent.
     * @return A read only view of the event.
     */
    public ReadOnlyEvent<@Nullable String> getConsoleEvent() {
        return consoleEvent;
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

    private void waitOnExit() {
        try {
            onExit.accept(process.onExit().get().exitValue());
        } catch (InterruptedException e) {
            LOGGER.info("WaitOnExit interrupted while waiting", e);
        } catch (ExecutionException e) {
            LOGGER.error("Some error occurred while awaiting server exit", e);
        }
    }

    private static class ConsoleHandler extends Thread {
        private final Process target;
        private final Consumer<String> onLine;

        public ConsoleHandler(Process target, Consumer<String> onLine) {
            super("ConsoleHandler");
            this.target = target;
            this.onLine = onLine;
            setDaemon(true);
        }

        @Override
        public void run() {
            var stream = new BufferedReader(new InputStreamReader(target.getInputStream()));
            String line;
            while (true) {
                try {
                    line = stream.readLine();
                } catch (IOException e) {
                    LOGGER.error("I/O error while reading process output", e);
                    continue;
                }
                onLine.accept(line);
                if (line == null) {
                    break;
                }
            }
        }
    }

    private static class WaitOnExit extends Thread {
        private final Process target;
        private final Consumer<Integer> onExit;

        public WaitOnExit(Process target, Consumer<Integer> onExit) {
            this.target = target;
            this.onExit = onExit;
            super("WaitOnExit");
            setDaemon(true);
        }

        @Override
        public void run() {
            try {
                onExit.accept(target.onExit().get().exitValue());
            } catch (InterruptedException e) {
                LOGGER.info("WaitOnExit interrupted while waiting", e);
            } catch (ExecutionException e) {
                LOGGER.error("Some error occurred while awaiting server exit", e);
            }
        }
    }
}
