package de.maria_writes_code.mcsm.backend.features.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.function.Consumer;

public class ConsoleHandler extends Thread {
    private final InputStream target;
    private final Context context;

    public static List<ConsoleHandler> combined(Process process, Context context) {
        return List.of(
            new ConsoleHandler(process.getInputStream(), context),
            new ConsoleHandler(process.getErrorStream(), context)
        );
    }

    public ConsoleHandler(InputStream target, Context context) {
        super("ConsoleHandler");
        this.target = target;
        this.context = context;
        setDaemon(true);
    }

    @Override
    public void run() {
        var stream = new BufferedReader(new InputStreamReader(target));
        String line;
        while (true) {
            try {
                line = stream.readLine();
            } catch (IOException e) {
                StoppableServerProcess.LOGGER.error("I/O error while reading process output", e);
                continue;
            }
            context.onLine.accept(line);
            context.history.add(line);

            if (line == null) {
                break;
            }
        }
    }

    public record Context(Consumer<String> onLine, List<String> history) { }
}