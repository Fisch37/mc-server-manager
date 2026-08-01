package de.maria_writes_code.mcsm.backend.features.server;

import java.util.stream.Stream;

import org.apache.commons.lang3.NotImplementedException;

import jakarta.validation.constraints.NotNull;

public class ServerProcess {
    @NotNull
    private Process process;

    public void sendCommand(String line) {
        throw new NotImplementedException();
    }

    public Stream<String> getConsoleOut() {
        throw new NotImplementedException();
    }

    public ServerStatus getStatus() {
        try {
            return process.exitValue() == 0 ? ServerStatus.Stopped : ServerStatus.Crashed;
        } catch (IllegalStateException ignored) {
            return ServerStatus.Started;
        }
    }
}
