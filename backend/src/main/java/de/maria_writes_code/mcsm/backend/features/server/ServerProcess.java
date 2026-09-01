package de.maria_writes_code.mcsm.backend.features.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.commons.lang3.NotImplementedException;
import org.jspecify.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.maria_writes_code.mcsm.backend.utils.Event;
import de.maria_writes_code.mcsm.backend.utils.ReadOnlyEvent;

public class ServerProcess {
    static final Logger LOGGER = LoggerFactory.getLogger("Server");

    @NonNull
    protected final Process process;
    @NonNull
    private Event<@Nullable String> consoleEvent;
    @NonNull
    private List<ConsoleHandler> consoleThreads;
    @NonNull
    private Thread waiterThread;
    @Nullable
    private Consumer<Integer> onExit;
    @NonNull
    private List<String> consoleHistory;

    public ServerProcess(Process process, Consumer<Integer> onExit) {
        this.process = process;
        this.consoleEvent = new Event<>();
        this.onExit = onExit;
        consoleHistory = Collections.synchronizedList(new ArrayList<>());
        consoleThreads = ConsoleHandler.combined(process, new ConsoleHandler.Context(consoleEvent::push, consoleHistory));
        consoleThreads.forEach(ConsoleHandler::start);
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

    /**
     * Returns an unmodifiable view of the console output.
     */
    public List<String> getConsoleState() {
        return Collections.unmodifiableList(consoleHistory);
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

    public int await() throws InterruptedException, ExecutionException {
        var completedProcess = process.onExit().get();
        return completedProcess.exitValue();
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
}
