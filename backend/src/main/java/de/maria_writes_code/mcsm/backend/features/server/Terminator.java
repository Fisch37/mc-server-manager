package de.maria_writes_code.mcsm.backend.features.server;

import java.io.BufferedOutputStream;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.maria_writes_code.mcsm.backend.features.templates.ServerTemplateDefinition;

public sealed interface Terminator permits Terminator.Signal, Terminator.Command {
    static final Logger LOGGER = LoggerFactory.getLogger("Server/Terminator");

    void terminate(Process target) throws IOException;

    public record Signal() implements Terminator {

        @Override
        public void terminate(Process target) {
            if (!target.supportsNormalTermination()) {
                // and we can't really do anything about it,
                // because we don't know if normal termination is
                // supported until we create a process.
                // What the actual fuck, Java.
                LOGGER.error(
                    "System does not support normal termination! "
                    + "This is absolutely terrible"
                );
            }
            target.destroy();
        }
    }

    public record Command(String line) implements Terminator {
        @Override
        public void terminate(Process target) throws IOException {
            try (var console = new BufferedOutputStream(target.getOutputStream())) {
                console.write(line.getBytes());
                console.write(System.lineSeparator().getBytes());
                console.flush();
            }
        }
    }

    public static Terminator create(ServerTemplateDefinition.Terminator terminator) {
        if (terminator instanceof ServerTemplateDefinition.CommandTerminator command) {
            return new Command(command.command());
        } else {
            return new Signal();
        }
    }
}
