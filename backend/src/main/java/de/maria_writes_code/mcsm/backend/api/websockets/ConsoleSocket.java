package de.maria_writes_code.mcsm.backend.api.websockets;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import com.fasterxml.jackson.core.JsonProcessingException;
import de.maria_writes_code.mcsm.backend.api.websockets.abc.JsonPublisherSocket;
import de.maria_writes_code.mcsm.backend.features.server.ActiveServer;
import de.maria_writes_code.mcsm.backend.utils.Utils;

public class ConsoleSocket extends JsonPublisherSocket implements Consumer<String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleSocket.class);

    private final ActiveServer server;

    public ConsoleSocket(ActiveServer server) {
        this.server = server;
        var process = server.getProcess();
        if (process == null) {
            throw new IllegalStateException("Cannot open a socket on an empty server");
        }
        process.getConsoleEvent().subscribe(this);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);

        var backlog = Utils.throwIfNull(
            server.getProcess(),
            () -> new IllegalStateException("Server Process not available at socket opened")
        ).getConsoleState();
        sendMessage(session, new ConsoleBacklogObject(server.getId(), backlog));
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        LOGGER.error("Transport error on websocket", exception);
    }

    @Override
    public boolean supportsPartialMessages() {
        // no handling of messages occurs
        // so we don't care how large messages are received
        return true;
    }

    @Override
    public void accept(@Nullable String newLine) {
        if (newLine == null) {
            try {
                super.close(CloseStatus.NORMAL);
            } catch (IOExceptionGroup e) {
                LOGGER.error("One or more I/O errors occured while closing a session", e);
            }
            return;
        } else {
            try {
                publish(
                    new ConsoleLineObject(server.getId(), newLine)
                );
            } catch (JsonProcessingException e) {
                LOGGER.error("Failed to serialize console line object", e);
            } catch (IOExceptionGroup e) {
                LOGGER.error("One or more I/O errors occurred while sending console line", e);
            }
        }
    }

    private record ConsoleLineObject(UUID server_id, String line) { }
    private record ConsoleBacklogObject(UUID server_id, List<String> backlog) { }
}
